package com.avs.sea.battle.ui

import androidx.compose.ui.unit.dp
import com.avs.sea.battle.SQUARES_COUNT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PortraitBoardsLayoutTest {

    // Includes compact areas (split screen, small windows) where a cell is smaller than the label row.
    private val screens = listOf(
        411.dp to 750.dp, 360.dp to 560.dp, 320.dp to 460.dp, 600.dp to 900.dp, 411.dp to 500.dp,
        320.dp to 400.dp, 411.dp to 300.dp, 300.dp to 200.dp,
    )

    @Test
    fun boardsUseEightyPercentOfTheWidthOnATallScreen() {
        val layout = portraitBoardsLayout(maxWidth = 411.dp, maxHeight = 750.dp)
        assertEquals(411f * 0.8f, layout.boardSize.value, 0.01f)
    }

    @Test
    fun computerGridStartsAWholeNumberOfSquaresBelowThePlayersGrid() {
        screens.forEach { (width, height) ->
            val layout = portraitBoardsLayout(width, height)
            val cell = ((layout.boardSize - LABEL_SIZE) / SQUARES_COUNT).value
            val squaresBetweenGridTops = (layout.boardSize + layout.gap).value / cell
            assertEquals("screen $width x $height", Math.round(squaresBetweenGridTops).toFloat(), squaresBetweenGridTops, 0.01f)
        }
    }

    @Test
    fun boardsAndGapFitTheAvailableHeight() {
        screens.forEach { (width, height) ->
            val layout = portraitBoardsLayout(width, height)
            assertTrue("screen $width x $height", layout.boardSize * 2 + layout.gap <= height + 0.01.dp)
            assertTrue("screen $width x $height", layout.boardSize <= width)
        }
    }

    @Test
    fun gapIsNeverNegative() {
        screens.forEach { (width, height) ->
            assertTrue("screen $width x $height", portraitBoardsLayout(width, height).gap >= 0.dp)
        }
    }
}
