package com.avs.sea.battle.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avs.sea.battle.R
import kotlin.math.roundToInt
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.ui.theme.NeuchaFontFamily
import com.avs.sea.battle.ui.theme.SeaBattleTheme

private val MENU_BUTTON_SIZE = 48.dp
private const val LANDSCAPE_GAP_SQUARES = 2
private val ACTION_AREA_HEIGHT = 56.dp
private val LANDSCAPE_ACTION_AREA_HEIGHT = 44.dp

// Kept clear above and below the hint and the stacked buttons while placing in portrait.
private val PLACEMENT_CONTROLS_MARGIN = 16.dp
private val PLACEMENT_BUTTON_MIN_WIDTH = 180.dp
private val PORTRAIT_HINT_SIZE = 21.sp

// The grid itself spans about 75% of the width, as in the original layout; the rest is labels.
private const val PORTRAIT_BOARD_WIDTH_FRACTION = 0.8f

private fun DrawScope.drawNotebookSquares(playerGrid: Rect, screenOrigin: Offset, color: Color) {
    val cell = playerGrid.width / SQUARES_COUNT
    val origin = playerGrid.topLeft - screenOrigin
    pageLinePositions(origin.x, cell, size.width).forEach { x ->
        drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
    }
    pageLinePositions(origin.y, cell, size.height).forEach { y ->
        drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
    }
}

@Composable
fun GameScreen(
    uiState: GameUiState,
    onGenerateShips: () -> Unit,
    onStartGame: () -> Unit,
    onNewGame: () -> Unit,
    onComputerCellClick: (Coordinate) -> Unit,
    onMenuAction: (MenuAction) -> Unit,
    onPersonCellClick: (Coordinate) -> Unit,
    onConfirmNewGame: () -> Unit,
    onDismissNewGame: () -> Unit,
) {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val colors = SeaBattleTheme.colors
    // The notebook squares run through the player's grid, one square per board cell.
    var screenOrigin by remember { mutableStateOf(Offset.Zero) }
    var playerGrid by remember { mutableStateOf<Rect?>(null) }
    val onGridPlaced: (Rect) -> Unit = { playerGrid = it }
    Box(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .onGloballyPositioned { screenOrigin = it.positionInRoot() }
            .drawBehind { playerGrid?.let { drawNotebookSquares(it, screenOrigin, colors.pageLine) } }
            .safeDrawingPadding()
    ) {
        if (uiState.phase == GamePhase.PLACING) {
            val buttons = @Composable { PlacementButtons(onGenerateShips, onStartGame) }
            if (isLandscape) {
                LandscapePlacement(uiState, onPersonCellClick, onGridPlaced, buttons)
            } else {
                PortraitPlacement(uiState, onPersonCellClick, onGridPlaced, buttons)
            }
        } else {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                // Landscape is short on height, so it gets tighter vertical spacing.
                StatusText(
                    uiState.statusResId,
                    Modifier.padding(
                        top = if (isLandscape) 2.dp else 10.dp,
                        start = MENU_BUTTON_SIZE,
                        end = MENU_BUTTON_SIZE,
                    )
                )
                Boards(
                    uiState,
                    isLandscape,
                    onComputerCellClick,
                    onGridPlaced,
                    Modifier.weight(1f).fillMaxWidth(),
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = if (isLandscape) LANDSCAPE_ACTION_AREA_HEIGHT else ACTION_AREA_HEIGHT),
                    contentAlignment = Alignment.Center,
                ) {
                    ActionArea(uiState, onNewGame)
                }
            }
        }
        MoreMenu(onMenuAction, Modifier.align(Alignment.TopEnd))
    }
    if (uiState.showNewGameConfirmation) {
        NewGameDialog(onConfirm = onConfirmNewGame, onDismiss = onDismissNewGame)
    }
}

// The board stays centred on the page, with the hint centred in the space above it and the
// buttons in the space below it. The hint and buttons are measured first, so a long translation
// or a large font shrinks the board instead of running into it.
@Composable
private fun PortraitPlacement(
    uiState: GameUiState,
    onPersonCellClick: (Coordinate) -> Unit,
    onGridPlaced: (Rect) -> Unit,
    buttons: @Composable () -> Unit,
) {
    Layout(
        contents = listOf(
            { StatusText(uiState.statusResId, Modifier.padding(horizontal = MENU_BUTTON_SIZE), PORTRAIT_HINT_SIZE) },
            { PersonBoard(uiState, BoardStyle.Portrait, onGridPlaced = onGridPlaced, onCellClick = onPersonCellClick) },
            buttons,
        ),
        modifier = Modifier.fillMaxSize(),
    ) { (hintMeasurables, boardMeasurables, buttonsMeasurables), constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val hint = hintMeasurables.single().measure(loose)
        val buttonStack = buttonsMeasurables.single().measure(loose)
        val boardSide = portraitPlacementBoardSize(
            maxWidth = constraints.maxWidth.toDp(),
            maxHeight = constraints.maxHeight.toDp(),
            controlsHeight = maxOf(hint.height, buttonStack.height).toDp(),
        ).roundToPx()
        val board = boardMeasurables.single().measure(Constraints.fixed(boardSide, boardSide))
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val boardTop = (height - boardSide) / 2
        val boardBottom = boardTop + boardSide
        layout(width, height) {
            hint.place((width - hint.width) / 2, (boardTop - hint.height) / 2)
            board.place((width - boardSide) / 2, boardTop)
            buttonStack.place((width - buttonStack.width) / 2, boardBottom + (height - boardBottom - buttonStack.height) / 2)
        }
    }
}

