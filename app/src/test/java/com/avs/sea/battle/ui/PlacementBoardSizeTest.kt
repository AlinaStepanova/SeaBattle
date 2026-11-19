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
    fun portraitBoardKeepsTheBattleBoardWidthWhenTheControlsAreShort() {
        val size = portraitPlacementBoardSize(maxWidth = 411.dp, maxHeight = 840.dp, controlsHeight = 110.dp)
        assertEquals(placementBoardSize(411.dp, 840.dp).value, size.value, 0.01f)
    }

    @Test
    fun tallControlsShrinkThePortraitBoardSoBothFitAboveAndBelowIt() {
        val controls = 200.dp
        val size = portraitPlacementBoardSize(maxWidth = 320.dp, maxHeight = 568.dp, controlsHeight = controls)
        assertTrue("board $size", size < placementBoardSize(320.dp, 568.dp))
        assertTrue("board $size leaves no room for the controls", size + (controls + 32.dp) * 2 <= 568.dp)
    }

    @Test
    fun portraitBoardIsNotDrawnWhenTheControlsFillTheScreen() {
        assertEquals(0f, portraitPlacementBoardSize(maxWidth = 411.dp, maxHeight = 400.dp, controlsHeight = 200.dp).value, 0.01f)
    }

    @Test
    fun boardIsNotDrawnWhenThereIsNoRoomForTheGrid() {
        assertEquals(0f, placementBoardSize(maxWidth = 20.dp, maxHeight = 400.dp).value, 0.01f)
    }
}
