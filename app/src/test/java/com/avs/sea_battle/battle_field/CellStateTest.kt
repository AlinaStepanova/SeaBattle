package com.avs.sea_battle.battle_field

import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class CellStateTest {

    private lateinit var cellState: CellState

    @Before
    fun setUp() {
        cellState = CellState.EMPTY
    }

    @Test
    fun getState() {
        assertTrue(cellState.state == CellState.EMPTY.state)
        cellState = CellState.SHIP
        assertTrue(cellState.name == "SHIP")
    }
}