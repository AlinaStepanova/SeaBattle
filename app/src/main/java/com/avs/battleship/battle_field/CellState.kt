package com.avs.battleship.battle_field

enum class CellState(val state : String) {

    EMPTY("0"), SHIP("1"), SHOT_SUCCESS("X"), SHOT_FAILURE(".");
}