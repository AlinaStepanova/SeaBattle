package com.avs.battleship.battle_field

import android.graphics.Point
import android.util.Log
import com.avs.battleship.SQUARES_COUNT

open class BaseBattleField {

    protected val battleField =
        Array(SQUARES_COUNT) { arrayOfNulls<Cell>(SQUARES_COUNT) }

    init {
        initBattleShip()
    }

    fun initBattleShip() {
        for (i in battleField.indices) {
            for (j in battleField[i].indices) {
                battleField[i][j] = Cell(i, j)
            }
        }
    }

    fun setCellState(point: Point, cellState: CellState) {
        if (point.x in 0 until SQUARES_COUNT && point.y in 0 until SQUARES_COUNT) {
            battleField[point.x][point.y]?.setCellState(cellState)
        }
    }

    fun isCellFreeToBeSelected(point: Point): Boolean {
        return (point.x in 0 until SQUARES_COUNT
                && point.y in 0 until SQUARES_COUNT
                && (battleField[point.x][point.y]?.getCellState() == CellState.EMPTY
                || battleField[point.x][point.y]?.getCellState() == CellState.SHIP))
    }

    fun printBattleField() {
        var result = "\n"
        for (i in battleField.indices) {
            for (j in battleField[i].indices) {
                result += battleField[i][j]?.getCellState()?.state + " "
            }
            result += "\n"
        }
        Log.d(this.javaClass.simpleName, result)
    }
}