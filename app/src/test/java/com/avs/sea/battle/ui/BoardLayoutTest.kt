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
    fun dotIsSmallComparedToTheCell() {
        val cell = 80f
        assertTrue(dotRadiusFor(cell) * 2 <= cell / 8)
    }

    @Test
    fun dotScalesWithTheCell() {
        assertEquals(dotRadiusFor(40f) * 2, dotRadiusFor(80f), 0.001f)
    }
}
