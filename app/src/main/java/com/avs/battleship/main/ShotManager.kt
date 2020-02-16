package com.avs.battleship.main

import android.graphics.Point
import com.avs.battleship.*
import com.avs.battleship.battle_field.BaseBattleField
import com.avs.battleship.battle_field.Cell
import com.avs.battleship.battle_field.CellState

class ShotManager {

    private val battleField = BaseBattleField()

    private lateinit var latestCell: Cell

    fun getRandomPoint(): Point {
        latestCell = Cell()
        do {
            latestCell.setCoordinates((0 until SQUARES_COUNT).random(), (0 until SQUARES_COUNT).random())
        } while (!battleField.isCellFreeToBeSelected(latestCell.getPoint()))
        return latestCell.getPoint()
    }

    fun handleShot(shipHit: Boolean) {
        battleField.setCellState(
            latestCell.getPoint(),
            if (shipHit) CellState.SHOT_SUCCESS
            else CellState.SHOT_FAILURE
        )
    }
}