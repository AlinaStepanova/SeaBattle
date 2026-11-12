# One-Tap Fire + Turn Arrows Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** One tap on the computer's board fires (no Fire button, no battle status text), and two pencil arrows between the boards show whose turn it is.

**Architecture:** `MainViewModel` fires straight from the computer-board tap and stops setting battle status strings (`statusResId` becomes nullable). A new stateless `TurnIndicator` composable draws the two arrows in the gap that `portraitBoardsLayout` / `landscapeBoardsLayout` now reserve for it; the arrow geometry and the "which arrow is active" mapping are `internal` pure functions, unit-tested like `hitCrossStrokes`.

**Tech Stack:** Kotlin, Jetpack Compose (Material 3, Canvas), JUnit 4, kotlinx-coroutines-test, Compose UI tests.

**Spec:** GitHub issue #23, "Remove the Fire button (one tap fires) + turn arrows between the boards" (`gh issue view 23`). Decision made with the user on top of it: the computer's-turn spinner is removed.

## Global Constraints

- Branch `feature/one-tap-fire` (from `dev-v2`); the PR targets `dev-v2`. CI does not run on `dev-v2`, so unit tests, lint and instrumented tests run locally (instrumented on the connected Pixel 10).
- Commits are authored by the user only: no `Co-Authored-By` line. Run `./gradlew testDebugUnitTest` before every commit and fix failures first.
- Run `/code-review` on the branch before opening the PR and go through the findings with the user.
- Every user-facing string exists in `values/`, `values-uk/` and `values-ru/`.
- Comments only where the logic needs them; reuse existing UI pieces (`GameButton`, `StatusText`, `SeaBattleTheme.colors`).
- Arrows: drawn in the pen style (round caps) in `SeaBattleTheme.colors.foreground`, light and dark; turn switch animated over 200 ms.
- Placement and game-over text are unchanged; nothing changes in the placement layouts.

## Review Focus

- A tap on a dot drawn around a sunk computer ship is a cell already shot at: it must be ignored and must not pass the turn (test in Task 1).
- Extra taps after the winning shot do nothing, and the arrows are gone at game over (tests in Tasks 1 and 4).
- "New game" during the computer's turn lands in placement with no arrows and no stale "Opponent's turn" (test in Task 4).
- Rotating mid-battle: the same turn must light ↓ in portrait and → in landscape (direction-mapping test in Task 3, UI tests in both orientations in Task 4).
- Compact windows (split screen) and the 640×360 dp landscape phone: the gap between the grids must still hold an arrow (layout tests in Task 3).

---

### Task 1: One-tap firing, no Fire button, no spinner

**Files:**
- Modify: `app/src/main/java/com/avs/sea/battle/main/MainViewModel.kt` (`handlePCAreaClick`, `makeFireAsPerson`)
- Modify: `app/src/main/java/com/avs/sea/battle/ui/GameUiState.kt`
- Modify: `app/src/main/java/com/avs/sea/battle/ui/GameScreen.kt` (`GameScreen` params, `ComputerBoard`, `ActionArea`, `SPINNER_SIZE`)
- Modify: `app/src/main/java/com/avs/sea/battle/main/MainActivity.kt`
- Modify: `app/src/main/res/values{,-uk,-ru}/strings.xml` (delete `fire_text`)
- Test: `app/src/test/java/com/avs/sea/battle/main/MainViewModelTest.kt`, `MainViewModelPlacementTest.kt`; `app/src/androidTest/.../ui/GameScreenSpacingTest.kt`, `GameScreenLandscapeTest.kt`

**Interfaces:**
- Produces: `MainViewModel.onComputerBoardTap(coordinate: Coordinate)` (replaces `handlePCAreaClick` + `makeFireAsPerson`; named to match `onPersonBoardTap`); private `fireAt(coordinate: Coordinate)` holding the old shot logic. `GameUiState.selectedCoordinate` is removed. `GameScreen` loses `onFire` and renames `onCellClick` to `onComputerCellClick`.

