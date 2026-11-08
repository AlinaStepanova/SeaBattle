package com.avs.sea.battle.ui

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.ui.theme.SeaBattleTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LabeledBoardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rowNumbersLineUpWithBoardRowsOnAWideBoard() {
        setBoard(width = 320.dp)
        val board = boardBounds()
        assertCenteredVertically("1", board.top, board)
        assertCenteredVertically("10", board.top + cellSize(board) * (SQUARES_COUNT - 1), board)
    }

    @Test
    fun columnLettersLineUpWithBoardColumnsOnAWideBoard() {
        setBoard(width = 320.dp)
        val board = boardBounds()
        assertCenteredHorizontally("A", board.left, board)
        assertCenteredHorizontally("J", board.left + cellSize(board) * (SQUARES_COUNT - 1), board)
    }

    @Test
    fun rowNumbersLineUpWithBoardRowsOnANarrowBoard() {
        setBoard(width = 180.dp)
        val board = boardBounds()
        assertCenteredVertically("10", board.top + cellSize(board) * (SQUARES_COUNT - 1), board)
    }

    private fun setBoard(width: Dp) {
        composeTestRule.setContent {
            SeaBattleTheme {
                LabeledBoard(
                    ships = emptyList(),
                    crosses = emptyList(),
                    dots = emptyList(),
                    selected = null,
                    onCellTap = null,
                    modifier = Modifier.width(width),
                )
            }
        }
    }

    private fun boardBounds(): Rect = with(composeTestRule.density) {
        val bounds = composeTestRule.onNodeWithTag(BOARD_GRID_TAG).getBoundsInRoot()
        Rect(bounds.left.toPx(), bounds.top.toPx(), bounds.right.toPx(), bounds.bottom.toPx())
    }

    private fun labelBounds(text: String): Rect = with(composeTestRule.density) {
        val bounds = composeTestRule.onNodeWithText(text).getBoundsInRoot()
        Rect(bounds.left.toPx(), bounds.top.toPx(), bounds.right.toPx(), bounds.bottom.toPx())
    }

    private fun cellSize(board: Rect) = board.height / SQUARES_COUNT

    private fun assertCenteredVertically(label: String, cellTop: Float, board: Rect) {
        val expected = cellTop + cellSize(board) / 2
        assertEquals("row label $label", expected, labelBounds(label).center.y, TOLERANCE_PX)
    }

    private fun assertCenteredHorizontally(label: String, cellLeft: Float, board: Rect) {
        val expected = cellLeft + cellSize(board) / 2
        assertEquals("column label $label", expected, labelBounds(label).center.x, TOLERANCE_PX)
    }

    private companion object {
        const val TOLERANCE_PX = 2f
    }
}
