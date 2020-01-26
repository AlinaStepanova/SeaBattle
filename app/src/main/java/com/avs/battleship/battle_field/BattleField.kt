package com.avs.battleship.battle_field

import android.util.Log
import com.avs.battleship.SQUARES_COUNT

class BattleField {

    private val battleField =
        Array(SQUARES_COUNT) { arrayOfNulls<Cell>(SQUARES_COUNT) }

    init {
        initBattleShip()
        printBattleField()
    }

    private fun initBattleShip() {
        for (i in battleField.indices) {
            for (j in battleField[i].indices) {
                battleField[i][j] = Cell()
            }
        }
    }

    private fun printBattleField() {
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