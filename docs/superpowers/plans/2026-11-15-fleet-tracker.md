# Remaining-Fleet Tracker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** During the battle, each player's 10 ships are shown as a row of small pencil ships next to their board, and a ship is struck through in red the moment it sinks.

**Architecture:** `BattleField.sunkShipSizes()` reads sunk ships from the model, a pure `fleetShips(sunkSizes)` turns them into `GameUiState.personFleet` / `computerFleet`, and a stateless `FleetTracker` composable draws them. `Boards` reserves room for the rows: in portrait one row above your board and one below the opponent's, with the game-over text moved into the gap between the boards; in landscape a strip under the boards holds both rows (outer-aligned) and the New game button between them.

**Tech Stack:** Kotlin, Jetpack Compose (Canvas), JUnit 4, kotlinx-coroutines-test, Compose UI tests.

**Spec:** GitHub issue #30, "Remaining-fleet tracker under each board" (`gh issue view 30`). Decisions made with the user on top of it:
- **Portrait:** your row goes above your board and the opponent's below theirs. At game over the result text moves into the gap between the boards.
- **Landscape:** each row goes under its board in the bottom strip: yours aligned left, the opponent's aligned right. At game over New game sits between them, and the mini ships shrink on small phones.
- **Sunk ships:** one red strike-through (the hit colour) across the whole ship. Afloat ships are hatched cells, like ships on the board.

## Global Constraints

- **Branch and PR:** branch `feature/fleet-tracker` (from `dev-v2`); the PR targets `dev-v2`. CI does not run on `dev-v2`: run unit tests, lint and instrumented tests locally (instrumented on the connected Pixel 10 or Pixel 8a).
- **Commits:** authored by the user only, with no `Co-Authored-By` line. Run `./gradlew testDebugUnitTest` before every commit.
- **Review:** run `/code-review` on the branch before opening the PR and go through the findings with the user.
- **Strings:** every user-facing string exists in `values/`, `values-uk/` and `values-ru/`.
- **Reuse:** use `hatchStrokes`, `hitStrokeWidth`, `SeaBattleTheme.colors` (`foreground`, `shipHatch`, `hit`), `StatusText` and `GameButton`. Comments only where the logic needs them.
- **Board size:** the boards must not shrink on common phones: portrait keeps 80% of the width on 360–412 dp phones, and landscape keeps the same height.
- **Placement:** placement screens are unchanged and show no fleet rows.
- **TalkBack:** deprioritised by the user. Only the simple afloat-count description from the ticket is in scope.

## Review Focus

- A hit that doesn't sink must leave the row unchanged, on either side (test in Task 2).
- When a ship of a repeated size sinks, the leftmost afloat ship of that size is crossed out, so two sunk 2-decks always show as the first two (test in Task 2).
- At game over the loser's row shows all ten struck through and stays visible (tests in Tasks 2 and 4).
- Landscape on a 640×360 dp phone: New game at game over must not overlap either row (test in Task 4).
- Portrait game-over text in the gap at font scale 1.3 must not run into the grids (test in Task 4).

---

### Task 1: Sunk ship sizes from the model

**Files:**
- Modify: `app/src/main/java/com/avs/sea/battle/battle_field/BattleField.kt`
- Test: `app/src/test/java/com/avs/sea/battle/battle_field/BattleFieldTest.kt`

**Interfaces:**
- Produces: `fun BattleField.sunkShipSizes(): List<Int>`: the length of every sunk ship, derived from `Ship.isDead()` and `getLength()`; nothing new is stored.

- [ ] **Step 1: Write the failing tests** against a fixed `FleetLayout` placed with `placeShips` (reuse an existing hand-built layout from `BattleFieldTest` if one fits, otherwise build one; all ships must be listed):
  - `noShipIsSunkBeforeAnyShot` → `emptyList()`.
  - `aHitThatDoesNotSinkCountsNothing`: one hit on the 4-deck → `emptyList()`.
  - `aSunkShipIsCountedBySize`: every cell of one 2-deck hit → `listOf(2)`.
  - `severalSunkShipsAreAllCounted`: sink the 4-deck and two 1-decks → `listOf(4, 1, 1)` in any order, compared as sorted lists.
  - `allShipsSunkGivesTheWholeFleet`: hit every ship cell → sorted equals `FLEET.sorted()`.
- [ ] **Step 2: Run** `./gradlew testDebugUnitTest --tests "com.avs.sea.battle.battle_field.BattleFieldTest"`. Expected: compile failure, `sunkShipSizes` unresolved.
- [ ] **Step 3: Implement** `sunkShipSizes()` as a one-line filter/map over `ships`.
- [ ] **Step 4: Run** `./gradlew testDebugUnitTest`. Expected: PASS.
- [ ] **Step 5: Commit** `git commit -m "report the sizes of sunk ships from the battle field"`

