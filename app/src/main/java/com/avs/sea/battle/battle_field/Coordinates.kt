package com.avs.sea.battle.battle_field

import com.avs.sea.battle.SQUARES_COUNT

data class Coordinate(private var _x: Int = -1, private var _y: Int = -1) {
    var x: Int
        get() = _x
        set(value) {
            _x = value
        }
    var y: Int
        get() = _y
        set(value) {
            _y = value
        }

    fun isOnBoard(): Boolean = x in 0 until SQUARES_COUNT && y in 0 until SQUARES_COUNT
}

data class CoordinateF(private var _x: Float = 0.0F, private var _y: Float = 0.0F) {
    var x: Float
        get() = _x
        set(value) {
            _x = value
        }
    var y: Float
        get() = _y
        set(value) {
            _y = value
        }
}