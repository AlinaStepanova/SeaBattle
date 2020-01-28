package com.avs.battleship.battle_field

import com.avs.battleship.MAX_SHIP_SIZE
import java.util.*
import kotlin.collections.ArrayList

enum class Orientation {
    VERTICAL, HORIZONTAL
}

abstract class Ship() {

    protected var cells: ArrayList<Cell> = ArrayList(MAX_SHIP_SIZE)
    protected var orientation: Orientation = if (Random().nextBoolean()) {
        Orientation.VERTICAL
    } else {
        Orientation.HORIZONTAL
    }

    protected fun initCells() {
        for (index in 0..getLength()) {
            cells[index] = Cell(CellState.SHIP)
        }
        cells.trimToSize()
    }

    open fun getRandomBoolean(): Boolean {
        val random = Random()
        return random.nextBoolean()
    }

    protected fun isDead(): Boolean {
        var isDead = true
        for (cell in cells) {
            if (cell.getCellState() == CellState.SHIP) {
                isDead = false
                break
            }
        }
        return isDead
    }

    abstract fun getLength(): Int

}