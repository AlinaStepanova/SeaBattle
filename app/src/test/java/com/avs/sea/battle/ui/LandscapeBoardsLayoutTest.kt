package com.avs.sea.battle.ui

import androidx.compose.ui.unit.dp
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
    fun gapIsTheSameShareOfTheBoardOnAnyScreen() {
        val screens = listOf(800.dp to 280.dp, 440.dp to 280.dp, 560.dp to 200.dp, 1200.dp to 600.dp)
        val ratios = screens.map { (width, height) ->
            val layout = landscapeBoardsLayout(width, height)
            layout.gap / layout.boardSize
        }
        ratios.forEach { assertEquals(ratios.first(), it, 0.001f) }
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
    fun boardsAreClearlySeparated() {
        val layout = landscapeBoardsLayout(maxWidth = 800.dp, maxHeight = 280.dp)
        assertTrue(layout.gap >= layout.boardSize * 0.15f)
    }
}