- [ ] **Step 1: Rewrite the firing tests in `MainViewModelTest`** (setUp already shuffles and starts the game; helpers `findFreeEmptyCell`, `winGame`, `loseGame` switch to `onComputerBoardTap`). Replace `makeFireAsPerson*` and `cellCannotBeSelectedDuringComputerTurn` with:
  - `tappingAFreeEmptyCellFiresAndPassesTheTurn`: tap `findFreeEmptyCell()` → `phase == COMPUTER_TURN`, cell in `computerBoardDots`.
  - `tappingAShipCellHitsAndKeepsTheTurn`: tap the first ship cell → `phase == BATTLE`, cell in `computerBoardCrosses`.
  - `tappingAnAlreadyHitCellIsIgnored`: tap a ship cell twice → state after the second tap equals the state after the first.
  - `tappingAMarkedCellAroundASunkShipIsIgnored`: hit every cell of one ship, then tap a cell from `computerBoardDots` → state unchanged, `phase == BATTLE`.
  - `aDoubleTapOnAMissFiresOnce`: tap the same free cell twice, then another free cell → `computerBoardDots` equals the dots after the first tap, `phase == COMPUTER_TURN`.
  - `tapsDuringTheComputersTurnAreIgnored`: miss, then tap a ship cell before advancing the dispatcher → `computerBoardCrosses` unchanged.
  - `tapsAfterGameOverAreIgnored`: `winGame()`, then tap a free cell → state unchanged.
  In `MainViewModelPlacementTest`, switch `missOnce` and the game-over loop to `onComputerBoardTap`.

- [ ] **Step 2: Run them to see them fail**
  Run: `./gradlew testDebugUnitTest --tests "com.avs.sea.battle.main.MainViewModel*"`
  Expected: compile failure, `onComputerBoardTap` unresolved.

- [ ] **Step 3: Implement.** `onComputerBoardTap` returns unless `activePlayer == Player.PERSON && computerBattleField.isCellFreeToBeSelected(coordinate)`, then calls `fireAt`. `fireAt` is `makeFireAsPerson`'s body without the selection (keep the status strings for now; Task 2 removes them). Remove `selectedCoordinate` from `GameUiState` and every `copy(selectedCoordinate = …)`. In `GameScreen`: drop `onFire`, rename `onCellClick` → `onComputerCellClick`, `ComputerBoard` passes `highlighted = emptyList()`, `ActionArea(uiState, onNewGame)` shows only the New game button at `OVER` (the `BATTLE`/`COMPUTER_TURN` branches and the spinner, `SPINNER_SIZE` and `CircularProgressIndicator` go). Wire `MainActivity` (`onComputerCellClick = viewModel::onComputerBoardTap`), the preview and both UI-test harnesses. Delete `fire_text` in en/uk/ru. Delete `computerTurnSpinnerIsSmall` in `GameScreenSpacingTest`.

- [ ] **Step 4: Run the unit tests**
  Run: `./gradlew testDebugUnitTest`
  Expected: PASS.

- [ ] **Step 5: Commit**
  `git commit -m "fire with one tap on the computer's board and drop the Fire button"`

### Task 2: No status text during the battle

**Files:**
- Modify: `GameUiState.kt`, `MainViewModel.kt` (`startGame`, `fireAt`, `playAsComputer`), `GameScreen.kt` (battle `Column`, `StatusText`, `LandscapePlacement`, `PortraitPlacement`)
- Modify: `app/src/main/res/values{,-uk,-ru}/strings.xml`
- Test: `MainViewModelTest.kt`, `GameScreenSpacingTest.kt`

**Interfaces:**
- Consumes: `onComputerBoardTap` (Task 1).
- Produces: `GameUiState.statusResId: Int?` (default still `R.string.status_place_ships_text`; `null` during `BATTLE` and `COMPUTER_TURN`).

- [ ] **Step 1: Write the failing tests.**
  - `MainViewModelTest.battleShowsNoStatusText`: after start → `statusResId == null`; after a hit → `null`; after a miss and `advanceUntilIdle()` → `null`. Change `startedGameIsInBattlePhaseWithShipsPlaced` to `assertNull(statusResId)`. The game-over assertions stay.
  - `GameScreenSpacingTest.battleShowsNoActionButtons`: for `BATTLE` and `COMPUTER_TURN` states, `onAllNodes(hasClickAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))` is empty.
  - `GameScreenSpacingTest.boardsStayPutWhenTheGameEnds` (portrait and landscape): drive `GameScreen` from a `mutableStateOf` holding a `BATTLE` state, record both grids' bounds, switch to the `OVER` state, `waitForIdle()` → bounds unchanged (within 0.5 dp). Give `setGameScreen` a variant that takes the state holder.