// Landscape is short on height, so the board takes the left half and the buttons the right.
@Composable
private fun LandscapePlacement(
    uiState: GameUiState,
    onPersonCellClick: (Coordinate) -> Unit,
    onGridPlaced: (Rect) -> Unit,
    buttons: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        StatusText(uiState.statusResId, Modifier.padding(top = 2.dp, start = MENU_BUTTON_SIZE, end = MENU_BUTTON_SIZE))
        Row(Modifier.weight(1f).fillMaxWidth().padding(horizontal = MENU_BUTTON_SIZE, vertical = 4.dp)) {
            BoxWithConstraints(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                PersonBoard(
                    uiState,
                    BoardStyle.Landscape,
                    Modifier.size(placementBoardSize(maxWidth, maxHeight)),
                    onGridPlaced,
                    onPersonCellClick,
                )
            }
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) { buttons() }
        }
    }
}

@Composable
private fun PlacementButtons(onGenerateShips: () -> Unit, onStartGame: () -> Unit) {
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .width(IntrinsicSize.Max)
            .widthIn(min = PLACEMENT_BUTTON_MIN_WIDTH),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val fullWidth = Modifier.fillMaxWidth()
        GameButton(stringResource(R.string.shuffle_ships_text), onGenerateShips, fullWidth, 22.sp, 6.dp)
        GameButton(stringResource(R.string.start_text), onStartGame, fullWidth, 22.sp, 6.dp)
    }
}

@Composable
private fun Boards(
    uiState: GameUiState,
    isLandscape: Boolean,
    onComputerCellClick: (Coordinate) -> Unit,
    onGridPlaced: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = if (isLandscape) BoardStyle.Landscape else BoardStyle.Portrait
    // Horizontal padding keeps the boards clear of the overflow menu in landscape.
    val padding = if (isLandscape) Modifier.padding(horizontal = MENU_BUTTON_SIZE, vertical = 4.dp) else Modifier
    BoxWithConstraints(modifier.then(padding), contentAlignment = Alignment.Center) {
        when {
            isLandscape -> {
                val layout = landscapeBoardsLayout(maxWidth, maxHeight)
                Row(horizontalArrangement = Arrangement.spacedBy(layout.gap)) {
                    PersonBoard(uiState, style, Modifier.size(layout.boardSize), onGridPlaced)
                    ComputerBoard(uiState, onComputerCellClick, style, Modifier.size(layout.boardSize))
                }
            }
            else -> {
                val layout = portraitBoardsLayout(maxWidth, maxHeight)
                Column(
                    verticalArrangement = Arrangement.spacedBy(layout.gap),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PersonBoard(uiState, style, Modifier.size(layout.boardSize), onGridPlaced)
                    ComputerBoard(uiState, onComputerCellClick, style, Modifier.size(layout.boardSize))
                }
            }
        }
    }
}

// The battle has no status text, but an empty line keeps its slot, so the boards don't move when
// the game-over text appears.
@Composable
private fun StatusText(statusResId: Int?, modifier: Modifier = Modifier, fontSize: TextUnit = 18.sp) {
    BasicText(
        text = statusResId?.let { stringResource(it) } ?: "",
        style = TextStyle(
            color = SeaBattleTheme.colors.foreground,
            fontFamily = NeuchaFontFamily,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
        ),
        modifier = modifier,
    )
}

@Composable
private fun PersonBoard(
    uiState: GameUiState,
    style: BoardStyle,
    modifier: Modifier = Modifier,
    onGridPlaced: (Rect) -> Unit = {},
    onCellClick: ((Coordinate) -> Unit)? = null,
) {
    LabeledBoard(
        ships = uiState.personBoardShips,
        crosses = uiState.personBoardCrosses,
        dots = uiState.personBoardDots,
        highlighted = uiState.selectedShipCells,
        rejected = uiState.rejectedCells,
        onCellTap = onCellClick,
        modifier = modifier,
        style = style,
        onGridPlaced = onGridPlaced,
    )
}

@Composable
private fun ComputerBoard(
    uiState: GameUiState,
    onCellClick: (Coordinate) -> Unit,
    style: BoardStyle,
    modifier: Modifier = Modifier,
) {
    LabeledBoard(
        ships = uiState.computerBoardShips,
        crosses = uiState.computerBoardCrosses,
        dots = uiState.computerBoardDots,
        highlighted = emptyList(),
        onCellTap = if (uiState.phase == GamePhase.BATTLE) onCellClick else null,
        modifier = modifier,
        style = style,
    )
}

