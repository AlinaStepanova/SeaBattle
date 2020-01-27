package com.avs.battleship.battle_field

import android.graphics.Point

class Cell() {

    private var point: Point = Point()

    private var cellState: CellState = CellState.EMPTY

    constructor(cellState: CellState, i: Int, j: Int) : this() {
        this.cellState = cellState
        this.point.set(i, j)
    }

    constructor(i: Int, j: Int) : this() {
        this.point.set(i, j)
    }

    fun getCellState(): CellState {
        return cellState
    }

    public fun getPoint(): Point {
        return point
    }

    public fun getI(): Int {
        return point.x
    }

    public fun getJ(): Int {
        return point.y
    }
}