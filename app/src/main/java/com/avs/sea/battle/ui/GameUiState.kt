package com.avs.sea.battle.ui

import com.avs.sea.battle.FLEET
import com.avs.sea.battle.R
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.main.Player

enum class GamePhase { PLACING, BATTLE, COMPUTER_TURN, OVER }

data class GameUiState(
    val statusResId: Int? = R.string.status_place_ships_text,
    val phase: GamePhase = GamePhase.PLACING,
    val personBoardShips: List<Coordinate> = emptyList(),
    val personBoardCrosses: List<Coordinate> = emptyList(),
    val personBoardDots: List<Coordinate> = emptyList(),
    val computerBoardShips: List<Coordinate> = emptyList(),
    val computerBoardCrosses: List<Coordinate> = emptyList(),
    val computerBoardDots: List<Coordinate> = emptyList(),
    val winner: Player? = null,
    val selectedShipCells: List<Coordinate> = emptyList(),
    val rejectedCells: List<Coordinate> = emptyList(),
    val showNewGameConfirmation: Boolean = false,
    val personFleet: List<FleetShipUi> = emptyList(),
    val computerFleet: List<FleetShipUi> = emptyList(),
)

data class FleetShipUi(val size: Int, val sunk: Boolean)

// One entry per FLEET ship, in FLEET order. Within a size the leftmost ships are the sunk ones,
// so a size group fills up from the left as its ships go down.
internal fun fleetShips(sunkSizes: List<Int>): List<FleetShipUi> = FLEET.mapIndexed { index, size ->
    val earlierOfSameSize = FLEET.take(index).count { it == size }
    FleetShipUi(size, sunk = earlierOfSameSize < sunkSizes.count { it == size })
}

sealed interface UiEvent {
    data object RequestReview : UiEvent
}

enum class MenuAction { NEW_GAME, SHARE, RATE, MORE_APPS, WRITE_TO_AUTHOR, PRIVACY_POLICY }
