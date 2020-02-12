package com.avs.battleship.battle_field

import android.graphics.Point

class Cell() {

    private var point: Point = Point()

    private var cellState: CellState = CellState.EMPTY

    constructor(cellState: CellState) : this() {
        this.cellState = cellState
    }

    constructor(i: Int, j: Int) : this() {
        this.point.set(i, j)
    }

    fun setCellState(cellState: CellState) {
        this.cellState = cellState
    }

    fun getCellState(): CellState {
        return cellState
    }

    fun getPoint(): Point {
        return point
    }

    fun setCoordinates(i: Int, j: Int) {
        point.set(i, j)
    }

    fun getI(): Int {
        return point.x
    }

    fun getJ(): Int {
        return point.y
    }

    override fun toString(): String {
        return this.getCellState().toString() + " i = " + getI() + " j = " + getJ()
    }
}