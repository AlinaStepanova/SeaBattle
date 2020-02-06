package com.avs.battleship.ships

import android.graphics.Point
import com.avs.battleship.MAX_SHIP_SIZE
import com.avs.battleship.battle_field.Cell
import com.avs.battleship.battle_field.CellState
import java.util.*
import kotlin.collections.ArrayList

abstract class Ship {

    protected var cells: ArrayList<Cell> = ArrayList(MAX_SHIP_SIZE)
    protected lateinit var row: IntRange
    protected lateinit var column: IntRange
    protected var orientation: Orientation = when (Random().nextBoolean()) {
        true -> Orientation.VERTICAL
        false -> Orientation.HORIZONTAL
    }

    protected fun initCells() {
        for (index in 0 until getLength()) {
            cells.add(Cell(CellState.SHIP))
        }
        cells.trimToSize()
    }

    public fun getShipCells(): ArrayList<Cell> {
        return cells
    }

    public fun getShipOrientation(): Orientation {
        return orientation
    }

    public fun getRowCoordinates(): IntRange {
        return row
    }

    public fun getColumnCoordinates(): IntRange {
        return column
    }

    public fun setCellsCoordinates(i: Int, j: Int) {
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

    fun setCellState(point: Point, cellState: CellState) {
        for (cell in cells) {
            if (cell.getI() == point.x && cell.getJ() == point.y) {
                cell.setCellState(cellState)
                break
            }
        }
    }

    fun isDead(): Boolean {
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

    override fun toString(): String {
        return getLength().toString() + " " + orientation + " " + cells.toString()
    }
}