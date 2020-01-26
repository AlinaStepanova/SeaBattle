package com.avs.battleship.battle_field

class Cell() {

    private var cellState : CellState = CellState.EMPTY

    constructor(cellState: CellState) : this() {
        this.cellState = cellState
    }

    fun getCellState() : CellState {
        return cellState
    }
}