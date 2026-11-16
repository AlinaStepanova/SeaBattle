package com.avs.sea.battle.battle_field

sealed interface ShotResult {
    data object Miss : ShotResult
    data object Hit : ShotResult
    data class Sunk(val cells: List<Coordinate>) : ShotResult
}
