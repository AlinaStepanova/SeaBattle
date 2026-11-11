package com.avs.sea.battle.ui

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlacementBoardSizeTest {

    @Test
    fun portraitBoardMatchesTheWidthOfTheBattleBoards() {
        val size = placementBoardSize(maxWidth = 411.dp, maxHeight = 750.dp)
        assertEquals(portraitBoardsLayout(411.dp, 750.dp).boardSize.value, size.value, 0.01f)
    }

    @Test
    fun landscapeBoardIsLimitedByTheHeight() {
        assertEquals(300f, placementBoardSize(maxWidth = 800.dp, maxHeight = 300.dp).value, 0.01f)
    }

    @Test
    fun boardAlwaysFitsTheAvailableSpace() {
        listOf(411.dp to 750.dp, 800.dp to 300.dp, 320.dp to 200.dp, 200.dp to 600.dp).forEach { (width, height) ->
            val size = placementBoardSize(width, height)
            assertTrue("$width x $height", size <= width && size <= height)
        }
    }

    @Test
    fun boardIsNotDrawnWhenThereIsNoRoomForTheGrid() {
        assertEquals(0f, placementBoardSize(maxWidth = 20.dp, maxHeight = 400.dp).value, 0.01f)
    }
}
