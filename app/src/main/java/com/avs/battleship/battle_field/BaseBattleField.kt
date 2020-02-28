package com.avs.battleship.battle_field

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

    fun setCellState(coordinate: Coordinate, cellState: CellState) {
        if (coordinate.x in 0 until SQUARES_COUNT && coordinate.y in 0 until SQUARES_COUNT) {
            battleField[coordinate.x][coordinate.y]?.setCellState(cellState)
        }
    }

    fun isCellFreeToBeSelected(coordinate: Coordinate): Boolean {
        return (coordinate.x in 0 until SQUARES_COUNT
                && coordinate.y in 0 until SQUARES_COUNT
                && (battleField[coordinate.x][coordinate.y]?.getCellState() == CellState.EMPTY
                || battleField[coordinate.x][coordinate.y]?.getCellState() == CellState.SHIP))
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