@Composable
private fun ActionArea(uiState: GameUiState, onNewGame: () -> Unit) {
    if (uiState.phase != GamePhase.OVER) return
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        // A long label wraps instead of pushing the button off screen.
        GameButton(stringResource(R.string.new_game_text), onNewGame, Modifier.weight(1f, fill = false))
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
            MenuEntry(R.string.new_game_text, MenuAction.NEW_GAME, onMenuAction) { expanded = false }
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
        text = { Text(stringResource(textResId), fontFamily = NeuchaFontFamily, fontSize = 20.sp) },
        onClick = {
            dismiss()
            onMenuAction(action)
        },
    )
}

@Composable
private fun NewGameDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val textStyle = TextStyle(fontFamily = NeuchaFontFamily, fontSize = 20.sp)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_game_dialog_title), style = textStyle.copy(fontSize = 24.sp)) },
        text = { Text(stringResource(R.string.new_game_dialog_message), style = textStyle) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.dialog_yes), style = textStyle) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_no), style = textStyle) }
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
            onComputerCellClick = {}, onMenuAction = {},
            onPersonCellClick = {}, onConfirmNewGame = {}, onDismissNewGame = {},
        )
    }
}

// The single board shown while placing ships: as wide as the portrait boards, but limited by
// the height in landscape.
internal fun placementBoardSize(maxWidth: Dp, maxHeight: Dp): Dp {
    val cell = minOf(
        (maxWidth * PORTRAIT_BOARD_WIDTH_FRACTION - LABEL_SIZE) / SQUARES_COUNT,
        (maxHeight - LABEL_SIZE) / SQUARES_COUNT,
    )
    return if (cell > 0.dp) LABEL_SIZE + cell * SQUARES_COUNT else 0.dp
}

// The portrait placement board leaves the same height above and below it, enough for the hint or
// the button stack, whichever is taller, plus a margin on both sides of it.
internal fun portraitPlacementBoardSize(maxWidth: Dp, maxHeight: Dp, controlsHeight: Dp): Dp =
    placementBoardSize(maxWidth, maxHeight - (controlsHeight + PLACEMENT_CONTROLS_MARGIN * 2) * 2)

internal data class LandscapeBoardsLayout(val boardSize: Dp, val gap: Dp)

// Two square boards side by side, sized together from the available space so they keep the
// same proportions on every screen. The grids are LANDSCAPE_GAP_SQUARES notebook squares
// apart, so both sit on the page's lines; the gap also holds the computer board's number
// column, so on tiny screens, where those squares are narrower than it, more squares are used.
internal fun landscapeBoardsLayout(maxWidth: Dp, maxHeight: Dp): LandscapeBoardsLayout {
    val cellByHeight = (maxHeight - LABEL_SIZE) / SQUARES_COUNT
    for (squares in LANDSCAPE_GAP_SQUARES..MAX_GAP_SQUARES) {
        val cell = minOf(cellByHeight, (maxWidth - LABEL_SIZE) / (2 * SQUARES_COUNT + squares))
        if (cell <= 0.dp) break
        if (cell * squares >= LABEL_SIZE) {
            return LandscapeBoardsLayout(boardSize = LABEL_SIZE + cell * SQUARES_COUNT, gap = cell * squares - LABEL_SIZE)
        }
    }
    return LandscapeBoardsLayout(boardSize = 0.dp, gap = 0.dp)
}

internal data class PortraitBoardsLayout(val boardSize: Dp, val gap: Dp)

// Two boards stacked, as wide as PORTRAIT_BOARD_WIDTH_FRACTION allows. The computer's grid
// starts a whole number of notebook squares below the player's grid: at least enough squares
// to hold its letter row, and otherwise close to an even share of the free height.
internal fun portraitBoardsLayout(maxWidth: Dp, maxHeight: Dp): PortraitBoardsLayout {
    val cellByWidth = (maxWidth * PORTRAIT_BOARD_WIDTH_FRACTION - LABEL_SIZE) / SQUARES_COUNT
    val fit = (1..MAX_GAP_SQUARES).firstNotNullOfOrNull { squares ->
        val cell = minOf(cellByWidth, (maxHeight - LABEL_SIZE) / (2 * SQUARES_COUNT + squares))
        if (cell > 0.dp && cell * squares >= LABEL_SIZE) cell to squares else null
    } ?: return PortraitBoardsLayout(boardSize = 0.dp, gap = 0.dp)
    val (cell, minSquares) = fit
    val boardSize = LABEL_SIZE + cell * SQUARES_COUNT
    val freeHeight = maxHeight - boardSize * 2
    var squares = ((freeHeight / 3 + LABEL_SIZE) / cell).roundToInt().coerceAtLeast(minSquares)
    while (squares > minSquares && cell * squares - LABEL_SIZE > freeHeight) squares--
    return PortraitBoardsLayout(boardSize = boardSize, gap = cell * squares - LABEL_SIZE)
}

// Beyond this, the screen is too small to draw the boards at all.
private const val MAX_GAP_SQUARES = 40
