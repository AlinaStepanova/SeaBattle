package com.avs.sea.battle.battle_field

import com.avs.sea.battle.FLEET
import com.avs.sea.battle.ships.Orientation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FleetLayoutTest {

    private val initial = FleetLayout.initial()

    private fun FleetLayout.isValid(): Boolean = ships.indices.all { index ->
        PlacementRules.canPlace(ships[index], ships.filterIndexed { i, _ -> i != index })
    }

    private fun PlacementResult.layout(): FleetLayout = (this as PlacementResult.Placed).layout

    @Test
    fun initialLayoutHoldsTheWholeFleetLargestFirst() {
        assertEquals(FLEET, initial.ships.map { it.size })
        assertEquals(FLEET.sum(), initial.cells().size)
    }

    @Test
    fun initialLayoutIsValid() {
        assertTrue(initial.isValid())
    }

    @Test
    fun initialLayoutMatchesTheConceptSketch() {
        val fourDeck = initial.ships.first()
        assertEquals(ShipPlacement(4, Coordinate(0, 1), Orientation.HORIZONTAL), fourDeck)
        val singles = initial.ships.filter { it.size == 1 }.map { it.origin }
        assertEquals(listOf(Coordinate(3, 9), Coordinate(5, 9), Coordinate(7, 9), Coordinate(9, 7)), singles)
    }

    @Test
    fun shipIndexAtFindsTheShipCoveringACell() {
        assertEquals(0, initial.shipIndexAt(Coordinate(0, 4)))
        assertEquals(2, initial.shipIndexAt(Coordinate(4, 0)))
        assertNull(initial.shipIndexAt(Coordinate(5, 5)))
    }

    @Test
    fun movingToFreeSpacePlacesTheShipWithItsTopLeftEndOnTheTarget() {
        val moved = initial.move(0, Coordinate(4, 3)).layout()
        assertEquals(ShipPlacement(4, Coordinate(4, 3), Orientation.HORIZONTAL), moved.ships[0])
        assertEquals(initial.ships.drop(1), moved.ships.drop(1))
    }

    @Test
    fun movingDoesNotChangeTheOriginalLayout() {
        initial.move(0, Coordinate(4, 3))
        assertEquals(FleetLayout.initial(), initial)
    }

    @Test
    fun movingPastTheRightEdgeIsRejectedWithTheCellsOnTheBoard() {
        val result = initial.move(0, Coordinate(5, 8))
        assertEquals(PlacementResult.Rejected(listOf(Coordinate(5, 8), Coordinate(5, 9))), result)
    }

    @Test
    fun movingPastTheBottomEdgeIsRejected() {
        val result = initial.move(2, Coordinate(8, 5))
        assertEquals(PlacementResult.Rejected(listOf(Coordinate(8, 5), Coordinate(9, 5))), result)
    }

    @Test
    fun movingNextToAnotherShipIsRejectedWithTheWouldBeCells() {
        // B9-E9 sits diagonally below the 2-deck at A7-A8 and right above B10-C10.
        val result = initial.move(0, Coordinate(8, 1))
        assertEquals(PlacementResult.Rejected(ShipPlacement(4, Coordinate(8, 1), Orientation.HORIZONTAL).cells()), result)
    }

    @Test
    fun movingOntoTheShipsOwnOldCellsIsAllowed() {
        val moved = initial.move(0, Coordinate(0, 0)).layout()
        assertEquals(Coordinate(0, 0), moved.ships[0].origin)
    }

    @Test
    fun rotatingInFreeSpaceTurnsTheShipAroundItsTopLeftEnd() {
        val layout = initial.move(0, Coordinate(4, 3)).layout()
        val rotated = layout.rotate(0).layout()
        assertEquals(ShipPlacement(4, Coordinate(4, 3), Orientation.VERTICAL), rotated.ships[0])
    }

    @Test
    fun rotatingIntoAnotherShipIsRejected() {
        // The 4-deck at B1-E1 would run down to B4, touching the 3-deck at A3-A5.
        val result = initial.rotate(0)
        assertTrue(result is PlacementResult.Rejected)
    }

    @Test
    fun rotatingNearTheBottomEdgeIsRejected() {
        val nearBottom = FleetLayout(listOf(ShipPlacement(3, Coordinate(8, 4), Orientation.HORIZONTAL)))
        assertEquals(PlacementResult.Rejected(listOf(Coordinate(8, 4), Coordinate(9, 4))), nearBottom.rotate(0))
    }

    @Test
    fun rotatingNearTheRightEdgeIsRejected() {
        val nearRight = FleetLayout(listOf(ShipPlacement(3, Coordinate(4, 9), Orientation.VERTICAL)))
        assertEquals(PlacementResult.Rejected(listOf(Coordinate(4, 9))), nearRight.rotate(0))
    }

    @Test
    fun rotatingASingleDeckShipKeepsItsCell() {
        val rotated = initial.rotate(6).layout()
        assertEquals(initial.ships[6].cells(), rotated.ships[6].cells())
    }

    @Test
    fun randomLayoutsAreAlwaysValidAndHoldTheWholeFleet() {
        val random = Random(42)
        repeat(500) {
            val layout = FleetLayout.random(random)
            assertEquals(FLEET, layout.ships.map { it.size })
            assertTrue(layout.isValid())
        }
    }

    @Test
    fun randomLayoutUsesBothDirections() {
        val random = Random(7)
        val orientations = (1..50).flatMap { FleetLayout.random(random).ships.filter { it.size > 1 } }
            .map { it.orientation }.toSet()
        assertEquals(Orientation.entries.toSet(), orientations)
    }
}
