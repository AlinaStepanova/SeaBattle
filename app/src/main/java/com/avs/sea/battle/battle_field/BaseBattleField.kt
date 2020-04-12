package com.avs.sea.battle.battle_field

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.battle_field.CellState.*

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

    @VisibleForTesting
    fun getCellsArray(): Array<Array<Cell?>> {
        return battleField
    }

    fun setCellState(coordinate: Coordinate, cellState: CellState) {
        if (coordinate.x in 0 until SQUARES_COUNT && coordinate.y in 0 until SQUARES_COUNT) {
            battleField[coordinate.x][coordinate.y]?.setCellState(cellState)
        }
    }

    fun isCellFreeToBeSelected(coordinate: Coordinate): Boolean {
        return (coordinate.x in 0 until SQUARES_COUNT
                && coordinate.y in 0 until SQUARES_COUNT
                && (battleField[coordinate.x][coordinate.y]?.getCellState() == EMPTY
                || battleField[coordinate.x][coordinate.y]?.getCellState() == SHIP))
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