- [ ] **Step 2: Run them to see them fail**
  Run: `./gradlew testDebugUnitTest --tests "com.avs.sea.battle.main.MainViewModelTest"`
  Expected: FAIL, `statusResId` is a string id during the battle.

- [ ] **Step 3: Implement.** `startGame` sets `statusResId = null`; `fireAt` and `playAsComputer` stop touching `statusResId`; `endGame` is unchanged. `StatusText(statusResId: Int?, …)` renders `""` for `null`, so the battle keeps a one-line status slot and the boards don't move when the game-over text appears (say so in one comment). In both placement layouts `uiState.statusResId` is passed as is. Delete `status_select_to_fire_text`, `status_shot_ship_again_text`, `status_opponent_shot_text` and `status_opponent_shot_again_text` in en/uk/ru.

- [ ] **Step 4: Run the tests**
  Run: `./gradlew testDebugUnitTest` → PASS; `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.avs.sea.battle.ui.GameScreenSpacingTest` → PASS.

- [ ] **Step 5: Commit**
  `git commit -m "drop the battle status text and its strings"`

### Task 3: Arrow geometry and a gap that holds the arrows

**Files:**
- Create: `app/src/main/java/com/avs/sea/battle/ui/TurnIndicator.kt` (geometry and mapping only in this task)
- Modify: `GameScreen.kt` (`portraitBoardsLayout`, `landscapeBoardsLayout`)
- Test: create `app/src/test/java/com/avs/sea/battle/ui/TurnArrowsTest.kt`; modify `PortraitBoardsLayoutTest.kt`, `LandscapeBoardsLayoutTest.kt`

**Interfaces:**
- Produces, in `TurnIndicator.kt`:
  - `internal enum class ArrowDirection { UP, DOWN, LEFT, RIGHT }`
  - `internal fun activeArrowDirection(phase: GamePhase, isLandscape: Boolean): ArrowDirection?`: `BATTLE` → `DOWN` (portrait) / `RIGHT` (landscape); `COMPUTER_TURN` → `UP` / `LEFT`; `PLACING`, `OVER` → `null`.
  - `internal fun turnArrowStrokes(size: Float, direction: ArrowDirection): List<Pair<Offset, Offset>>`: `[shaft, headLeft, headRight]` in a `size × size` box. The shaft is centred and spans 80% of the box (10%–90%) and ends at the tip; both head strokes start at the tip and run back at 45°, each `0.3 * size` long.
  - `internal val TURN_ARROW_SIZE = 28.dp`
- Layout contract: `portraitBoardsLayout(...).gap >= TURN_ARROW_SIZE` and `landscapeBoardsLayout(...).gap >= TURN_ARROW_SIZE` whenever `boardSize > 0`.

- [ ] **Step 1: Write the failing tests.**
  - `TurnArrowsTest.activeArrowPointsAtTheBoardBeingShotAt`: the six mappings above, plus `null` for `PLACING` and `OVER` in both orientations.
  - `TurnArrowsTest.shaftIsCentredAndSpansEightyPercent`: `DOWN` with size 100 → shaft `(50,10)` to `(50,90)`; `RIGHT` → `(10,50)` to `(90,50)`.
  - `TurnArrowsTest.headMeetsAtTheTipOnTheSideTheArrowPoints`: the tip is `(50,90)` for `DOWN`, `(50,10)` for `UP`, `(90,50)` for `RIGHT` and `(10,50)` for `LEFT`; both head strokes start there.
  - `TurnArrowsTest.headStrokesAreMirroredAt45Degrees`: each head stroke is 30 long, its |dx| equals |dy|, and the two end points are mirrored across the shaft.
  - `PortraitBoardsLayoutTest.gapAlwaysHoldsTheTurnArrows` over the existing `screens`: `boardSize == 0.dp || gap >= TURN_ARROW_SIZE`.
  - `LandscapeBoardsLayoutTest.gapAlwaysHoldsTheTurnArrows` over the regular and compact screens, with the same assertion. Rename `gridsAreExactlyTwoSquaresApartOnAnyScreen` to `gridsAreAWholeNumberOfSquaresApartAndAtLeastTwo`: narrow screens now need a third square. `boardsUseEightyPercentOfTheWidthOnATallScreen` and `boardsFillTheHeightOnAWideScreen` must keep passing; they pin "boards no smaller than today" on phone-sized screens.

