package com.avs.sea.battle.main

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avs.sea.battle.R
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.ui.ArrowDirection
import com.avs.sea.battle.ui.BOARD_GRID_TAG
import com.avs.sea.battle.ui.GamePhase
import com.avs.sea.battle.ui.turnArrowTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OneTapFireTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val viewModel: MainViewModel
        get() = ViewModelProvider(composeTestRule.activity)[MainViewModel::class.java]

    private val state get() = viewModel.uiState.value

    @Before
    fun startTheBattle() {
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.start_text)).performClick()
        composeTestRule.waitForIdle()
    }

    // The computer answers a miss after a real-time delay, so only the lasting dot is checked here;
    // the turn switch is covered by MainViewModelTest and TurnIndicatorTest.
    @Test
    fun oneTapOnTheComputersBoardFires() {
        val water = openWater()
        tapComputerCell(water)
        assertTrue("no dot at $water", water in state.computerBoardDots)
    }

    @Test
    fun newGameDuringTheComputersTurnHidesTheArrows() {
        val activity = composeTestRule.activity
        tapComputerCell(openWater())
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.more_settings)).performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.new_game_text)).performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.dialog_yes)).performClick()
        composeTestRule.waitForIdle()
        assertEquals(GamePhase.PLACING, state.phase)
        ArrowDirection.entries.forEach { direction ->
            val arrows = composeTestRule.onAllNodes(hasTestTag(turnArrowTag(direction))).fetchSemanticsNodes()
            assertEquals("$direction arrow is shown", 0, arrows.size)
        }
    }

    private fun openWater(): Coordinate {
        val ships = viewModel.getComputerBattleField().getShipsCoordinates()
        return (0 until SQUARES_COUNT).flatMap { x -> (0 until SQUARES_COUNT).map { y -> Coordinate(x, y) } }
            .first { it !in ships }
    }

    private fun tapComputerCell(coordinate: Coordinate) {
        composeTestRule.onAllNodesWithTag(BOARD_GRID_TAG)[COMPUTER].performTouchInput {
            val cell = width.toFloat() / SQUARES_COUNT
            click(Offset((coordinate.y + 0.5f) * cell, (coordinate.x + 0.5f) * cell))
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        const val COMPUTER = 1
    }
}
