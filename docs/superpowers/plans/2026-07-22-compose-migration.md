# Jetpack Compose Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate SeaBattle's entire UI from XML + DataBinding + custom Views to Jetpack Compose with a single `StateFlow<GameUiState>`, pixel-faithful to the current look.

**Architecture:** Additive first — new `ui/` package (theme, `BoardGrid`, `GameScreen`) compiles alongside the legacy UI. Then one atomic task rewrites `MainViewModel` to StateFlow and swaps `MainActivity` to `setContent`. Finally the legacy Views/XML/DataBinding are deleted. Game logic (`battle_field/`, `ShotManager`) is untouched.

**Tech Stack:** Kotlin 2.1.20 (bundled Compose compiler), Compose BOM 2025.06.01, material3, activity-compose 1.10.1, lifecycle-runtime-compose 2.9.2, kotlinx-coroutines-test + core-testing (already present).

## Global Constraints

- Spec: `docs/superpowers/specs/2026-07-22-compose-migration-design.md`. Branch: `feature/compose-migration`, stacked on `fix/medium-gameplay-fixes` (PR #10).
- Every Gradle command needs: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.
- Run `./gradlew testDebugUnitTest` before every commit; all tests must pass.
- Commits are authored by Alina's git config only — never add Claude/co-author trailers.
- Never stage `.idea/` files or `CLAUDE.md`.
- Pixel parity targets: light background `#FFFAFAFA`, dark `#FF303030`, lines/text black (light) / white (dark), `greySelected #99C1C0C0`, `greyTransparent #99808080`, Neucha font, 10×10 board (`SQUARES_COUNT`).
- Coordinate convention (unchanged from Views): `Coordinate.x` = row (vertical), `Coordinate.y` = column (horizontal). Screen mapping: cell left = `y * cellSize`, cell top = `x * cellSize`.

---

### Task 1: Compose build plumbing

**Files:**
- Modify: `build.gradle` (root, buildscript dependencies block)
- Modify: `app/build.gradle`

**Interfaces:**
- Produces: Compose runtime available to all later tasks; `buildFeatures { compose true }`.

- [ ] **Step 1: Add the Compose compiler Gradle plugin to the root buildscript**

In `build.gradle`, inside `buildscript { dependencies { ... } }`, after the kotlin-gradle-plugin line:

```groovy
        classpath "org.jetbrains.kotlin:compose-compiler-gradle-plugin:$kotlin_version"
```

- [ ] **Step 2: Apply the plugin and enable Compose in `app/build.gradle`**

After `apply plugin: 'kotlin-kapt'` add:

```groovy
apply plugin: 'org.jetbrains.kotlin.plugin.compose'
```

Inside `android { }`, next to the existing `buildFeatures` block, extend it:

```groovy
    buildFeatures {
        viewBinding = true
        compose = true
    }
```

- [ ] **Step 3: Add Compose dependencies**

In `dependencies { }`:

```groovy
    def composeBom = platform('androidx.compose:compose-bom:2025.06.01')
    implementation composeBom
    androidTestImplementation composeBom
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.activity:activity-compose:1.10.1'
    implementation 'androidx.lifecycle:lifecycle-runtime-compose:2.9.2'
    debugImplementation 'androidx.compose.ui:ui-tooling'
    debugImplementation 'androidx.compose.ui:ui-test-manifest'
    androidTestImplementation 'androidx.compose.ui:ui-test-junit4'
```

- [ ] **Step 4: Verify build and tests**

Run: `./gradlew assembleDebug testDebugUnitTest`
Expected: BUILD SUCCESSFUL (no source uses Compose yet).

- [ ] **Step 5: Commit**

```bash
git add build.gradle app/build.gradle
git commit -m "add jetpack compose build setup"
```

---

### Task 2: Theme and GameButton

**Files:**
- Create: `app/src/main/java/com/avs/sea/battle/ui/theme/SeaBattleTheme.kt`
- Create: `app/src/main/java/com/avs/sea/battle/ui/GameButton.kt`

**Interfaces:**
- Produces: `SeaBattleTheme(content: @Composable () -> Unit)`; `SeaBattleColors` accessible via `SeaBattleTheme.colors` (properties `background: Color`, `foreground: Color`, `selected: Color`, `shipFill: Color`); `NeuchaFontFamily: FontFamily`; `GameButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier)`.

- [ ] **Step 1: Create the theme**

`app/src/main/java/com/avs/sea/battle/ui/theme/SeaBattleTheme.kt`:

```kotlin
package com.avs.sea.battle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.avs.sea.battle.R

val NeuchaFontFamily = FontFamily(Font(R.font.neucha))

data class SeaBattleColors(
    val background: Color,
    val foreground: Color,
    val selected: Color,
    val shipFill: Color,
)

private val LightColors = SeaBattleColors(
    background = Color(0xFFFAFAFA),
    foreground = Color.Black,
    selected = Color(0x99C1C0C0),
    shipFill = Color(0x99808080),
)

private val DarkColors = SeaBattleColors(
    background = Color(0xFF303030),
    foreground = Color.White,
    selected = Color(0x99C1C0C0),
    shipFill = Color(0x99808080),
)

private val LocalSeaBattleColors = staticCompositionLocalOf { LightColors }

object SeaBattleTheme {
    val colors: SeaBattleColors
        @Composable get() = LocalSeaBattleColors.current
}

@Composable
fun SeaBattleTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    CompositionLocalProvider(LocalSeaBattleColors provides colors, content = content)
}
```

- [ ] **Step 2: Create GameButton**

`app/src/main/java/com/avs/sea/battle/ui/GameButton.kt`:

```kotlin
package com.avs.sea.battle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avs.sea.battle.ui.theme.NeuchaFontFamily
import com.avs.sea.battle.ui.theme.SeaBattleTheme

@Composable
fun GameButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val colors = SeaBattleTheme.colors
    BasicText(
        text = text,
        style = TextStyle(
            color = colors.foreground,
            fontSize = 20.sp,
            fontFamily = NeuchaFontFamily,
        ),
        modifier = modifier
            .background(if (pressed) colors.selected else colors.background)
            .border(1.dp, colors.foreground)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Preview
@Composable
private fun GameButtonPreview() {
    SeaBattleTheme { GameButton(text = "Generate ships", onClick = {}) }
}
```

- [ ] **Step 3: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/avs/sea/battle/ui
git commit -m "add compose theme and game button"
```

---

### Task 3: BoardGrid composable

**Files:**
- Create: `app/src/main/java/com/avs/sea/battle/ui/BoardGrid.kt`

**Interfaces:**
- Consumes: `SeaBattleTheme.colors`, `NeuchaFontFamily` (Task 2); `Coordinate`, `SQUARES_COUNT` (existing).
- Produces: `LabeledBoard(ships: List<Coordinate>, crosses: List<Coordinate>, dots: List<Coordinate>, selected: Coordinate?, onCellTap: ((Coordinate) -> Unit)?, modifier: Modifier = Modifier)` — full board with letter/number labels; internally uses `BoardGrid` (same params minus labels).

- [ ] **Step 1: Create the board composables**

`app/src/main/java/com/avs/sea/battle/ui/BoardGrid.kt` (exact file content):

```kotlin
package com.avs.sea.battle.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avs.sea.battle.R
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.ui.theme.NeuchaFontFamily
import com.avs.sea.battle.ui.theme.SeaBattleTheme

private val LETTER_IDS = listOf(
    R.string.letter_a, R.string.letter_b, R.string.letter_c, R.string.letter_d,
    R.string.letter_e, R.string.letter_f, R.string.letter_g, R.string.letter_h,
    R.string.letter_i, R.string.letter_j,
)

private val NUMBER_IDS = listOf(
    R.string.number_1, R.string.number_2, R.string.number_3, R.string.number_4,
    R.string.number_5, R.string.number_6, R.string.number_7, R.string.number_8,
    R.string.number_9, R.string.number_10,
)

private val LABEL_SIZE = 20.dp

@Composable
fun LabeledBoard(
    ships: List<Coordinate>,
    crosses: List<Coordinate>,
    dots: List<Coordinate>,
    selected: Coordinate?,
    onCellTap: ((Coordinate) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val labelStyle = TextStyle(
        color = SeaBattleTheme.colors.foreground,
        fontFamily = NeuchaFontFamily,
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
    )
    Column(modifier = modifier) {
        Row(Modifier.fillMaxWidth().padding(start = LABEL_SIZE)) {
            LETTER_IDS.forEach { id ->
                BasicText(
                    text = stringResource(id),
                    style = labelStyle,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.width(LABEL_SIZE).aspectRatio(1f / SQUARES_COUNT)) {
                NUMBER_IDS.forEach { id ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        BasicText(text = stringResource(id), style = labelStyle)
                    }
                }
            }
            BoardGrid(
                ships = ships,
                crosses = crosses,
                dots = dots,
                selected = selected,
                onCellTap = onCellTap,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun BoardGrid(
    ships: List<Coordinate>,
    crosses: List<Coordinate>,
    dots: List<Coordinate>,
    selected: Coordinate?,
    onCellTap: ((Coordinate) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = SeaBattleTheme.colors
    val tapModifier = if (onCellTap != null) {
        Modifier.pointerInput(onCellTap) {
            detectTapGestures { offset ->
                val cell = size.width.toFloat() / SQUARES_COUNT
                val row = (offset.y / cell).toInt().coerceIn(0, SQUARES_COUNT - 1)
                val column = (offset.x / cell).toInt().coerceIn(0, SQUARES_COUNT - 1)
                onCellTap(Coordinate(row, column))
            }
        }
    } else {
        Modifier
    }
    Canvas(modifier = modifier.aspectRatio(1f).then(tapModifier)) {
        val cell = size.width / SQUARES_COUNT
        val stroke = 1f
        ships.forEach { ship ->
            drawRect(
                color = colors.shipFill,
                topLeft = Offset(ship.y * cell, ship.x * cell),
                size = Size(cell, cell),
            )
        }
        selected?.let { sel ->
            drawRect(
                color = colors.selected,
                topLeft = Offset(sel.y * cell, sel.x * cell),
                size = Size(cell, cell),
            )
        }
        for (i in 0..SQUARES_COUNT) {
            drawLine(colors.foreground, Offset(0f, i * cell), Offset(size.width, i * cell), stroke)
            drawLine(colors.foreground, Offset(i * cell, 0f), Offset(i * cell, size.height), stroke)
        }
        dots.forEach { dot ->
            drawCircle(
                color = colors.foreground,
                radius = 4.dp.toPx(),
                center = Offset(dot.y * cell + cell / 2, dot.x * cell + cell / 2),
            )
        }
        crosses.forEach { cross ->
            val left = cross.y * cell
            val top = cross.x * cell
            drawLine(colors.foreground, Offset(left, top), Offset(left + cell, top + cell), stroke * 2.5f)
            drawLine(colors.foreground, Offset(left + cell, top), Offset(left, top + cell), stroke * 2.5f)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LabeledBoardPreview() {
    SeaBattleTheme {
        LabeledBoard(
            ships = listOf(Coordinate(0, 0), Coordinate(0, 1), Coordinate(5, 5)),
            crosses = listOf(Coordinate(3, 3)),
            dots = listOf(Coordinate(7, 7)),
            selected = Coordinate(9, 9),
            onCellTap = null,
        )
    }
}
```

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/avs/sea/battle/ui/BoardGrid.kt
git commit -m "add compose board grid with labels"
```

---

### Task 4: Stateless GameScreen with menu

**Files:**
- Create: `app/src/main/java/com/avs/sea/battle/ui/GameUiState.kt`
- Create: `app/src/main/java/com/avs/sea/battle/ui/GameScreen.kt`

**Interfaces:**
- Consumes: `LabeledBoard`, `GameButton`, theme (Tasks 2–3).
- Produces:
  - `enum class GamePhase { PLACING, BATTLE, COMPUTER_TURN, OVER }`
  - `data class GameUiState(statusResId: Int = R.string.status_welcome_text, phase: GamePhase = GamePhase.PLACING, personBoardShips/personBoardCrosses/personBoardDots/computerBoardShips/computerBoardCrosses/computerBoardDots: List<Coordinate> = emptyList(), selectedCoordinate: Coordinate? = null, winner: Player? = null)`
  - `sealed interface UiEvent { data object RequestReview : UiEvent }`
  - `enum class MenuAction { SHARE, RATE, MORE_APPS, WRITE_TO_AUTHOR, PRIVACY_POLICY }`
  - `GameScreen(uiState: GameUiState, onGenerateShips: () -> Unit, onStartGame: () -> Unit, onNewGame: () -> Unit, onCellClick: (Coordinate) -> Unit, onFire: () -> Unit, onMenuAction: (MenuAction) -> Unit)`

- [ ] **Step 1: Create the UI state types**

`app/src/main/java/com/avs/sea/battle/ui/GameUiState.kt`:

```kotlin
package com.avs.sea.battle.ui

import com.avs.sea.battle.R
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.main.Player

enum class GamePhase { PLACING, BATTLE, COMPUTER_TURN, OVER }

data class GameUiState(
    val statusResId: Int = R.string.status_welcome_text,
    val phase: GamePhase = GamePhase.PLACING,
    val personBoardShips: List<Coordinate> = emptyList(),
    val personBoardCrosses: List<Coordinate> = emptyList(),
    val personBoardDots: List<Coordinate> = emptyList(),
    val computerBoardShips: List<Coordinate> = emptyList(),
    val computerBoardCrosses: List<Coordinate> = emptyList(),
    val computerBoardDots: List<Coordinate> = emptyList(),
    val selectedCoordinate: Coordinate? = null,
    val winner: Player? = null,
)

sealed interface UiEvent {
    data object RequestReview : UiEvent
}

enum class MenuAction { SHARE, RATE, MORE_APPS, WRITE_TO_AUTHOR, PRIVACY_POLICY }
```

- [ ] **Step 2: Create GameScreen**

`app/src/main/java/com/avs/sea/battle/ui/GameScreen.kt`:

```kotlin
package com.avs.sea.battle.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avs.sea.battle.R
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.ui.theme.NeuchaFontFamily
import com.avs.sea.battle.ui.theme.SeaBattleTheme

@Composable
fun GameScreen(
    uiState: GameUiState,
    onGenerateShips: () -> Unit,
    onStartGame: () -> Unit,
    onNewGame: () -> Unit,
    onCellClick: (Coordinate) -> Unit,
    onFire: () -> Unit,
    onMenuAction: (MenuAction) -> Unit,
) {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Box(
        Modifier
            .fillMaxSize()
            .background(SeaBattleTheme.colors.background)
            .safeDrawingPadding()
    ) {
        if (isLandscape) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PersonBoard(uiState, Modifier.weight(1f).padding(8.dp).align(Alignment.CenterVertically))
                Column(
                    Modifier.weight(0.8f).fillMaxHeight().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    StatusText(uiState.statusResId, Modifier.weight(1f))
                    ActionArea(uiState, onGenerateShips, onStartGame, onNewGame, onFire)
                }
                ComputerBoard(uiState, onCellClick, Modifier.weight(1f).padding(8.dp).align(Alignment.CenterVertically))
            }
        } else {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                StatusText(uiState.statusResId, Modifier.padding(top = 10.dp, start = 40.dp, end = 40.dp))
                PersonBoard(uiState, Modifier.fillMaxWidth(0.75f).padding(top = 8.dp))
                ComputerBoard(uiState, onCellClick, Modifier.fillMaxWidth(0.75f).padding(top = 16.dp))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ActionArea(uiState, onGenerateShips, onStartGame, onNewGame, onFire)
                }
            }
        }
        MoreMenu(onMenuAction, Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun StatusText(statusResId: Int, modifier: Modifier = Modifier) {
    BasicText(
        text = stringResource(statusResId),
        style = TextStyle(
            color = SeaBattleTheme.colors.foreground,
            fontFamily = NeuchaFontFamily,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
        ),
        modifier = modifier,
    )
}

@Composable
private fun PersonBoard(uiState: GameUiState, modifier: Modifier = Modifier) {
    LabeledBoard(
        ships = uiState.personBoardShips,
        crosses = uiState.personBoardCrosses,
        dots = uiState.personBoardDots,
        selected = null,
        onCellTap = null,
        modifier = modifier,
    )
}

@Composable
private fun ComputerBoard(
    uiState: GameUiState,
    onCellClick: (Coordinate) -> Unit,
    modifier: Modifier = Modifier,
) {
    LabeledBoard(
        ships = uiState.computerBoardShips,
        crosses = uiState.computerBoardCrosses,
        dots = uiState.computerBoardDots,
        selected = uiState.selectedCoordinate,
        onCellTap = if (uiState.phase == GamePhase.BATTLE) onCellClick else null,
        modifier = modifier,
    )
}

@Composable
private fun ActionArea(
    uiState: GameUiState,
    onGenerateShips: () -> Unit,
    onStartGame: () -> Unit,
    onNewGame: () -> Unit,
    onFire: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        when (uiState.phase) {
            GamePhase.PLACING -> {
                GameButton(stringResource(R.string.generate_ships_text), onGenerateShips)
                if (uiState.personBoardShips.isNotEmpty()) {
                    GameButton(stringResource(R.string.start_text), onStartGame)
                }
            }
            GamePhase.BATTLE -> {
                if (uiState.selectedCoordinate != null) {
                    GameButton(stringResource(R.string.fire_text), onFire)
                }
            }
            GamePhase.COMPUTER_TURN -> CircularProgressIndicator(
                color = SeaBattleTheme.colors.foreground,
                modifier = Modifier.padding(4.dp),
            )
            GamePhase.OVER -> GameButton(stringResource(R.string.new_game_text), onNewGame)
        }
    }
}

@Composable
private fun MoreMenu(onMenuAction: (MenuAction) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(R.drawable.ic_more),
                contentDescription = stringResource(R.string.more_settings),
                tint = SeaBattleTheme.colors.foreground,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MenuEntry(R.string.menu_share_text, MenuAction.SHARE, onMenuAction) { expanded = false }
            MenuEntry(R.string.menu_rate_on_play_market_text, MenuAction.RATE, onMenuAction) { expanded = false }
            MenuEntry(R.string.menu_more_apps_text, MenuAction.MORE_APPS, onMenuAction) { expanded = false }
            MenuEntry(R.string.menu_write_to_author_text, MenuAction.WRITE_TO_AUTHOR, onMenuAction) { expanded = false }
            MenuEntry(R.string.menu_privacy_policy_text, MenuAction.PRIVACY_POLICY, onMenuAction) { expanded = false }
        }
    }
}

@Composable
private fun MenuEntry(
    textResId: Int,
    action: MenuAction,
    onMenuAction: (MenuAction) -> Unit,
    dismiss: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(textResId), fontFamily = NeuchaFontFamily, fontSize = 16.sp) },
        onClick = {
            dismiss()
            onMenuAction(action)
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun GameScreenPreview() {
    SeaBattleTheme {
        GameScreen(
            uiState = GameUiState(personBoardShips = listOf(Coordinate(1, 1))),
            onGenerateShips = {}, onStartGame = {}, onNewGame = {},
            onCellClick = {}, onFire = {}, onMenuAction = {},
        )
    }
}
```

- [ ] **Step 3: Verify build and existing tests**

Run: `./gradlew assembleDebug testDebugUnitTest`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/avs/sea/battle/ui/GameUiState.kt app/src/main/java/com/avs/sea/battle/ui/GameScreen.kt
git commit -m "add stateless compose game screen"
```

---

### Task 5: ViewModel StateFlow rewrite + MainActivity Compose host (TDD)

**Files:**
- Modify: `app/src/test/java/com/avs/sea/battle/main/MainViewModelTest.kt` (full rewrite)
- Modify: `app/src/main/java/com/avs/sea/battle/main/MainViewModel.kt` (full rewrite)
- Modify: `app/src/main/java/com/avs/sea/battle/main/MainActivity.kt` (full rewrite)

**Interfaces:**
- Consumes: `GameUiState`, `GamePhase`, `UiEvent`, `MenuAction`, `GameScreen` (Task 4).
- Produces: `MainViewModel.uiState: StateFlow<GameUiState>`, `MainViewModel.events: SharedFlow<UiEvent>`; same public actions as before: `generateShips()`, `startGame()`, `startNewGame()`, `handlePCAreaClick(Coordinate)`, `makeFireAsPerson()`; `@VisibleForTesting getComputerBattleField(): BattleField`.

- [ ] **Step 1: Rewrite MainViewModelTest against the new API (failing first)**

Replace the whole file `app/src/test/java/com/avs/sea/battle/main/MainViewModelTest.kt`:

```kotlin
package com.avs.sea.battle.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.avs.sea.battle.R
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.ui.GamePhase
import com.avs.sea.battle.ui.UiEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MainViewModel
    private val events = mutableListOf<UiEvent>()
    private lateinit var eventsJob: Job

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = MainViewModel()
        events.clear()
        eventsJob = CoroutineScope(UnconfinedTestDispatcher()).launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.generateShips()
        viewModel.startGame()
    }

    @After
    fun tearDown() {
        eventsJob.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun startedGameIsInBattlePhaseWithShipsPlaced() {
        assertEquals(GamePhase.BATTLE, viewModel.uiState.value.phase)
        assertEquals(20, viewModel.uiState.value.personBoardShips.size)
        assertEquals(R.string.status_select_to_fire_text, viewModel.uiState.value.statusResId)
    }

    @Test
    fun makeFireAsPersonWithoutSelectionKeepsPersonTurn() {
        viewModel.makeFireAsPerson()
        assertEquals(GamePhase.BATTLE, viewModel.uiState.value.phase)
        assertEquals(R.string.status_select_to_fire_text, viewModel.uiState.value.statusResId)
    }

    @Test
    fun makeFireAsPersonAtEmptyCellPassesTurnToComputer() {
        viewModel.handlePCAreaClick(findEmptyCell())
        viewModel.makeFireAsPerson()
        assertEquals(GamePhase.COMPUTER_TURN, viewModel.uiState.value.phase)
        assertEquals(R.string.status_opponent_shot_text, viewModel.uiState.value.statusResId)
        assertNull(viewModel.uiState.value.selectedCoordinate)
    }

    @Test
    fun winningGameEndsGameAndRevealsComputerShips() {
        winGame()
        assertEquals(GamePhase.OVER, viewModel.uiState.value.phase)
        assertEquals(Player.PERSON, viewModel.uiState.value.winner)
        assertEquals(20, viewModel.uiState.value.computerBoardShips.size)
        assertEquals(R.string.status_game_over_you_win_text, viewModel.uiState.value.statusResId)
    }

    @Test
    fun winningGameEmitsSingleReviewRequest() {
        winGame()
        assertEquals(listOf<UiEvent>(UiEvent.RequestReview), events)
    }

    @Test
    fun startingNewGameResetsState() {
        winGame()
        viewModel.startNewGame()
        assertEquals(GamePhase.PLACING, viewModel.uiState.value.phase)
        assertEquals(emptyList<Coordinate>(), viewModel.uiState.value.personBoardShips)
        assertEquals(emptyList<Coordinate>(), viewModel.uiState.value.computerBoardCrosses)
        assertNull(viewModel.uiState.value.winner)
    }

    private fun findEmptyCell(): Coordinate {
        val ships = viewModel.getComputerBattleField().getShipsCoordinates()
        for (i in 0 until SQUARES_COUNT) {
            for (j in 0 until SQUARES_COUNT) {
                if (ships.none { it.x == i && it.y == j }) {
                    return Coordinate(i, j)
                }
            }
        }
        throw IllegalStateException("Battle field has no empty cells")
    }

    private fun winGame() {
        val computerField = viewModel.getComputerBattleField()
        while (!computerField.isGameOver()) {
            viewModel.handlePCAreaClick(computerField.getShipsCoordinates().first())
            viewModel.makeFireAsPerson()
        }
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew testDebugUnitTest --tests "com.avs.sea.battle.main.MainViewModelTest"`
Expected: FAIL — compilation errors (`uiState`, `events` unresolved on `MainViewModel`).

- [ ] **Step 3: Rewrite MainViewModel**

Replace the whole file `app/src/main/java/com/avs/sea/battle/main/MainViewModel.kt`:

```kotlin
package com.avs.sea.battle.main

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avs.sea.battle.R
import com.avs.sea.battle.SECOND_IN_MILLIS
import com.avs.sea.battle.battle_field.BattleField
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.ui.GamePhase
import com.avs.sea.battle.ui.GameUiState
import com.avs.sea.battle.ui.UiEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private var activePlayer: Player = Player.NONE
    private lateinit var personBattleField: BattleField
    private lateinit var computerBattleField: BattleField
    private lateinit var shotManager: ShotManager

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    init {
        initValues()
    }

    private fun initValues() {
        activePlayer = Player.NONE
        shotManager = ShotManager()
        personBattleField = BattleField()
        computerBattleField = BattleField()
        computerBattleField.randomizeShips()
        _uiState.value = GameUiState()
    }

    fun generateShips() {
        personBattleField.initBattleShip()
        personBattleField.randomizeShips()
        _uiState.update {
            it.copy(
                personBoardShips = personBattleField.getShipsCoordinates(),
                statusResId = R.string.status_generate_or_start_text,
            )
        }
    }

    fun startGame() {
        activePlayer = Player.PERSON
        _uiState.update {
            it.copy(phase = GamePhase.BATTLE, statusResId = R.string.status_select_to_fire_text)
        }
    }

    fun startNewGame() {
        initValues()
    }

    fun handlePCAreaClick(coordinate: Coordinate) {
        if (activePlayer == Player.PERSON && computerBattleField.isCellFreeToBeSelected(coordinate)) {
            _uiState.update { it.copy(selectedCoordinate = coordinate) }
        }
    }

    fun makeFireAsPerson() {
        val target = _uiState.value.selectedCoordinate
        if (activePlayer != Player.PERSON || target == null) return
        val shipState = computerBattleField.handleShot(target)
        if (shipState.first) {
            _uiState.update {
                it.copy(
                    selectedCoordinate = null,
                    computerBoardCrosses = computerBattleField.getCrossesCoordinates(),
                    computerBoardDots = if (shipState.second.isNotEmpty()) {
                        computerBattleField.getDotsCoordinates()
                    } else {
                        it.computerBoardDots
                    },
                    statusResId = R.string.status_shot_ship_again_text,
                )
            }
            if (computerBattleField.isGameOver()) {
                endGame(isPersonWon = true)
            }
        } else {
            activePlayer = Player.COMPUTER
            _uiState.update {
                it.copy(
                    selectedCoordinate = null,
                    computerBoardDots = computerBattleField.getDotsCoordinates(),
                    statusResId = R.string.status_opponent_shot_text,
                    phase = GamePhase.COMPUTER_TURN,
                )
            }
            playAsComputer()
        }
    }

    private fun playAsComputer() {
        val coordinate = shotManager.getCoordinateToShot()
        val shipState = personBattleField.handleShot(coordinate)
        shotManager.handleShot(shipState)
        viewModelScope.launch {
            delay(SECOND_IN_MILLIS)
            if (shipState.first) {
                _uiState.update {
                    it.copy(
                        personBoardCrosses = personBattleField.getCrossesCoordinates(),
                        personBoardDots = if (shipState.second.isNotEmpty()) {
                            personBattleField.getDotsCoordinates()
                        } else {
                            it.personBoardDots
                        },
                    )
                }
                if (personBattleField.isGameOver()) {
                    endGame(isPersonWon = false)
                } else {
                    _uiState.update { it.copy(statusResId = R.string.status_opponent_shot_again_text) }
                    playAsComputer()
                }
            } else {
                activePlayer = Player.PERSON
                _uiState.update {
                    it.copy(
                        personBoardDots = personBattleField.getDotsCoordinates(),
                        phase = GamePhase.BATTLE,
                        statusResId = R.string.status_select_to_fire_text,
                    )
                }
            }
        }
    }

    private fun endGame(isPersonWon: Boolean) {
        activePlayer = Player.NONE
        _uiState.update {
            it.copy(
                phase = GamePhase.OVER,
                winner = if (isPersonWon) Player.PERSON else Player.COMPUTER,
                computerBoardShips = computerBattleField.getShipsCoordinates(),
                statusResId = if (isPersonWon) {
                    R.string.status_game_over_you_win_text
                } else {
                    R.string.status_game_over_you_lose_text
                },
            )
        }
        if (isPersonWon) {
            _events.tryEmit(UiEvent.RequestReview)
        }
    }

    @VisibleForTesting
    fun getComputerBattleField(): BattleField = computerBattleField
}
```

- [ ] **Step 4: Rewrite MainActivity as a Compose host**

Replace the whole file `app/src/main/java/com/avs/sea/battle/main/MainActivity.kt`:

```kotlin
package com.avs.sea.battle.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.avs.sea.battle.R
import com.avs.sea.battle.RECIPIENTS
import com.avs.sea.battle.getShareIntent
import com.avs.sea.battle.openGmail
import com.avs.sea.battle.openMarket
import com.avs.sea.battle.privacy_policy.PrivacyPolicyActivity
import com.avs.sea.battle.ui.GameScreen
import com.avs.sea.battle.ui.MenuAction
import com.avs.sea.battle.ui.UiEvent
import com.avs.sea.battle.ui.theme.SeaBattleTheme
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        UiEvent.RequestReview -> launchReviewFlow()
                    }
                }
            }
        }
        setContent {
            SeaBattleTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                GameScreen(
                    uiState = uiState,
                    onGenerateShips = viewModel::generateShips,
                    onStartGame = viewModel::startGame,
                    onNewGame = viewModel::startNewGame,
                    onCellClick = viewModel::handlePCAreaClick,
                    onFire = viewModel::makeFireAsPerson,
                    onMenuAction = ::handleMenuAction,
                )
            }
        }
    }

    private fun handleMenuAction(action: MenuAction) {
        when (action) {
            MenuAction.SHARE -> startActivity(
                Intent.createChooser(getShareIntent(this), getString(R.string.share_text))
            )
            MenuAction.RATE -> openActivity(
                { startActivity(openMarket(false)) }, R.string.cannot_open_market_error_text
            )
            MenuAction.MORE_APPS -> openActivity(
                { startActivity(openMarket(true)) }, R.string.cannot_open_market_error_text
            )
            MenuAction.WRITE_TO_AUTHOR -> openActivity(
                { startActivity(openGmail(RECIPIENTS, getString(R.string.app_name))) },
                R.string.cannot_send_email_error_text
            )
            MenuAction.PRIVACY_POLICY -> startActivity(
                Intent(this, PrivacyPolicyActivity::class.java)
            )
        }
    }

    private fun openActivity(call: () -> Unit, messageId: Int) {
        try {
            call()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(messageId), Toast.LENGTH_LONG).show()
        }
    }

    private fun launchReviewFlow() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener {
            try {
                val flow = manager.launchReviewFlow(this, it.result)
                flow.addOnCompleteListener {
                    Log.d("Review", "Review flow completed")
                }
            } catch (e: Exception) {
                Log.e("Review", "Error: ${e.message}")
            }
        }
    }
}
```

- [ ] **Step 5: Run the ViewModel tests until green**

Run: `./gradlew testDebugUnitTest --tests "com.avs.sea.battle.main.MainViewModelTest"`
Expected: PASS (7 tests).

- [ ] **Step 6: Run the full suite**

Run: `./gradlew testDebugUnitTest`
Expected: BUILD SUCCESSFUL, all classes green.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/avs/sea/battle/main/MainViewModel.kt \
        app/src/main/java/com/avs/sea/battle/main/MainActivity.kt \
        app/src/test/java/com/avs/sea/battle/main/MainViewModelTest.kt
git commit -m "migrate game screen to compose with stateflow ui state"
```

---

### Task 6: PrivacyPolicyActivity in Compose with WebView back handling

**Files:**
- Modify: `app/src/main/java/com/avs/sea/battle/privacy_policy/PrivacyPolicyActivity.kt` (full rewrite)
- Delete: `app/src/main/res/layout/activity_privacy_policy.xml`

- [ ] **Step 1: Rewrite the activity**

Replace the whole file:

```kotlin
package com.avs.sea.battle.privacy_policy

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.avs.sea.battle.PRIVACY_POLICY_URL
import com.avs.sea.battle.ui.theme.SeaBattleTheme

class PrivacyPolicyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeaBattleTheme {
                var webView by remember { mutableStateOf<WebView?>(null) }
                var canGoBack by remember { mutableStateOf(false) }
                BackHandler(enabled = canGoBack) { webView?.goBack() }
                AndroidView(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = object : WebViewClient() {
                                override fun doUpdateVisitedHistory(
                                    view: WebView?, url: String?, isReload: Boolean
                                ) {
                                    canGoBack = view?.canGoBack() == true
                                }
                            }
                            loadUrl(PRIVACY_POLICY_URL)
                            webView = this
                        }
                    },
                )
            }
        }
    }
}
```

- [ ] **Step 2: Delete the XML layout**

```bash
git rm app/src/main/res/layout/activity_privacy_policy.xml
```

- [ ] **Step 3: Verify build and tests**

Run: `./gradlew assembleDebug testDebugUnitTest`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/avs/sea/battle/privacy_policy/PrivacyPolicyActivity.kt
git commit -m "migrate privacy policy screen to compose with webview back handling"
```

---

### Task 7: Delete legacy UI and DataBinding

**Files:**
- Delete: `app/src/main/java/com/avs/sea/battle/views/` (all 3 files)
- Delete: `app/src/main/res/layout/` and `app/src/main/res/layout-land/` (activity_main + 8 letters/numbers layouts)
- Delete: `app/src/main/res/drawable/button_background.xml`, `app/src/main/res/drawable/square_background.xml`
- Modify: `app/build.gradle` (remove kapt/DataBinding/lifecycle-extensions)
- Modify: `app/src/main/res/values/styles.xml` (remove now-unused styles)
- Modify: `app/src/androidTest/java/com/avs/sea/battle/main/MainActivityStartTest.kt` (rewrite as Compose test)

- [ ] **Step 1: Delete legacy source and resources**

```bash
git rm -r app/src/main/java/com/avs/sea/battle/views
git rm app/src/main/res/layout/activity_main.xml app/src/main/res/layout-land/activity_main.xml
git rm app/src/main/res/layout/layout_letters_pc.xml app/src/main/res/layout/layout_letters_person.xml
git rm app/src/main/res/layout/layout_numbers_pc.xml app/src/main/res/layout/layout_numbers_person.xml
git rm app/src/main/res/layout-land/layout_letters_pc.xml app/src/main/res/layout-land/layout_letters_person.xml
git rm app/src/main/res/layout-land/layout_numbers_pc.xml app/src/main/res/layout-land/layout_numbers_person.xml
git rm app/src/main/res/drawable/button_background.xml app/src/main/res/drawable/square_background.xml
```

- [ ] **Step 2: Remove DataBinding/kapt from `app/build.gradle`**

Remove these lines:

```groovy
apply plugin: 'kotlin-kapt'
```

```groovy
    dataBinding {
        enabled = true
    }
```

and from `buildFeatures` remove `viewBinding = true` (keep `compose = true`), and from dependencies remove:

```groovy
    kapt "com.android.databinding:compiler:3.1.4"
    implementation 'androidx.lifecycle:lifecycle-extensions:2.2.0'
```

- [ ] **Step 3: Trim `styles.xml`**

Remove `SingleDigitStyle`, `SingleDigitStylePortrait`, `SingleCharacterStyle`, `SingleCharacterStylePortrait`, `MainButtonStyle`, `MainButtonStylePortrait`, `FireButtonStyle`, `FireButtonStylePortrait`, and `PopupStyle`. Keep `AppTheme` (still referenced by the manifest).

- [ ] **Step 4: Rewrite the instrumented smoke test for Compose**

Replace `app/src/androidTest/java/com/avs/sea/battle/main/MainActivityStartTest.kt`:

```kotlin
package com.avs.sea.battle.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.avs.sea.battle.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class MainActivityStartTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivityStartTest() {
        val activity = composeTestRule.activity
        composeTestRule.onNodeWithText(activity.getString(R.string.status_welcome_text))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.generate_ships_text))
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.more_settings))
            .assertIsDisplayed()
    }
}
```

- [ ] **Step 5: Verify build, tests, and lint**

Run: `./gradlew testDebugUnitTest lintDebug assembleDebug`
Expected: BUILD SUCCESSFUL. If lint flags unused resources (letters/numbers strings are still used by Compose; `blueAccent`, `colorPrimaryDark` may be unused), leave string resources alone and remove only colors lint proves unused.

- [ ] **Step 6: Commit**

```bash
git add -A app/src/main/res app/build.gradle app/src/androidTest
git commit -m "remove legacy view ui and databinding"
```

---

### Task 8: Verification, manual parity check, push, PR

- [ ] **Step 1: Full local gate**

Run: `./gradlew testDebugUnitTest lintDebug assembleDebug`
Expected: BUILD SUCCESSFUL, all green.

- [ ] **Step 2: Manual visual parity check (requires device/emulator — flag for Alina if unavailable)**

Checklist for the PR description: portrait + landscape in light + dark; board grid/dots/crosses/ship squares match; status texts; Generate→Start→Fire→New game flow; computer-turn progress indicator; overflow menu items all launch; privacy policy opens and back navigates page history; edge-to-edge insets. If the dark background (`0xFF303030`) or dot radius (4.dp) looks off next to the current app, adjust the constants in `SeaBattleTheme.kt` / `BoardGrid.kt`.

- [ ] **Step 3: Push and create PR**

```bash
git push -u origin feature/compose-migration
```

Create the PR via the GitHub API (token from `git credential fill`, never printed), base `fix/medium-gameplay-fixes`, title "Migrate UI to Jetpack Compose", body summarizing: full migration, StateFlow UiState, pixel-faithful, WebView back handling, legacy/DataBinding removal, test coverage, manual parity checklist, and the stacked-PR merge order (#9 → #10 → this).
