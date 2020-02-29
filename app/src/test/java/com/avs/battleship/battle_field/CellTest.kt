package com.avs.battleship.battle_field

import org.junit.Before
import org.junit.Test
import com.avs.battleship.battle_field.CellState.*

import org.junit.Assert.*

class CellTest {

    lateinit var cell: Cell

    @Before
    fun setUp() {
        cell = Cell(5, 6)
    }

    @Test
    fun setCellState() {
        cell.setCellState(SHOT_FAILURE)
        assertEquals(cell.getCellState(), SHOT_FAILURE)
    }

    @Test
    fun isState() {
        assertFalse(cell.isState(SHIP))
        val extraCell = Cell(SHIP)
        assertTrue(extraCell.isState(SHIP))
    }

    @Test
    fun getCellState() {
        assertEquals(cell.getCellState(), EMPTY)
    }

    @Test
    fun getCoordinate() {
        assertEquals(cell.getCoordinate(), Coordinate(5, 6))
    }

    @Test
    fun setCoordinates() {
        cell.setCoordinates(2,7)
        assertEquals(cell.getCoordinate(), Coordinate(2, 7))
        cell.setCoordinates(20,-7)
        assertNotEquals(cell.getCoordinate(), Coordinate(20, -7))
    }

    @Test
    fun getX() {
        assertEquals(cell.getX(), 5)
    }

    @Test
    fun getY() {
        assertEquals(cell.getY(), 6)
    }
}