### Task 2: Fleet state in `GameUiState`

**Files:**
- Modify: `app/src/main/java/com/avs/sea/battle/ui/GameUiState.kt`, `app/src/main/java/com/avs/sea/battle/main/MainViewModel.kt`
- Test: create `app/src/test/java/com/avs/sea/battle/ui/FleetShipsTest.kt`; modify `MainViewModelTest.kt`

**Interfaces:**
- Consumes: `sunkShipSizes()` (Task 1), `FLEET`.
- Produces:
  - `data class FleetShipUi(val size: Int, val sunk: Boolean)` in `GameUiState.kt`.
  - `internal fun fleetShips(sunkSizes: List<Int>): List<FleetShipUi>` in `GameUiState.kt`: one entry per `FLEET` ship, in `FLEET` order. Within a size, the first *k* are `sunk = true`, where *k* is how often that size appears in `sunkSizes`.
  - `GameUiState.personFleet` and `GameUiState.computerFleet: List<FleetShipUi>`, both `emptyList()` by default (placement).

- [ ] **Step 1: Write the failing tests.**
  - `FleetShipsTest.noneSunkListsTheWholeFleetAfloat`: sizes are `[4,3,3,2,2,2,1,1,1,1]` and all `sunk == false`.
  - `FleetShipsTest.sunkShipsAreTheFirstOfTheirSize`: `fleetShips(listOf(2, 2, 1))` → the entries at indices 3 and 4 (the first two 2-decks) and 6 (the first 1-deck) are sunk, and the rest are not.
  - `FleetShipsTest.allSunk`: `fleetShips(FLEET)` → all sunk.
  - `MainViewModelTest`:
    - `battleStartsWithBothFleetsAfloat`: both lists equal `fleetShips(emptyList())`.
    - `sinkingAComputerShipStrikesItInTheComputerFleet`: hit computer ship cells until `computerBoardDots` is non-empty, using the same loop as `tappingAMarkedCellAroundASunkShipIsIgnored` → exactly one `sunk` entry in `computerFleet`, and `personFleet` is unchanged.
    - `aHitThatDoesNotSinkLeavesTheFleetUnchanged`: one hit on a cell whose ship is still afloat afterwards (check `computerBoardDots` is empty) → `computerFleet` has no `sunk` entry.
    - `theComputersShotsUpdateThePlayersFleet`: run `loseGame()` → every `personFleet` entry is sunk at `OVER`.
    - `aNewGameClearsBothFleets`: after `startNewGame()` → both lists are empty.
- [ ] **Step 2: Run** `./gradlew testDebugUnitTest --tests "com.avs.sea.battle.*"`. Expected: compile failure (`fleetShips`, `FleetShipUi`).
- [ ] **Step 3: Implement.** `startGame` sets both fleets to `fleetShips(emptyList())`. `fireAt` sets `computerFleet` from `computerBattleField.sunkShipSizes()` on a hit, and the computer's hit branch in `playAsComputer` sets `personFleet` from `personBattleField.sunkShipSizes()`. `initValues` already resets to the empty defaults.
- [ ] **Step 4: Run** `./gradlew testDebugUnitTest`. Expected: PASS.
- [ ] **Step 5: Commit** `git commit -m "keep both fleets in the game state"`

### Task 3: `FleetTracker` composable

**Files:**
- Create: `app/src/main/java/com/avs/sea/battle/ui/FleetTracker.kt`
- Modify: `app/src/main/res/values{,-uk,-ru}/strings.xml`
- Test: create `app/src/test/java/com/avs/sea/battle/ui/FleetTrackerLayoutTest.kt` and `app/src/androidTest/java/com/avs/sea/battle/ui/FleetTrackerTest.kt`

**Interfaces:**
- Consumes: `FleetShipUi` (Task 2).
- Produces:
  - `internal fun fleetTrackerCell(maxWidth: Dp): Dp`: the mini-ship cell size, `min(10.dp, maxWidth / 24.5f)`. A row is 20 cells plus 9 gaps of half a cell.
  - `@Composable internal fun FleetTracker(ships: List<FleetShipUi>, isOpponent: Boolean, alignment: Alignment.Horizontal, modifier: Modifier = Modifier)`. It draws nothing for an empty list.
  - `internal fun fleetTrackerTag(isOpponent: Boolean): String` (`"fleet_tracker_opponent"` / `"fleet_tracker_yours"`) and `internal fun fleetShipTag(index: Int): String` (`"fleet_ship_$index"`).
  - `internal val SunkShipKey = SemanticsPropertyKey<Boolean>("SunkShip")` with `var SemanticsPropertyReceiver.sunkShip`. It's a test-visible custom key; accessibility services don't see custom keys, so TalkBack doesn't stop on each ship.
  - Strings `fleet_yours_afloat` (en "Your fleet: %1$d of %2$d ships afloat", uk "Ваш флот: на плаву %1$d з %2$d", ru "Ваш флот: на плаву %1$d из %2$d") and `fleet_opponent_afloat` (en "Opponent\'s fleet: %1$d of %2$d ships afloat", uk "Флот опонента: на плаву %1$d з %2$d", ru "Флот оппонента: на плаву %1$d из %2$d").