- [ ] **Step 2: Run them to see them fail**
  Run: `./gradlew testDebugUnitTest --tests "com.avs.sea.battle.ui.*"`
  Expected: compile failure (`TurnIndicator.kt` symbols), then gap assertions fail on narrow screens.

- [ ] **Step 3: Implement.** In both layout functions the "fits" condition for a number of squares becomes `cell * squares - LABEL_SIZE >= TURN_ARROW_SIZE` (it was `>= 0` for portrait and `cell * squares >= LABEL_SIZE` for landscape). Portrait's even-share rounding stays and is clamped to that minimum. Only very compact windows get slightly smaller boards; phones keep theirs.

- [ ] **Step 4: Run the tests**
  Run: `./gradlew testDebugUnitTest`
  Expected: PASS.

- [ ] **Step 5: Commit**
  `git commit -m "add turn arrow geometry and keep room for the arrows between the boards"`

### Task 4: `TurnIndicator` between the boards

**Files:**
- Modify: `app/src/main/java/com/avs/sea/battle/ui/TurnIndicator.kt` (the composable)
- Modify: `GameScreen.kt` (`Boards`)
- Modify: `app/src/main/res/values{,-uk,-ru}/strings.xml`
- Test: create `app/src/androidTest/java/com/avs/sea/battle/ui/TurnIndicatorTest.kt` and `app/src/androidTest/java/com/avs/sea/battle/main/OneTapFireTest.kt`

**Interfaces:**
- Consumes: `activeArrowDirection`, `turnArrowStrokes`, `TURN_ARROW_SIZE`, `ArrowDirection` (Task 3); `onComputerBoardTap` (Task 1).
- Produces:
  - `@Composable internal fun TurnIndicator(phase: GamePhase, isLandscape: Boolean, modifier: Modifier = Modifier)`
  - `internal fun turnArrowTag(direction: ArrowDirection): String` (`"turn_arrow_${direction.name}"`)
  - `internal const val TURN_INDICATOR_TAG = "turn_indicator"`
  - Strings `turn_yours_text` (en "Your turn", uk "Ваш хід", ru "Ваш ход") and `turn_opponent_text` (en "Opponent's turn", uk "Хід опонента", ru "Ход оппонента").

