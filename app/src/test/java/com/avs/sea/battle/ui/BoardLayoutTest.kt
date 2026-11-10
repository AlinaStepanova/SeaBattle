package com.avs.sea.battle.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardLayoutTest {

    @Test
    fun boardFillsWidthMinusLabelColumnWhenHeightIsUnbounded() {
        assertEquals(280.dp, boardSideFor(maxWidth = 300.dp, maxHeight = Dp.Infinity))
    }

    @Test
    fun boardFillsWidthMinusLabelColumnWhenWidthIsTheTighterBound() {
        assertEquals(280.dp, boardSideFor(maxWidth = 300.dp, maxHeight = 500.dp))
    }

    @Test
    fun boardShrinksToFitHeightMinusLetterRowWhenHeightIsTheTighterBound() {
        assertEquals(230.dp, boardSideFor(maxWidth = 400.dp, maxHeight = 250.dp))
    }

    @Test
    fun boardSideIsNeverNegativeWhenSpaceIsSmallerThanLabels() {
        assertEquals(0.dp, boardSideFor(maxWidth = 10.dp, maxHeight = 10.dp))
    }

    @Test
    fun portraitDotsAreLargerThanLandscapeDots() {
        assertTrue(dotRadiusFor(80f, BoardStyle.Portrait) > dotRadiusFor(80f, BoardStyle.Landscape))
    }

    @Test
    fun dotsStayWellInsideTheCellInBothOrientations() {
        val cell = 80f
        listOf(BoardStyle.Portrait, BoardStyle.Landscape).forEach { style ->
            assertTrue(dotRadiusFor(cell, style) * 2 <= cell / 5)
        }
    }

    @Test
    fun dotScalesWithTheCell() {
        assertEquals(dotRadiusFor(40f, BoardStyle.Portrait) * 2, dotRadiusFor(80f, BoardStyle.Portrait), 0.001f)
    }

    @Test
    fun landscapeLabelsAreSmallerThanPortraitLabels() {
        assertTrue(BoardStyle.Landscape.labelFontSize.value < BoardStyle.Portrait.labelFontSize.value)
    }

    @Test
    fun hitCrossIsClearlyBolderThanAGridLine() {
        // On a Pixel 8a a portrait cell is about 81px; the grid line is 1px.
        assertTrue(hitStrokeWidth(81f) >= 5f)
    }

    @Test
    fun hitCrossScalesWithTheCell() {
        assertEquals(hitStrokeWidth(40f) * 2, hitStrokeWidth(80f), 0.001f)
    }

    @Test
    fun hitCrossSpansEightyPercentOfTheCellAndIsCentered() {
        val cell = 80f
        val (first, second) = hitCrossStrokes(cell)
        listOf(first, second).forEach { (start, end) ->
            assertEquals(cell * 0.8f, kotlin.math.abs(end.x - start.x), 0.001f)
            assertEquals(cell * 0.8f, kotlin.math.abs(end.y - start.y), 0.001f)
            assertEquals(cell / 2, (start.x + end.x) / 2, 0.001f)
            assertEquals(cell / 2, (start.y + end.y) / 2, 0.001f)
        }
    }

    @Test
    fun missDotsAreSmallPenDots() {
        val cell = 80f
        listOf(BoardStyle.Portrait, BoardStyle.Landscape).forEach { style ->
            assertTrue(dotRadiusFor(cell, style) * 2 <= cell * 0.14f)
        }
    }
}