- [ ] **Step 1: Write the failing tests.**
  - `FleetTrackerLayoutTest.cellIsCappedOnWideRows`: `fleetTrackerCell(309.dp) == 10.dp`.
  - `FleetTrackerLayoutTest.cellShrinksToFitNarrowRows`: `fleetTrackerCell(147.dp) == 6.dp`.
  - `FleetTrackerLayoutTest.rowNeverOverflows`: for 100, 147, 245 and 400 dp, `fleetTrackerCell(w) * 24.5f <= w + 0.01.dp`.
  - `FleetTrackerTest` renders `FleetTracker` in a fixed 300 dp box with `fleetShips(listOf(3))`:
    - `aSunkShipIsStruckThrough`: the `fleetShipTag(1)` node has `SunkShipKey == true`, and `fleetShipTag(0)` and `fleetShipTag(2)` have `false`.
    - `shipsKeepTheirLengths`: the `fleetShipTag(0)` width is four times the `fleetShipTag(9)` width (±0.5 dp).
    - `rowReadsTheAfloatCount`: the tracker node's content description is "Opponent's fleet: 9 of 10 ships afloat" (via `string(R.string.fleet_opponent_afloat, 9, 10)`).
    - `emptyFleetDrawsNothing`: with `emptyList()`, no `fleetShipTag(0)` node.
- [ ] **Step 2: Run** the unit test (compile failure expected), then `connectedDebugAndroidTest` for `FleetTrackerTest` (fails).
- [ ] **Step 3: Implement.**
  - A `Row` with `spacedBy(cell / 2, alignment)` holds one `Canvas(Modifier.size(cell * size, cell))` per ship.
  - Each canvas outlines the ship and its cell dividers in `foreground`, and hatches each cell with `hatchStrokes(cell)` in `shipHatch`.
  - A sunk ship gets one `hit`-coloured line through its middle, `hitStrokeWidth(cell)` wide with round caps.
  - Each canvas has `testTag(fleetShipTag(i))` and `semantics { sunkShip = ship.sunk }`.
  - The row has the tracker tag and `semantics { contentDescription = … }` with the afloat count and the total.
  - Add the strings.
- [ ] **Step 4: Run** the unit suite and the `FleetTrackerTest` class. Expected: PASS.
- [ ] **Step 5: Commit** `git commit -m "add the fleet tracker row"`

### Task 4: Place the rows and move the portrait game-over text

**Files:**
- Modify: `app/src/main/java/com/avs/sea/battle/ui/GameScreen.kt` (battle `Column`, `Boards`, `ActionArea`)
- Test: `app/src/test/java/com/avs/sea/battle/ui/PortraitBoardsLayoutTest.kt`, `LandscapeBoardsLayoutTest.kt`; `app/src/androidTest/.../ui/GameScreenSpacingTest.kt` (or a new `FleetTrackerPlacementTest.kt`)

**Interfaces:**
- Consumes: `FleetTracker`, `fleetTrackerTag`, `fleetTrackerCell` (Task 3); `personFleet` / `computerFleet` (Task 2).
- Produces:
  - `internal fun portraitBattleBoardsLayout(maxWidth: Dp, maxHeight: Dp): PortraitBoardsLayout` = `portraitBoardsLayout(maxWidth, maxHeight - FLEET_ROW_SLOT * 2)`, with `FLEET_ROW_SLOT = 18.dp` (a 10 dp row plus 8 dp to its board).
  - `internal fun landscapeFleetRowWidth(gridWidth: Dp): Dp` = `gridWidth * 0.65f`.
  - `LANDSCAPE_STRIP_HEIGHT = 44.dp`, which replaces `LANDSCAPE_ACTION_AREA_HEIGHT`.