- [ ] **Step 1: Write the failing UI tests.** In `TurnIndicatorTest`, render `GameScreen` the same way `GameScreenSpacingTest` does:
  - `portraitYourTurnLightsTheArrowPointingDown`: `BATTLE`, portrait → `turnArrowTag(DOWN)` `assertIsSelected()`, `turnArrowTag(UP)` `assertIsNotSelected()`.
  - `portraitComputersTurnLightsTheArrowPointingUp`, `landscapeYourTurnLightsTheArrowPointingRight`, `landscapeComputersTurnLightsTheArrowPointingLeft`: the same pattern.
  - `arrowsSitBetweenTheGrids`: portrait → both arrows' bounds lie between the person grid's bottom and the computer grid's top; landscape → between the person grid's right edge and the computer grid's left edge.
  - `arrowsAreHiddenDuringPlacementAndAtGameOver`: `PLACING` and `OVER` → no node with any `turnArrowTag`.
  - `turnIndicatorIsAPoliteLiveRegion`: `BATTLE` → the `TURN_INDICATOR_TAG` node has content description `turn_yours_text` and `SemanticsProperties.LiveRegion == LiveRegionMode.Polite`; `COMPUTER_TURN` → `turn_opponent_text`.
  In `OneTapFireTest` (real `MainActivity`, `ViewModelProvider` like `ManualPlacementTest`), start the game with Start Game, then:
  - `oneTapOnTheComputersBoardFires`: tap a cell that `getComputerBattleField()` reports as free and not a ship (tap the second `BOARD_GRID_TAG` node, using `ManualPlacementTest`'s cell maths) → `phase == COMPUTER_TURN` and the cell is in `computerBoardDots`.
  - `aMissHandsTheArrowToThePlayersBoard`: after that miss → `turnArrowTag(UP)` is selected and the indicator reads `turn_opponent_text`.
  - `newGameDuringTheComputersTurnHidesTheArrows`: after the miss, ⋮ → New game → Yes → no `turnArrowTag` nodes.

- [ ] **Step 2: Run them to see them fail**
  Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.avs.sea.battle`
  Expected: the new tests fail (no indicator yet).

- [ ] **Step 3: Implement `TurnIndicator`.**
  - When `activeArrowDirection` is `null` it draws an empty `Box(modifier)`, so the gap stays the same size.
  - Otherwise there are two arrows of `TURN_ARROW_SIZE`, centred and 8 dp apart: a `Row` of ↓ ↑ in portrait (the arrow toward the computer first), a `Column` of → ← in landscape.
  - Each arrow is a `Canvas` drawing `turnArrowStrokes` with round caps. It animates `animateFloatAsState(if (active) 1f else 0f, tween(200))` between faint (`foreground` at 0.25 alpha, stroke `0.06 * size`) and active (`foreground`, stroke `0.1 * size`).
  - Each arrow node has `testTag(turnArrowTag(direction))` and `semantics { selected = active }`.
  - The container has `testTag(TURN_INDICATOR_TAG)` and `semantics { liveRegion = LiveRegionMode.Polite; contentDescription = <turn string> }`.
  - In `Boards`, replace `spacedBy(layout.gap)` with an explicit `TurnIndicator` between the two boards: portrait `Modifier.size(width = layout.boardSize, height = layout.gap).padding(start = LABEL_SIZE)`; landscape `Modifier.size(width = layout.gap, height = layout.boardSize).padding(top = LABEL_SIZE)`. The arrows then centre on the grid, not on the labels.
  - Add the two strings in en/uk/ru.

- [ ] **Step 4: Run the tests**
  Run: `./gradlew testDebugUnitTest` → PASS; the connected run from Step 2 → PASS, including the existing spacing and landscape tests.

- [ ] **Step 5: Commit**
  `git commit -m "show whose turn it is with arrows between the boards"`

### Task 5: Store screenshots and docs

**Files:**
- Modify: `tools/store-assets/gen_screenshot_test.py`
- Modify: `CLAUDE.md` (untracked, local only; not committed)

- [ ] **Step 1: Update the generator.** `battle` and `computer_turn` get no status (`state_kt` emits `statusResId = null` when a state has no `status`). Drop `selected` and the `selectedCoordinate` emission. In the `GameScreen` call, drop `onFire` and rename `onCellClick` → `onComputerCellClick`.
- [ ] **Step 2: Verify** as on PR #32. Generate `StoreScreenshots.kt`, `installDebug installDebugAndroidTest`, `am instrument` `StoreScreenshots#battle` and `#computer_turn`, pull both PNGs and check them: ↓ lit on `battle`, ↑ on `computer_turn`, no status text, no Fire button. Then delete the generated file and uninstall `com.avs.sea.battle.test`.
- [ ] **Step 3: Update `CLAUDE.md`'s architecture notes:** `onComputerBoardTap`, `TurnIndicator` in `ui/`, and no status text during the battle.
- [ ] **Step 4: Commit** the generator only: `git commit -m "update the store screenshot generator for one-tap firing"`

### Task 6: Verify, review, PR

- [ ] **Step 1:** `./gradlew testDebugUnitTest lintDebug` → all pass. Run `./gradlew connectedDebugAndroidTest` on the Pixel 10 with the screen kept awake → all pass.
- [ ] **Step 2:** Install on the Pixel 10 and check portrait and landscape in light and dark: one tap fires, the arrows switch on every turn change (including chains of hits), they're hidden in placement and at game over, and TalkBack announces the turn.
- [ ] **Step 3:** Run `/code-review` on `feature/one-tap-fire` against `dev-v2`. Show the user the findings and fix the agreed ones (unit tests before each commit).
- [ ] **Step 4:** Push and open the PR into `dev-v2` with "Closes #23", ending with the attribution line.
