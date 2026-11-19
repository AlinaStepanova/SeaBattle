package com.avs.sea.battle.ships

import com.avs.sea.battle.FOUR_DECK_SHIP_SIZE
import com.avs.sea.battle.ONE_DECK_SHIP_SIZE
import com.avs.sea.battle.THREE_DECK_SHIP_SIZE
import com.avs.sea.battle.TWO_DECK_SHIP_SIZE
import com.avs.sea.battle.battle_field.Cell
import com.avs.sea.battle.battle_field.Coordinate
import kotlin.collections.ArrayList
import com.avs.sea.battle.battle_field.CellState.*

abstract class Ship {

    private var cells: ArrayList<Cell> = ArrayList(FOUR_DECK_SHIP_SIZE)
    private lateinit var row: IntRange
    private lateinit var column: IntRange
    private var orientation: Orientation = Orientation.HORIZONTAL

    protected fun initCells() {
        for (index in 0 until getLength()) {
            cells.add(Cell(SHIP))
        }
        cells.trimToSize()
    }

    fun getShipCells(): ArrayList<Cell> {
        return cells
    }

    fun getShipOrientation(): Orientation {
        return orientation
    }

    fun getRowCoordinates(): IntRange {
        return row
    }

    fun getColumnCoordinates(): IntRange {
        return column
    }

    fun place(origin: Coordinate, orientation: Orientation) {
        this.orientation = orientation
        setCellsCoordinates(origin.x, origin.y)
    }

    fun setCellsCoordinates(i: Int, j: Int) {
        var iPos = i
        var jPos = j
        if (orientation == Orientation.VERTICAL) {
            row = IntRange(i, i + getLength() - 1)
            column = IntRange(j, j)
            for (index in 0 until getLength()) {
                cells[index].setCoordinates(iPos, jPos)
                iPos++
            }
        } else {
            row = IntRange(i, i)
            column = IntRange(j, j + getLength() - 1)
            for (index in 0 until getLength()) {
                cells[index].setCoordinates(iPos, jPos)
                jPos++
            }
        }
    }

    fun setShotSuccessState(coordinate: Coordinate) {
        for (cell in cells) {
            if (cell.getX() == coordinate.x && cell.getY() == coordinate.y) {
                cell.setCellState(SHOT_SUCCESS)
                break
            }
        }
    }

    fun isDead(): Boolean {
        var isDead = true
        for (cell in cells) {
            if (cell.getCellState() == SHIP) {
                isDead = false
                break
            }
        }
        return isDead
    }

    abstract fun getLength(): Int

    override fun toString(): String {
        return getLength().toString() + " " + orientation + " " + cells.toString()
    }

    companion object {
        fun ofSize(size: Int): Ship = when (size) {
            FOUR_DECK_SHIP_SIZE -> FourDeckShip()
            THREE_DECK_SHIP_SIZE -> ThreeDeckShip()
            TWO_DECK_SHIP_SIZE -> TwoDeckShip()
            ONE_DECK_SHIP_SIZE -> OneDeckShip()
            else -> throw IllegalArgumentException("No ship of size $size")
        }
    }
}