- [ ] **Step 1: Write the failing tests.**
  - `PortraitBoardsLayoutTest.commonPhonesKeepFullWidthBoardsWithFleetRows`: `portraitBattleBoardsLayout` keeps `boardSize == width * 0.8` for 360×652, 393×747, 411×785 and 412×835 dp. These are today's board areas plus the status line portrait no longer uses.
  - `LandscapeBoardsLayoutTest.fleetRowsLeaveRoomForNewGameBetweenThem`: take the `Boards` areas of a 640×360 and a 914×411 dp landscape phone, 544×289 and 818×340 dp, and compute `landscapeBoardsLayout(width, height - LANDSCAPE_STRIP_HEIGHT)` and its grid (`boardSize - LABEL_SIZE`). The space between the two outer-aligned rows, `LABEL_SIZE + gap + 2 * (grid - landscapeFleetRowWidth(grid))`, is ≥ 140 dp, which is room for New game in en/uk/ru.
  - UI, portrait battle:
    - your tracker's bottom is ≤ the top of your grid's letter row and within 12 dp of it;
    - the opponent tracker's top is ≥ the computer grid's bottom and within 12 dp;
    - both trackers lie within the screen.
  - UI, portrait game over (`OVER` with fleets set):
    - the status text node sits between the person grid's bottom and the computer grid's top;
    - at font scale 1.3 it still does;
    - New game doesn't overlap the opponent tracker.
  - UI, landscape battle:
    - your tracker's left edge equals your grid's left edge (±1 dp) and its top is below your grid's bottom;
    - the opponent tracker's right edge equals the computer grid's right edge.
  - UI, landscape game over on 640×360: New game's bounds don't intersect either tracker.
  - UI: no tracker nodes in `PLACING`.
  - `boardsStayPutWhenTheGameEnds` (both orientations) must keep passing, with fleets set in both states.
- [ ] **Step 2: Run** the unit tests (compile failure expected), then the instrumented tests (the new UI checks fail).
- [ ] **Step 3: Implement.**
  - **Portrait:** the battle `Column` drops the top `StatusText`. `Boards` stacks: your `FleetTracker` (start-padded `LABEL_SIZE`, grid width, bottom-aligned in an `FLEET_ROW_SLOT` box); your board; the gap; the computer's board; the opponent's `FleetTracker` (top-aligned in an `FLEET_ROW_SLOT` box). The gap shows `TurnIndicator` during the battle and, at `OVER`, the centred `StatusText(uiState.statusResId)` in the same box. The outer `ActionArea` box stays below.
  - **Landscape:** the battle `Column` keeps the top `StatusText` and drops the outer `ActionArea` box. `Boards` uses `landscapeBoardsLayout(maxWidth, maxHeight - LANDSCAPE_STRIP_HEIGHT)` and puts a strip `2 * boardSize + gap` wide and `LANDSCAPE_STRIP_HEIGHT` tall under the boards row. In the strip, your tracker is top-start (start-padded `LABEL_SIZE`, width `landscapeFleetRowWidth(grid)`, `Alignment.Start`), the opponent's is top-end (width `landscapeFleetRowWidth(grid)`, `Alignment.End`), and `ActionArea` is centred.
  - Update the comments on the layout functions you change. Rule on any existing spacing test that assumed the old top status line in portrait (`portraitActionButtonSitsCloseBelowComputerBoard` may need its distance re-measured to the opponent tracker).
- [ ] **Step 4: Run** `./gradlew testDebugUnitTest lintDebug` and the full `connectedDebugAndroidTest`. Expected: PASS.
- [ ] **Step 5: Commit** `git commit -m "show both fleets around the boards and move the portrait result into the gap"`

### Task 5: Store screenshots and docs

**Files:**
- Modify: `tools/store-assets/gen_screenshot_test.py`, `CLAUDE.md` (untracked; not committed)

- [ ] **Step 1: Update the generator.**
  - Add `fleet(fleet, hits)`, which returns `(size, sunk)` pairs in `FLEET` order; a ship is sunk when all its cells are hit, and repeated sizes keep the leftmost-first rule from Task 2.
  - Emit `personFleet = listOf(FleetShipUi(…), …)` and `computerFleet = …` for `battle`, `computer_turn`, `win` and `lose`.
- [ ] **Step 2: Verify** as on PR #33:
  - generate the test file, install both APKs, and capture `battle`, `win` and `lose` with `am instrument`;
  - pull the shots and check them: the rows sit at the outer edges with sunk ships struck through, and the portrait result text is in the gap;
  - delete the generated file and uninstall the test APK.
- [ ] **Step 3:** In `CLAUDE.md`, add `FleetTracker`, `fleetShips` and `sunkShipSizes`, and note that the portrait game-over text sits between the boards.
- [ ] **Step 4: Commit** the generator: `git commit -m "add the fleet rows to the store screenshot states"`

### Task 6: Verify, review, PR

- [ ] **Step 1:** `./gradlew testDebugUnitTest lintDebug` and `./gradlew connectedDebugAndroidTest`, all passing.
- [ ] **Step 2:** On the phone, check portrait and landscape in light and dark: rows at the outer edges, a ship struck through when it sinks (on the correct side), the final state at game over, and New game clear of the rows.
- [ ] **Step 3:** Run `/code-review` on `feature/fleet-tracker` against `dev-v2`. Show the user the findings and fix the agreed ones.
- [ ] **Step 4:** Push and open the PR into `dev-v2` with "Closes #30", in the same format as PR #33.
