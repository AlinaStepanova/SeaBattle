package com.avs.sea.battle.battle_field

import com.avs.sea.battle.ships.Orientation

// Where one ship lies: its top/left end and the direction it extends in.
data class ShipPlacement(val size: Int, val origin: Coordinate, val orientation: Orientation) {

    fun cells(): List<Coordinate> = (0 until size).map { offset ->
        if (orientation == Orientation.VERTICAL) {
            Coordinate(origin.x + offset, origin.y)
        } else {
            Coordinate(origin.x, origin.y + offset)
        }
    }

    fun movedTo(newOrigin: Coordinate): ShipPlacement = copy(origin = newOrigin.copy())

    fun rotated(): ShipPlacement = copy(
        orientation = if (orientation == Orientation.VERTICAL) Orientation.HORIZONTAL else Orientation.VERTICAL
    )

    fun isInsideBoard(): Boolean = cells().all { it.isOnBoard() }
}
