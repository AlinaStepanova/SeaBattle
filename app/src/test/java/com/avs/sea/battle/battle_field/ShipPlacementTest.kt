package com.avs.sea.battle.battle_field

import com.avs.sea.battle.ships.Orientation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShipPlacementTest {

    @Test
    fun horizontalShipExtendsToTheRightOfItsOrigin() {
        val ship = ShipPlacement(3, Coordinate(2, 4), Orientation.HORIZONTAL)
        assertEquals(listOf(Coordinate(2, 4), Coordinate(2, 5), Coordinate(2, 6)), ship.cells())
    }

    @Test
    fun verticalShipExtendsDownFromItsOrigin() {
        val ship = ShipPlacement(3, Coordinate(2, 4), Orientation.VERTICAL)
        assertEquals(listOf(Coordinate(2, 4), Coordinate(3, 4), Coordinate(4, 4)), ship.cells())
    }

    @Test
    fun rotatingKeepsTheOriginAndFlipsTheDirection() {
        val ship = ShipPlacement(4, Coordinate(1, 1), Orientation.HORIZONTAL)
        val rotated = ship.rotated()
        assertEquals(Coordinate(1, 1), rotated.origin)
        assertEquals(Orientation.VERTICAL, rotated.orientation)
        assertEquals(Orientation.HORIZONTAL, rotated.rotated().orientation)
    }

    @Test
    fun movingKeepsSizeAndDirection() {
        val ship = ShipPlacement(2, Coordinate(0, 0), Orientation.VERTICAL)
        assertEquals(ShipPlacement(2, Coordinate(5, 7), Orientation.VERTICAL), ship.movedTo(Coordinate(5, 7)))
    }

    @Test
    fun movedShipDoesNotShareItsOriginWithTheTarget() {
        val target = Coordinate(5, 7)
        val moved = ShipPlacement(2, Coordinate(0, 0), Orientation.VERTICAL).movedTo(target)
        target.x = 0
        assertEquals(Coordinate(5, 7), moved.origin)
    }

    @Test
    fun shipTouchingTheLastRowAndColumnIsInsideTheBoard() {
        assertTrue(ShipPlacement(4, Coordinate(9, 6), Orientation.HORIZONTAL).isInsideBoard())
        assertTrue(ShipPlacement(4, Coordinate(6, 9), Orientation.VERTICAL).isInsideBoard())
    }

    @Test
    fun shipRunningPastAnEdgeIsOutsideTheBoard() {
        assertFalse(ShipPlacement(4, Coordinate(9, 7), Orientation.HORIZONTAL).isInsideBoard())
        assertFalse(ShipPlacement(4, Coordinate(7, 9), Orientation.VERTICAL).isInsideBoard())
        assertFalse(ShipPlacement(1, Coordinate(-1, 0), Orientation.HORIZONTAL).isInsideBoard())
    }
}
