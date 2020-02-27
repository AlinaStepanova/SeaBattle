package com.avs.battleship.battle_field

data class Coordinate(private var _x: Int = -1, private var _y: Int = -1) {
    var x: Int get() = _x
    set(value) {
        _x = value
    }
    var y: Int get() = _y
        set(value) {
            _y = value
        }
}

data class CoordinateF(val x: Float = 0.0F, val y: Float = 0.0F)