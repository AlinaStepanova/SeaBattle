package com.avs.sea_battle.battle_field

import com.avs.sea_battle.SQUARES_COUNT

class Cell() {

    private var coordinate = Coordinate()

    private var cellState: CellState = CellState.EMPTY

    constructor(cellState: CellState) : this() {
        this.cellState = cellState
    }

    constructor(x: Int, y: Int) : this() {
        this.coordinate.x = x
        this.coordinate.y = y
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

    fun getCoordinate(): Coordinate {
        return coordinate
    }

    fun setCoordinates(x: Int, y: Int) {
        if (x in 0 until SQUARES_COUNT && y in 0 until SQUARES_COUNT) {
            this.coordinate.x = x
            this.coordinate.y = y
        }
    }

    fun getX(): Int {
        return coordinate.x
    }

    fun getY(): Int {
        return coordinate.y
    }

    override fun toString(): String {
        return this.getCellState().toString() + " x = " + getX() + " y = " + getY()
    }

    override fun hashCode(): Int {
        var result = coordinate.hashCode()
        result = 31 * result + cellState.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cell

        if (coordinate != other.coordinate) return false
        if (cellState != other.cellState) return false

        return true
    }
}