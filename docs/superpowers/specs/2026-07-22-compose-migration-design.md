# Jetpack Compose Migration — Design

Date: 2026-07-22
Status: approved
Branch: `feature/compose-migration` (stacked on `fix/medium-gameplay-fixes`, PR #10)

## Goal

Migrate the entire UI of SeaBattle from XML + DataBinding + custom Views to Jetpack Compose, pixel-faithful to the current look. Game logic (`battle_field/`, `ShotManager`, turn orchestration) is untouched. This PR lands before the accessibility work so that accessibility semantics can be built on Compose.

## Decisions (agreed with Alina)

- **Scope**: full migration. Both activities become Compose hosts; all XML layouts, DataBinding, and kapt are deleted.
- **State layer**: LiveData is replaced with a single `StateFlow<GameUiState>` plus a `SharedFlow` for one-shot UI events.
- **Visuals**: pixel-faithful recreation — Neucha font, existing black/white day-night colors, same board rendering, same button styling, same portrait/landscape arrangements. No Material redesign.
- **Structure**: single branch with disciplined commits (each compiles, tests green): dependencies/theme → board composable → screen composables → ViewModel StateFlow rewrite (tests first) → delete legacy UI.

## Build changes

- Add the `org.jetbrains.kotlin.plugin.compose` Gradle plugin (Kotlin 2.1.20 bundles the matching compiler).
- Add Compose BOM, `activity-compose`, `material3` (DropdownMenu, ripple), `ui-tooling-preview`; `lifecycle-runtime-compose` for lifecycle-aware collection.
- Remove: `dataBinding`/`viewBinding` build features, `kotlin-kapt` plugin, `com.android.databinding:compiler:3.1.4`, `androidx.lifecycle:lifecycle-extensions:2.2.0`.
- Delete: `layout/` and `layout-land/` activity + letters/numbers layouts, `SquareView`, `PersonSquareView`, `ComputerSquareView`.

## State layer

`MainViewModel` exposes:

```kotlin
data class GameUiState(
    val statusResId: Int,
    val phase: GamePhase,               // PLACING, BATTLE, COMPUTER_TURN, OVER
    val personShips: List<Coordinate>,
    val personCrosses: List<Coordinate>,
    val personDots: List<Coordinate>,
    val computerCrosses: List<Coordinate>,
    val computerDots: List<Coordinate>,
    val computerShips: List<Coordinate>, // populated only at game over
    val selectedCoordinate: Coordinate?,
)
```

- Button visibility, Fire button visibility, and the progress indicator derive from `phase` and `selectedCoordinate` in the UI; no separate visibility streams.
- One-shot events: `SharedFlow<UiEvent>` with `UiEvent.RequestReview`, collected with `repeatOnLifecycle`. Replaces the `showReviewRequest` + `onReviewFlowLaunched` consume pattern.
- Public ViewModel API (`generateShips`, `startGame`, `startNewGame`, `handlePCAreaClick`, `makeFireAsPerson`) and all game logic remain unchanged.

## UI structure (`ui/` package)

- `GameScreen` — collects `GameUiState`; portrait `Column` / landscape `Row` chosen via `LocalConfiguration.orientation`, matching current guideline-based layouts.
- `BoardGrid` — one composable replaces both custom Views. A `Canvas` draws grid lines, ship squares, dots, crosses, and the selected cell exactly as `SquareView` does today (same stroke widths, `greyTransparent` ships, radius-based dots). `pointerInput` tap handling converts the touch to a `Coordinate` and is enabled only for the computer board during `BATTLE`. Row/column labels (`Text`) mirror the current letters/numbers include layouts.
- `GameButton` — bordered `Text` matching `MainButtonStyle` + the pressed-state behavior from PR #10, via `interactionSource`.
- Overflow menu — `IconButton` + `DropdownMenu` reusing existing menu strings; share/rate/email/privacy intent helpers in `Utils.kt` stay as-is.
- `PrivacyPolicyActivity` — Compose host with `AndroidView`-wrapped WebView and a `BackHandler` that navigates WebView history before finishing (fixes an audit finding).
- Theme — `SeaBattleTheme` with Neucha `FontFamily` from `res/font`, day/night colors identical to current (`colorPrimary` background, black/white lines/text, `greySelected`, `greyTransparent`), edge-to-edge via `enableEdgeToEdge` + `safeDrawingPadding()`.

## Testing

- `MainViewModelTest` rewritten first (red → green) against `GameUiState` + events: the five existing behaviors (no-selection guard, turn passing, win → review event exactly once, new-game reset) plus phase transitions.
- Game-logic tests (`BattleFieldTest`, `ShotManagerTest`, etc.) untouched and must stay green.
- Visual parity verified manually against the current app in light/dark, portrait/landscape; checklist included in the PR description.

## Non-goals

- No Material 3 visual redesign, no navigation library, no accessibility semantics beyond what Compose gives for free (dedicated a11y PR follows), no changes to game rules or AI.
