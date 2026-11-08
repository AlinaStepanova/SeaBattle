package com.avs.sea.battle.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.aspectRatio
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

private val MENU_BUTTON_SIZE = 48.dp
private val BOARD_GAP = 56.dp
private val ACTION_AREA_HEIGHT = 56.dp
private val LANDSCAPE_ACTION_AREA_HEIGHT = 44.dp
private val SPINNER_SIZE = 16.dp

// The grid itself spans about 75% of the width, as in the original layout; the rest is labels.
private const val PORTRAIT_BOARD_WIDTH_FRACTION = 0.8f

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
            if (isLandscape) {
                // Horizontal padding keeps the computer board clear of the overflow menu.
                Row(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = MENU_BUTTON_SIZE, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(BOARD_GAP, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val boardModifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxHeight()
                        .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    PersonBoard(uiState, BoardStyle.Landscape, boardModifier)
                    ComputerBoard(uiState, onCellClick, BoardStyle.Landscape, boardModifier)
                }
            } else {
                Column(
                    Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val boardModifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(PORTRAIT_BOARD_WIDTH_FRACTION)
                    PersonBoard(uiState, BoardStyle.Portrait, boardModifier)
                    ComputerBoard(uiState, onCellClick, BoardStyle.Portrait, boardModifier)
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(if (isLandscape) LANDSCAPE_ACTION_AREA_HEIGHT else ACTION_AREA_HEIGHT),
                contentAlignment = Alignment.Center,
            ) {
                ActionArea(uiState, onGenerateShips, onStartGame, onNewGame, onFire)
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
private fun PersonBoard(uiState: GameUiState, style: BoardStyle, modifier: Modifier = Modifier) {
    LabeledBoard(
        ships = uiState.personBoardShips,
        crosses = uiState.personBoardCrosses,
        dots = uiState.personBoardDots,
        selected = null,
        onCellTap = null,
        modifier = modifier,
        style = style,
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
        selected = uiState.selectedCoordinate,
        onCellTap = if (uiState.phase == GamePhase.BATTLE) onCellClick else null,
        modifier = modifier,
        style = style,
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
                strokeWidth = 2.dp,
                modifier = Modifier.size(SPINNER_SIZE),
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
