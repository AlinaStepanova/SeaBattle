package com.avs.sea.battle.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.avs.sea.battle.R
import com.avs.sea.battle.ui.theme.SeaBattleTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TurnIndicatorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun portraitYourTurnLightsTheArrowPointingDown() {
        setScreen(isLandscape = false, phase = GamePhase.BATTLE)
        assertActive(ArrowDirection.DOWN, inactive = ArrowDirection.UP)
    }

    @Test
    fun portraitComputersTurnLightsTheArrowPointingUp() {
        setScreen(isLandscape = false, phase = GamePhase.COMPUTER_TURN)
        assertActive(ArrowDirection.UP, inactive = ArrowDirection.DOWN)
    }

    @Test
    fun landscapeYourTurnLightsTheArrowPointingRight() {
        setScreen(isLandscape = true, phase = GamePhase.BATTLE)
        assertActive(ArrowDirection.RIGHT, inactive = ArrowDirection.LEFT)
    }

    @Test
    fun landscapeComputersTurnLightsTheArrowPointingLeft() {
        setScreen(isLandscape = true, phase = GamePhase.COMPUTER_TURN)
        assertActive(ArrowDirection.LEFT, inactive = ArrowDirection.RIGHT)
    }

    @Test
    fun aMissHandsTheArrowToThePlayersBoard() {
        val state = setScreen(isLandscape = false, phase = GamePhase.BATTLE)
        state.value = battleState(GamePhase.COMPUTER_TURN)
        composeTestRule.waitForIdle()
        assertActive(ArrowDirection.UP, inactive = ArrowDirection.DOWN)
        composeTestRule.onNodeWithTag(TURN_INDICATOR_TAG).assertContentDescriptionEquals(string(R.string.turn_opponent_text))
    }

    // The computer's board starts at its letter row (portrait) or number column (landscape).
    @Test
    fun portraitArrowsKeepClearOfBothBoards() {
        setScreen(isLandscape = false, phase = GamePhase.BATTLE)
        listOf(ArrowDirection.DOWN, ArrowDirection.UP).forEach { direction ->
            val arrow = arrowBounds(direction)
            assertTrue("$direction arrow is too close to your grid", arrow.top - grid(PERSON).bottom >= CLEARANCE)
            assertTrue("$direction arrow is too close to the computer's board", grid(COMPUTER).top - LABEL_SIZE - arrow.bottom >= CLEARANCE)
        }
    }

    @Test
    fun landscapeArrowsKeepClearOfBothBoards() {
        setScreen(isLandscape = true, phase = GamePhase.BATTLE)
        listOf(ArrowDirection.RIGHT, ArrowDirection.LEFT).forEach { direction ->
            val arrow = arrowBounds(direction)
            assertTrue("$direction arrow is too close to your grid", arrow.left - grid(PERSON).right >= CLEARANCE)
            assertTrue("$direction arrow is too close to the computer's board", grid(COMPUTER).left - LABEL_SIZE - arrow.right >= CLEARANCE)
        }
    }

    @Test
    fun arrowsAreHiddenDuringPlacementAndAtGameOver() {
        val state = setScreen(isLandscape = false, phase = GamePhase.PLACING)
        assertNoArrows()
        state.value = GameUiState(phase = GamePhase.OVER, statusResId = R.string.status_game_over_you_lose_text)
        composeTestRule.waitForIdle()
        assertNoArrows()
    }

    @Test
    fun turnIndicatorIsAPoliteLiveRegion() {
        val state = setScreen(isLandscape = false, phase = GamePhase.BATTLE)
        val indicator = composeTestRule.onNodeWithTag(TURN_INDICATOR_TAG)
        indicator.assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        indicator.assertContentDescriptionEquals(string(R.string.turn_yours_text))
        state.value = battleState(GamePhase.COMPUTER_TURN)
        composeTestRule.waitForIdle()
        indicator.assertContentDescriptionEquals(string(R.string.turn_opponent_text))
    }

    private fun assertActive(active: ArrowDirection, inactive: ArrowDirection) {
        composeTestRule.onNodeWithTag(turnArrowTag(active)).assertIsSelected()
        composeTestRule.onNodeWithTag(turnArrowTag(inactive)).assertIsNotSelected()
    }

    private fun assertNoArrows() {
        ArrowDirection.entries.forEach { direction ->
            val arrows = composeTestRule.onAllNodes(hasTestTag(turnArrowTag(direction))).fetchSemanticsNodes()
            assertEquals("$direction arrow is shown", 0, arrows.size)
        }
    }

    private fun battleState(phase: GamePhase) = GameUiState(phase = phase, statusResId = null)

    private fun setScreen(isLandscape: Boolean, phase: GamePhase): MutableState<GameUiState> {
        val state = mutableStateOf(
            if (phase == GamePhase.PLACING) GameUiState() else battleState(phase)
        )
        composeTestRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                orientation = if (isLandscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT
            }
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                SeaBattleTheme {
                    // Pixel 8a window size
                    val (width, height) = if (isLandscape) 914.dp to 411.dp else 411.dp to 914.dp
                    Box(Modifier.requiredSize(width = width, height = height)) {
                        GameScreen(
                            uiState = state.value,
                            onGenerateShips = {},
                            onStartGame = {},
                            onNewGame = {},
                            onComputerCellClick = {},
                            onMenuAction = {},
                            onPersonCellClick = {},
                            onConfirmNewGame = {},
                            onDismissNewGame = {},
                        )
                    }
                }
            }
        }
        return state
    }

    private fun arrowBounds(direction: ArrowDirection): DpRect =
        composeTestRule.onNodeWithTag(turnArrowTag(direction)).getBoundsInRoot()

    private fun grid(index: Int): DpRect =
        composeTestRule.onAllNodesWithTag(BOARD_GRID_TAG)[index].getBoundsInRoot()

    private fun string(id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private companion object {
        const val PERSON = 0
        const val COMPUTER = 1
        val CLEARANCE = 7.5.dp
    }
}
