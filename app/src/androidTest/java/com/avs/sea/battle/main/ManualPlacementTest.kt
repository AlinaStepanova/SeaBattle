package com.avs.sea.battle.main

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avs.sea.battle.R
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.battle_field.FleetLayout
import com.avs.sea.battle.battle_field.ShipPlacement
import com.avs.sea.battle.ships.Orientation
import com.avs.sea.battle.ui.BOARD_GRID_TAG
import com.avs.sea.battle.ui.GamePhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManualPlacementTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val viewModel: MainViewModel
        get() = ViewModelProvider(composeTestRule.activity)[MainViewModel::class.java]

    private val state get() = viewModel.uiState.value

    @Test
    fun tappingMovesAndRotatesTheSelectedShip() {
        tapCell(Coordinate(4, 3))
        assertEquals(ShipPlacement(4, Coordinate(4, 3), Orientation.HORIZONTAL).cells(), state.selectedShipCells)
        tapCell(Coordinate(4, 4))
        assertEquals(ShipPlacement(4, Coordinate(4, 3), Orientation.VERTICAL).cells(), state.selectedShipCells)
    }

    @Test
    fun blockedMoveIsRefused() {
        tapCell(Coordinate(5, 8))
        assertEquals(FleetLayout.initial().cells(), state.personBoardShips)
        assertTrue(state.rejectedCells.isNotEmpty())
    }

    @Test
    fun startedBattleUsesTheArrangedFleet() {
        tapCell(Coordinate(4, 3))
        val arranged = state.personBoardShips.toSet()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.start_text)).performClick()
        composeTestRule.waitForIdle()
        assertEquals(GamePhase.BATTLE, state.phase)
        assertEquals(arranged, state.personBoardShips.toSet())
    }

    @Test
    fun newGameFromTheMenuMidBattleAsksFirst() {
        val activity = composeTestRule.activity
        composeTestRule.onNodeWithText(activity.getString(R.string.start_text)).performClick()
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.more_settings)).performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.new_game_text)).performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.dialog_yes)).performClick()
        composeTestRule.waitForIdle()
        assertEquals(GamePhase.PLACING, state.phase)
        assertEquals(FleetLayout.initial().cells(), state.personBoardShips)
    }

    private fun tapCell(coordinate: Coordinate) {
        composeTestRule.onNodeWithTag(BOARD_GRID_TAG).performTouchInput {
            val cell = width.toFloat() / SQUARES_COUNT
            click(Offset((coordinate.y + 0.5f) * cell, (coordinate.x + 0.5f) * cell))
        }
        composeTestRule.waitForIdle()
    }
}
