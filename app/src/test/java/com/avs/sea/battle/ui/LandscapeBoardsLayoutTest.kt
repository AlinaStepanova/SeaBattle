package com.avs.sea.battle.ui

import androidx.compose.ui.unit.dp
import com.avs.sea.battle.SQUARES_COUNT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LandscapeBoardsLayoutTest {

    @Test
    fun boardsFillTheHeightOnAWideScreen() {
        val layout = landscapeBoardsLayout(maxWidth = 800.dp, maxHeight = 280.dp)
        assertEquals(280.dp, layout.boardSize)
    }

    @Test
    fun boardsShrinkToFitTheWidthOnANarrowScreen() {
        val layout = landscapeBoardsLayout(maxWidth = 440.dp, maxHeight = 280.dp)
        assertTrue(layout.boardSize < 280.dp)
        assertEquals(440f, (layout.boardSize * 2 + layout.gap).value, 0.01f)
    }

    @Test
    fun gridsAreExactlyTwoSquaresApartOnAnyScreen() {
        val screens = listOf(800.dp to 280.dp, 440.dp to 280.dp, 560.dp to 200.dp, 1200.dp to 600.dp)
        screens.forEach { (width, height) ->
            val layout = landscapeBoardsLayout(width, height)
            val cell = (layout.boardSize - LABEL_SIZE) / SQUARES_COUNT
            // The computer board's number column sits between the grids, inside the gap.
            assertEquals(2 * cell.value, (layout.gap + LABEL_SIZE).value, 0.01f)
        }
    }

    @Test
    fun boardsAndGapNeverOverflowTheAvailableSpace() {
        val screens = listOf(800.dp to 280.dp, 440.dp to 280.dp, 300.dp to 400.dp, 560.dp to 200.dp)
        screens.forEach { (width, height) ->
            val layout = landscapeBoardsLayout(width, height)
            assertTrue(layout.boardSize * 2 + layout.gap <= width + 0.01.dp)
            assertTrue(layout.boardSize <= height)
        }
    }

    @Test
    fun gridsAreClearlySeparated() {
        val layout = landscapeBoardsLayout(maxWidth = 800.dp, maxHeight = 280.dp)
        val grid = layout.boardSize - LABEL_SIZE
        assertTrue(layout.gap + LABEL_SIZE >= grid * 0.15f)
    }
}
