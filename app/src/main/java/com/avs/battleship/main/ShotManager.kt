package com.avs.battleship.main

import android.graphics.Point
import android.util.Log
import com.avs.battleship.*
import com.avs.battleship.battle_field.Cell
import com.avs.battleship.battle_field.CellState

class ShotManager {

    private val battleField =
        Array(SQUARES_COUNT) { arrayOfNulls<Cell>(SQUARES_COUNT) }

    lateinit var latestPoint: Point

    init {
        initBattleShip()
    }

    private fun initBattleShip() {
        for (i in battleField.indices) {
            for (j in battleField[i].indices) {
                battleField[i][j] = Cell(i, j)
            }
        }
    }

    fun getPointToShoot(): Point {
        do {
            latestPoint = Point((0 until SQUARES_COUNT).random(), (0 until SQUARES_COUNT).random())
        } while (!isCellFreeToBeSelected(latestPoint))

        return latestPoint
    }

    private fun isCellFreeToBeSelected(point: Point): Boolean {
        return (point.x in 0..SQUARES_COUNT
                && point.y in 0..SQUARES_COUNT
                && (battleField[point.x][point.y]?.getCellState() == CellState.EMPTY
                || battleField[point.x][point.y]?.getCellState() == CellState.SHIP))
    }

    fun handleShot(shipHit: Boolean) {
        battleField[latestPoint.x][latestPoint.y]?.setCellState(
            if (shipHit) CellState.SHOT_SUCCESS
            else CellState.SHOT_FAILURE
        )
    }
}