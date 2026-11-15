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

    // A row is 20 ship cells plus 9 gaps of half a cell.
    @Test
    fun rowNeverOverflows() {
        listOf(100.dp, 147.dp, 245.dp, 400.dp).forEach { width ->
            assertTrue("row of width $width", fleetTrackerCell(width) * 24.5f <= width + 0.01.dp)
        }
    }
}
