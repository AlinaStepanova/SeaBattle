package com.avs.battleship.battle_field

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
        this.coordinate.x = x
        this.coordinate.y = y
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
}