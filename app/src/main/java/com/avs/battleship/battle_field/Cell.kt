package com.avs.battleship.battle_field

import android.graphics.Point

class Cell() {

    private var point: Point = Point()

    private var cellState: CellState = CellState.EMPTY

    constructor(cellState: CellState) : this() {
        this.cellState = cellState
    }

    constructor(x: Int, y: Int) : this() {
        this.point.set(x, y)
    }

    fun setCellState(cellState: CellState) {
        this.cellState = cellState
    }

    fun isState(cellState: CellState) : Boolean {
        return this.getCellState() == cellState
    }

    fun getCellState(): CellState {
        return cellState
    }

    fun getPoint(): Point {
        return point
    }

    fun setCoordinates(x: Int, y: Int) {
        point.set(x, y)
    }

    fun getX(): Int {
        return point.x
    }

    fun getY(): Int {
        return point.y
    }

    override fun toString(): String {
        return this.getCellState().toString() + " x = " + getX() + " y = " + getY()
    }
}