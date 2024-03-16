package com.avs.sea.battle.main

import com.avs.sea.battle.TWO_DECK_SHIP_SIZE
import com.avs.sea.battle.battle_field.Cell
import com.avs.sea.battle.battle_field.CellState
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.ships.Orientation
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class ShotManagerTest {

    private lateinit var shotManager: ShotManager

    @Before
    fun setUp() {
        shotManager = ShotManager()
    }

    @Test
    fun getMaxPoint() {
        assertEquals(
            shotManager.getMaxCoordinate(
                mutableListOf(
                    Coordinate(5, 4),
                    Coordinate(3, 4),
                    Coordinate(4, 4)
                ), Orientation.VERTICAL
            ), Coordinate(5, 4)
        )
        assertEquals(
            shotManager.getMaxCoordinate(
                mutableListOf(
                    Coordinate(5, 4),
                    Coordinate(5, 3),
                    Coordinate(5, 5),
                    Coordinate(5, 6)
                ), Orientation.HORIZONTAL
            ), Coordinate(5, 6)
        )
    }

    @Test
    fun getMinPoint() {
        assertEquals(
            shotManager.getMinCoordinate(
                mutableListOf(
                    Coordinate(5, 4),
                    Coordinate(3, 4),
                    Coordinate(4, 4)
                ), Orientation.VERTICAL
            ), Coordinate(3, 4)
        )
        assertEquals(
            shotManager.getMinCoordinate(
                mutableListOf(
                    Coordinate(5, 4),
                    Coordinate(5, 3),
                    Coordinate(5, 5),
                    Coordinate(5, 6)
                ), Orientation.HORIZONTAL
            ), Coordinate(5, 3)
        )
    }

    @Test
    fun resetValues() {
        shotManager.resetCells()
        assertEquals(shotManager.getFirstCell().getCellState(), CellState.EMPTY)
        assertEquals(shotManager.getSecondCell().getCellState(), CellState.EMPTY)
        assertEquals(shotManager.getThirdCell().getCellState(), CellState.EMPTY)
        assertEquals(shotManager.getFourthCell().getCellState(), CellState.EMPTY)
    }

    @Test
    fun updateBattleField() {
        shotManager.updateBattleField(true, Cell(1, 1))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(1, 1)))
        shotManager.updateBattleField(false, Cell(2, 2))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(2, 2)))
    }

    @Test
    fun getRandomCoordinate() {
        val coordinate = shotManager.getRandomCoordinate()
        assertTrue(coordinate.x in 0..9)
        assertTrue(coordinate.y in 0..9)
    }

    @Test
    fun isLeftCellAvailable() {
        shotManager.updateBattleField(false, Cell(2, 2))
        assertFalse(shotManager.isLeftCellAvailable(Coordinate(2, 3)))
        assertTrue(shotManager.isLeftCellAvailable(Coordinate(5, 5)))
    }

    @Test
    fun isRightCellAvailable() {
        shotManager.updateBattleField(false, Cell(2, 2))
        assertFalse(shotManager.isRightCellAvailable(Coordinate(2, 1)))
        assertTrue(shotManager.isRightCellAvailable(Coordinate(5, 5)))
    }

    @Test
    fun isTopCellAvailable() {
        shotManager.updateBattleField(false, Cell(2, 2))
        assertFalse(shotManager.isTopCellAvailable(Coordinate(3, 2)))
        assertTrue(shotManager.isTopCellAvailable(Coordinate(5, 5)))
    }

    @Test
    fun isBottomCellAvailable() {
        shotManager.updateBattleField(false, Cell(2, 2))
        assertFalse(shotManager.isBottomCellAvailable(Coordinate(1, 2)))
        assertTrue(shotManager.isBottomCellAvailable(Coordinate(5, 5)))
    }

    @Test
    fun markHorizontalNeighbours() {
        shotManager.markHorizontalNeighbours(Cell(2, 2))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(2, 1)))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(2, 3)))
        assertTrue(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(2, 4)))

    }

    @Test
    fun markVerticalNeighbours() {
        shotManager.markVerticalNeighbours(Cell(2, 2))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(1, 2)))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(3, 2)))
        assertTrue(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(4, 2)))
        assertTrue(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(0, 2)))
    }

    @Test
    fun checkNeighbourCells() {
        var coordinate = shotManager.checkNeighbourCells(Cell(2, 5), Cell(2, 6))
        assertTrue(coordinate.x == 2)
        assertTrue(coordinate.y == 5)
        coordinate = shotManager.checkNeighbourCells(Cell(4, 6), Cell(3, 6))
        assertTrue(coordinate.x == 2)
        assertTrue(coordinate.y == 6)
    }

    @Test
    fun markEdgeCells() {
        shotManager.markEdgeCells(mutableListOf(Coordinate(2, 2), Coordinate(2, 3)))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(2, 1)))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(2, 4)))
        shotManager.markEdgeCells(
            mutableListOf(Coordinate(5, 5), Coordinate(3, 5), Coordinate(4, 5))
        )
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(6, 5)))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(2, 5)))
        shotManager.markEdgeCells(mutableListOf(Coordinate(8, 8)))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(8, 7)))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(8, 9)))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(7, 8)))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(9, 8)))
    }

    @Test
    fun getNextCoordinateToShot() {
        var coordinate = shotManager.getNextCoordinateToShot(Cell(5, 5))
        assertTrue(coordinate.x == 5)
        assertTrue(coordinate.y == 4)
        shotManager.updateBattleField(false, Cell(5, 4))
        coordinate = shotManager.getNextCoordinateToShot(Cell(5, 5))
        assertTrue(coordinate.x == 5)
        assertTrue(coordinate.y == 6)
        shotManager.updateBattleField(false, Cell(5, 6))
        coordinate = shotManager.getNextCoordinateToShot(Cell(5, 5))
        assertTrue(coordinate.x == 4)
        assertTrue(coordinate.y == 5)
        shotManager.updateBattleField(false, Cell(4, 5))
        coordinate = shotManager.getNextCoordinateToShot(Cell(5, 5))
        assertTrue(coordinate.x == 6)
        assertTrue(coordinate.y == 5)
        shotManager.updateBattleField(false, Cell(6, 5))
        coordinate = shotManager.getNextCoordinateToShot(Cell(5, 5))
        assertTrue(coordinate.x == -1)
        assertTrue(coordinate.y == -1)
    }

    @Test
    fun checkHorizontalCoordinates() {
        var coordinate = shotManager.checkHorizontalCoordinates(Coordinate(5, 7), Coordinate(5, 8))
        assertTrue(coordinate.x == 4)
        assertTrue(coordinate.y == 7)
        shotManager.updateBattleField(false, Cell(4, 7))
        coordinate = shotManager.checkHorizontalCoordinates(Coordinate(5, 7), Coordinate(5, 8))
        assertTrue(coordinate.x == 6)
        assertTrue(coordinate.y == 7)
        shotManager.updateBattleField(false, Cell(6, 7))
        coordinate = shotManager.checkHorizontalCoordinates(Coordinate(5, 7), Coordinate(5, 8))
        assertTrue(coordinate.x == 4)
        assertTrue(coordinate.y == 8)
        shotManager.updateBattleField(false, Cell(4, 8))
        coordinate = shotManager.checkHorizontalCoordinates(Coordinate(5, 7), Coordinate(5, 8))
        assertTrue(coordinate.x == 6)
        assertTrue(coordinate.y == 8)
        shotManager.updateBattleField(false, Cell(6, 8))
        coordinate = shotManager.checkHorizontalCoordinates(Coordinate(5, 7), Coordinate(5, 8))
        assertTrue(coordinate.x == -1)
        assertTrue(coordinate.y == -1)
    }

    @Test
    fun checkVerticalCoordinates() {
        var coordinate = shotManager.checkVerticalCoordinates(Coordinate(2, 4), Coordinate(3, 4))
        assertTrue(coordinate.x == 2)
        assertTrue(coordinate.y == 3)
        shotManager.updateBattleField(false, Cell(2, 3))
        coordinate = shotManager.checkVerticalCoordinates(Coordinate(2, 4), Coordinate(3, 4))
        assertTrue(coordinate.x == 2)
        assertTrue(coordinate.y == 5)
        shotManager.updateBattleField(false, Cell(2, 5))
        coordinate = shotManager.checkVerticalCoordinates(Coordinate(2, 4), Coordinate(3, 4))
        assertTrue(coordinate.x == 3)
        assertTrue(coordinate.y == 3)
        shotManager.updateBattleField(false, Cell(3, 3))
        coordinate = shotManager.checkVerticalCoordinates(Coordinate(2, 4), Coordinate(3, 4))
        assertTrue(coordinate.x == 3)
        assertTrue(coordinate.y == 5)
        shotManager.updateBattleField(false, Cell(3, 5))
        coordinate = shotManager.checkVerticalCoordinates(Coordinate(2, 4), Coordinate(3, 4))
        assertTrue(coordinate.x == -1)
        assertTrue(coordinate.y == -1)
    }

    @Test
    fun resetValuesAfterShipIsDead() {
        assertEquals(shotManager.getShipsSize(), 10)
        shotManager.resetValuesAfterShipIsDead(mutableListOf(Coordinate(1, 1), Coordinate(1, 2)))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(1, 0)))
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(Coordinate(1, 3)))
        resetValues()
        assertEquals(shotManager.getShipsSize(), 9)
    }

    @Test
    fun getCoordinateToShot() {
        var coordinate = shotManager.getCoordinateToShot()
        assertEquals(coordinate, shotManager.getFirstCell().getCoordinate())
        shotManager.handleShot(true)
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(coordinate))
        coordinate = shotManager.getCoordinateToShot()
        assertEquals(coordinate, shotManager.getSecondCell().getCoordinate())
        shotManager.handleShot(true)
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(coordinate))
        coordinate = shotManager.getCoordinateToShot()
        assertEquals(coordinate, shotManager.getThirdCell().getCoordinate())
        shotManager.handleShot(true)
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(coordinate))
        coordinate = shotManager.getCoordinateToShot()
        assertEquals(coordinate, shotManager.getFourthCell().getCoordinate())
        shotManager.handleShot(true)
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(coordinate))
        coordinate = shotManager.getCoordinateToShot()
        assertEquals(coordinate, shotManager.getFirstCell().getCoordinate())
        shotManager.handleShot(false)
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(coordinate))
        coordinate = shotManager.getCoordinateToShot()
        assertEquals(coordinate, shotManager.getFirstCell().getCoordinate())
        shotManager.handleShot(true)
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(coordinate))
        coordinate = shotManager.getCoordinateToShot()
        assertEquals(coordinate, shotManager.getSecondCell().getCoordinate())
        shotManager.handleShot(false)
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(coordinate))
        coordinate = shotManager.getCoordinateToShot()
        assertEquals(coordinate, shotManager.getSecondCell().getCoordinate())
        shotManager.handleShot(true)
        assertFalse(shotManager.getBattleField().isCellFreeToBeSelected(coordinate))
    }
}