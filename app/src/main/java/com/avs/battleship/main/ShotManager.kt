package com.avs.battleship.main

import android.graphics.Point
import com.avs.battleship.*
import com.avs.battleship.battle_field.BaseBattleField
import com.avs.battleship.battle_field.CellState

class ShotManager {

    private val battleField = BaseBattleField()

    lateinit var latestPoint: Point

    fun getRandomPoint(): Point {
        do {
            latestPoint = Point((0 until SQUARES_COUNT).random(), (0 until SQUARES_COUNT).random())
        } while (!battleField.isCellFreeToBeSelected(latestPoint))
        return latestPoint
    }

    fun handleShot(shipHit: Boolean) {
        battleField.setCellState(
            latestPoint,
            if (shipHit) CellState.SHOT_SUCCESS
            else CellState.SHOT_FAILURE
        )
    }
}