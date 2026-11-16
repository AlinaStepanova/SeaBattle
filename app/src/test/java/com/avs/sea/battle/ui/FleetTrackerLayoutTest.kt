package com.avs.sea.battle.ui

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FleetTrackerLayoutTest {

    @Test
    fun cellIsCappedOnWideRows() {
        assertEquals(12f, fleetTrackerCell(309.dp).value, 0.001f)
    }

    @Test
    fun cellShrinksToFitNarrowRows() {
        assertEquals(6f, fleetTrackerCell(147.dp).value, 0.001f)
    }

    // Round caps reach half the stroke width past each end, so the ends are inset by that much.
    @Test
    fun sunkStrikeStaysWithinTheShip() {
        val strike = sunkStrike(shipWidth = 40f, cell = 10f)
        assertEquals(5f, strike.start.y, 0.001f)
        assertEquals(5f, strike.end.y, 0.001f)
        assertEquals(0f, strike.start.x - strike.width / 2, 0.001f)
        assertEquals(40f, strike.end.x + strike.width / 2, 0.001f)
    }

    @Test
    fun sunkStrikeIsAThinPenLine() {
        val strike = sunkStrike(shipWidth = 40f, cell = 10f)
        assertTrue("strike is ${strike.width} on a 10 px cell", strike.width in 1.5f..2f)
    }

    @Test
    fun landscapeColumnCellIsCappedInWideMargins() {
        assertEquals(14f, landscapeFleetColumnCell(sideSpace = 129.dp, gridHeight = 276.dp).value, 0.001f)
    }

    // The side space of a 640 x 360 dp landscape phone.
    @Test
    fun landscapeColumnCellShrinksToFitNarrowMargins() {
        assertEquals(8.75f, landscapeFleetColumnCell(sideSpace = 51.dp, gridHeight = 225.dp).value, 0.01f)
    }

    // A column is 10 ships of one cell each plus 9 gaps of half a cell, and as wide as the
    // 4-deck plus 8 dp on either side.
    @Test
    fun landscapeColumnFitsBesideTheGrid() {
        listOf(51.dp to 225.dp, 129.dp to 276.dp, 200.dp to 120.dp, 60.dp to 400.dp).forEach { (side, grid) ->
            val cell = landscapeFleetColumnCell(side, grid)
            assertTrue("column of $cell is taller than $grid", cell * 14.5f <= grid + 0.01.dp)
            assertTrue("column of $cell is wider than $side", cell * 4 + 16.dp <= side + 0.01.dp)
        }
    }

    // A row is 20 ship cells plus 9 gaps of half a cell.
    @Test
    fun rowNeverOverflows() {
        listOf(100.dp, 147.dp, 245.dp, 400.dp).forEach { width ->
            assertTrue("row of width $width", fleetTrackerCell(width) * 24.5f <= width + 0.01.dp)
        }
    }
}
