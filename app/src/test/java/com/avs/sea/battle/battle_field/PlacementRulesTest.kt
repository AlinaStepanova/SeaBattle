package com.avs.sea.battle.battle_field

import com.avs.sea.battle.ships.Orientation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlacementRulesTest {

    private val existing = listOf(ShipPlacement(2, Coordinate(4, 4), Orientation.HORIZONTAL))

    private fun single(x: Int, y: Int) = ShipPlacement(1, Coordinate(x, y), Orientation.HORIZONTAL)

    @Test
    fun shipOnAnEmptyBoardFits() {
        assertTrue(PlacementRules.canPlace(single(0, 0), emptyList()))
    }

    @Test
    fun shipOneCellApartFits() {
        assertTrue(PlacementRules.canPlace(single(4, 7), existing))
        assertTrue(PlacementRules.canPlace(single(2, 4), existing))
    }

    @Test
    fun overlappingShipDoesNotFit() {
        assertFalse(PlacementRules.canPlace(single(4, 5), existing))
    }

    @Test
    fun shipTouchingSideBySideDoesNotFit() {
        assertFalse(PlacementRules.canPlace(single(4, 6), existing))
        assertFalse(PlacementRules.canPlace(single(3, 4), existing))
    }

    @Test
    fun shipTouchingDiagonallyDoesNotFit() {
        assertFalse(PlacementRules.canPlace(single(3, 3), existing))
        assertFalse(PlacementRules.canPlace(single(5, 6), existing))
    }

    @Test
    fun shipLeavingTheBoardDoesNotFit() {
        assertFalse(PlacementRules.canPlace(ShipPlacement(3, Coordinate(0, 8), Orientation.HORIZONTAL), emptyList()))
    }
}
