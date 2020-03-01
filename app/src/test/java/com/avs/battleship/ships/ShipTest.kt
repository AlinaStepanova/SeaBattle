package com.avs.battleship.ships

import com.avs.battleship.FOUR_DECK_SHIP_SIZE
import com.avs.battleship.ONE_DECK_SHIP_SIZE
import com.avs.battleship.THREE_DECK_SHIP_SIZE
import com.avs.battleship.TWO_DECK_SHIP_SIZE
import com.avs.battleship.battle_field.CellState
import com.avs.battleship.battle_field.Coordinate
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class ShipTest {

    lateinit var fourDeckShip: Ship
    lateinit var threeDeckShip: Ship
    lateinit var twoDeckShip: Ship
    lateinit var oneDeckShip: Ship

    @Before
    fun setUp() {
        fourDeckShip = FourDeckShip()
        threeDeckShip = ThreeDeckShip()
        twoDeckShip = TwoDeckShip()
        oneDeckShip = OneDeckShip()
    }

    @Test
    fun getShipCells() {
        assertFalse(fourDeckShip.getShipCells().isNullOrEmpty())
        assertEquals(fourDeckShip.getShipCells().size, FOUR_DECK_SHIP_SIZE)
        assertEquals(threeDeckShip.getShipCells().size, THREE_DECK_SHIP_SIZE)
        assertEquals(twoDeckShip.getShipCells().size, TWO_DECK_SHIP_SIZE)
        assertEquals(oneDeckShip.getShipCells().size, ONE_DECK_SHIP_SIZE)
        assertEquals(oneDeckShip.getShipCells()[0].getCellState(), CellState.SHIP)

    }

    @Test
    fun getShipOrientation() {
        assertTrue(fourDeckShip.getShipOrientation().name.isNotEmpty())
        assertTrue(threeDeckShip.getShipOrientation() in Orientation.values())
    }

    @Test
    fun setCellsCoordinates() {
        fourDeckShip.setCellsCoordinates(0, 0)
        oneDeckShip.setCellsCoordinates(5, 5)
        assertEquals(oneDeckShip.getShipCells()[0].getCellState(), CellState.SHIP)
        assertEquals(fourDeckShip.getShipCells()[0].getCellState(), CellState.SHIP)
        assertEquals(fourDeckShip.getShipCells()[1].getCellState(), CellState.SHIP)
        assertEquals(fourDeckShip.getShipCells()[2].getCellState(), CellState.SHIP)
        assertEquals(fourDeckShip.getShipCells()[3].getCellState(), CellState.SHIP)
        if (fourDeckShip.getShipOrientation() == Orientation.VERTICAL) {
            assertEquals(fourDeckShip.getShipCells()[0].getCoordinate(), Coordinate(0, 0))
            assertEquals(fourDeckShip.getShipCells()[1].getCoordinate(), Coordinate(1, 0))
            assertEquals(fourDeckShip.getShipCells()[2].getCoordinate(), Coordinate(2, 0))
            assertEquals(fourDeckShip.getShipCells()[3].getCoordinate(), Coordinate(3, 0))
        } else {
            assertEquals(fourDeckShip.getShipCells()[0].getCoordinate(), Coordinate(0, 0))
            assertEquals(fourDeckShip.getShipCells()[1].getCoordinate(), Coordinate(0, 1))
            assertEquals(fourDeckShip.getShipCells()[2].getCoordinate(), Coordinate(0, 2))
            assertEquals(fourDeckShip.getShipCells()[3].getCoordinate(), Coordinate(0, 3))
        }
        assertEquals(oneDeckShip.getShipCells()[0].getCoordinate(), Coordinate(5, 5))
    }

    @Test
    fun setShotSuccessState() {
        twoDeckShip.setCellsCoordinates(2, 5)
        twoDeckShip.setShotSuccessState(Coordinate(2, 5))
        assertEquals(twoDeckShip.getShipCells().last {
            it.getCoordinate().x == 2 && it.getCoordinate().y == 5
        }.getCellState(), CellState.SHOT_SUCCESS)
    }

    @Test
    fun isDead() {
        oneDeckShip.setCellsCoordinates(1, 1)
        assertFalse(oneDeckShip.isDead())
        oneDeckShip.setShotSuccessState(Coordinate(1, 1))
        assertTrue(oneDeckShip.isDead())
        twoDeckShip.setCellsCoordinates(1, 5)
        twoDeckShip.setShotSuccessState(Coordinate(1, 5))
        assertFalse(twoDeckShip.isDead())
        if (twoDeckShip.getShipOrientation() == Orientation.VERTICAL) {
            twoDeckShip.setShotSuccessState(Coordinate(2, 5))
        } else {
            twoDeckShip.setShotSuccessState(Coordinate(1, 6))
        }
        assertTrue(twoDeckShip.isDead())
    }

    @Test
    fun getRowCoordinates() {
        threeDeckShip.setCellsCoordinates(0, 0)
        if (threeDeckShip.getShipOrientation() == Orientation.VERTICAL) {
            assertEquals(threeDeckShip.getRowCoordinates(), IntRange(0, THREE_DECK_SHIP_SIZE - 1))
        } else {
            assertEquals(threeDeckShip.getRowCoordinates(), IntRange(0, 0))
        }
        oneDeckShip.setCellsCoordinates(8, 1)
        assertEquals(oneDeckShip.getRowCoordinates(), IntRange(8, 8))
    }

    @Test
    fun getColumnCoordinates() {
        threeDeckShip.setCellsCoordinates(2, 4)
        if (threeDeckShip.getShipOrientation() == Orientation.VERTICAL) {
            assertEquals(threeDeckShip.getColumnCoordinates(), IntRange(4, 4))
        } else {
            assertEquals(threeDeckShip.getColumnCoordinates(), IntRange(4, 4 + THREE_DECK_SHIP_SIZE - 1))
        }
        oneDeckShip.setCellsCoordinates(8, 1)
        assertEquals(oneDeckShip.getColumnCoordinates(), IntRange(1, 1))
    }

    @Test
    fun getLength() {
        assertEquals(fourDeckShip.getLength(), FOUR_DECK_SHIP_SIZE)
        assertEquals(threeDeckShip.getLength(), THREE_DECK_SHIP_SIZE)
        assertEquals(twoDeckShip.getLength(), TWO_DECK_SHIP_SIZE)
        assertEquals(oneDeckShip.getLength(), ONE_DECK_SHIP_SIZE)
    }
}