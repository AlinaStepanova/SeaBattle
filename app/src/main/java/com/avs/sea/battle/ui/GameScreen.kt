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
                PersonBoard(
                    uiState,
                    Modifier.weight(1f).padding(8.dp).align(Alignment.CenterVertically)
                )
                Column(
                    Modifier.weight(0.8f).fillMaxHeight().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    StatusText(uiState.statusResId, Modifier.weight(1f))
                    ActionArea(uiState, onGenerateShips, onStartGame, onNewGame, onFire)
                }
                ComputerBoard(
                    uiState,
                    onCellClick,
                    Modifier.weight(1f).padding(8.dp).align(Alignment.CenterVertically)
                )
            }
        } else {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                StatusText(
                    uiState.statusResId,
                    Modifier.padding(top = 10.dp, start = 40.dp, end = 40.dp)
                )
                PersonBoard(uiState, Modifier.fillMaxWidth(0.75f).padding(top = 8.dp))
                ComputerBoard(
                    uiState,
                    onCellClick,
                    Modifier.fillMaxWidth(0.75f).padding(top = 16.dp)
                )
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
