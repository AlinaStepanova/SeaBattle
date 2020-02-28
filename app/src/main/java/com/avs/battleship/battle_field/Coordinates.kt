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

data class CoordinateF(private var _x: Float = 0.0F, private var _y: Float = 0.0F) {
    var x: Float get() = _x
        set(value) {
            _x = value
        }
    var y: Float get() = _y
        set(value) {
            _y = value
        }
}