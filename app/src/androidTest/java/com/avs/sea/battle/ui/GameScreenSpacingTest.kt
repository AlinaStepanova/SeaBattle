package com.avs.sea.battle.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.avs.sea.battle.R
import com.avs.sea.battle.ui.theme.SeaBattleTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenSpacingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun portraitActionButtonSitsCloseBelowComputerBoard() {
        setGameScreen(isLandscape = false)
        val gap = generateButton().top - grid(COMPUTER).bottom
        assertTrue("gap below computer board was $gap", gap <= 48.dp)
    }

    @Test
    fun portraitBoardsAreAtLeastThreeQuartersOfScreenWidth() {
        setGameScreen(isLandscape = false)
        val width = grid(PERSON).width
        assertTrue("board width was $width", width >= PHONE_SHORT_SIDE * 0.75f - 1.dp)
    }

    @Test
    fun landscapeBoardsSitCloseToEachOther() {
        setGameScreen(isLandscape = true)
        val gap = grid(COMPUTER).left - grid(PERSON).right
        assertTrue("gap between boards was $gap", gap <= 80.dp)
    }

    @Test
    fun landscapeBoardsStartRightBelowStatus() {
        setGameScreen(isLandscape = true)
        val status = composeTestRule.onNodeWithText(string(R.string.status_welcome_text)).getBoundsInRoot()
        val gap = grid(PERSON).top - status.bottom
        assertTrue("gap between status and board was $gap", gap <= 26.dp)
    }

    @Test
    fun landscapeButtonsSitRightBelowBoards() {
        setGameScreen(isLandscape = true)
        val gap = generateButton().bottom - grid(PERSON).bottom
        assertTrue("space from board to button bottom was $gap", gap <= 48.dp)
    }

    @Test
    fun computerTurnSpinnerIsSmall() {
        setGameScreen(isLandscape = false, uiState = GameUiState(phase = GamePhase.COMPUTER_TURN))
        val spinner = composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .getBoundsInRoot()
        assertTrue("spinner size was ${spinner.width}", spinner.width <= 20.dp)
    }

    private fun setGameScreen(isLandscape: Boolean, uiState: GameUiState = GameUiState()) {
        composeTestRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                orientation = if (isLandscape) {
                    Configuration.ORIENTATION_LANDSCAPE
                } else {
                    Configuration.ORIENTATION_PORTRAIT
                }
            }
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                SeaBattleTheme {
                    Box(
                        Modifier.requiredSize(
                            width = if (isLandscape) PHONE_LONG_SIDE else PHONE_SHORT_SIDE,
                            height = if (isLandscape) PHONE_SHORT_SIDE else PHONE_LONG_SIDE,
                        )
                    ) {
                        GameScreen(
                            uiState = uiState,
                            onGenerateShips = {},
                            onStartGame = {},
                            onNewGame = {},
                            onCellClick = {},
                            onFire = {},
                            onMenuAction = {},
                        )
                    }
                }
            }
        }
    }

    private fun grid(index: Int): DpRect =
        composeTestRule.onAllNodesWithTag(BOARD_GRID_TAG)[index].getBoundsInRoot()

    private fun generateButton(): DpRect =
        composeTestRule.onNodeWithText(string(R.string.generate_ships_text)).getBoundsInRoot()

    private fun string(id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private val DpRect.width: Dp get() = right - left

    private companion object {
        const val PERSON = 0
        const val COMPUTER = 1

        // Pixel 8a window size
        val PHONE_SHORT_SIDE = 411.dp
        val PHONE_LONG_SIDE = 914.dp
    }
}
