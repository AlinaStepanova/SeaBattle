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
