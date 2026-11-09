package com.avs.sea.battle.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.avs.sea.battle.battle_field.Coordinate
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.avs.sea.battle.R
import com.avs.sea.battle.ui.theme.SeaBattleTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun landscapeGapBetweenBoardsIsProportionalToBoardSize() {
        setGameScreen(isLandscape = true)
        assertGapIsProportional()
    }

    @Test
    fun smallPhoneLandscapeGapBetweenBoardsIsProportionalToBoardSize() {
        setGameScreen(isLandscape = true, width = SMALL_PHONE_LONG_SIDE, height = SMALL_PHONE_SHORT_SIDE)
        assertGapIsProportional()
    }

    @Test
    fun smallPhoneLandscapeBoardsFitOnScreen() {
        setGameScreen(isLandscape = true, width = SMALL_PHONE_LONG_SIDE, height = SMALL_PHONE_SHORT_SIDE)
        val screen = composeTestRule.onNodeWithTag(SCREEN_TAG).getBoundsInRoot()
        assertTrue("person board starts off screen", grid(PERSON).left >= screen.left)
        assertTrue("computer board ends off screen", grid(COMPUTER).right <= screen.right)
    }

    @Test
    fun landscapeBoardLabelsUseLandscapeSize() {
        setGameScreen(isLandscape = true)
        assertEquals(BoardStyle.Landscape.labelFontSize, labelFontSize("J"))
    }

    @Test
    fun portraitBoardLabelsUsePortraitSize() {
        setGameScreen(isLandscape = false)
        assertEquals(BoardStyle.Portrait.labelFontSize, labelFontSize("J"))
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
    fun actionButtonsFitNarrowScreenWithLargeFont() {
        setGameScreen(
            isLandscape = false,
            uiState = GameUiState(personBoardShips = listOf(Coordinate(0, 0))),
            width = NARROW_PHONE_SHORT_SIDE,
            height = NARROW_PHONE_LONG_SIDE,
            fontScale = 2f,
        )
        val screen = composeTestRule.onNodeWithTag(SCREEN_TAG).getBoundsInRoot()
        val generate = generateButton()
        val start = composeTestRule.onNodeWithText(string(R.string.start_text)).getBoundsInRoot()
        listOf("generate" to generate, "start" to start).forEach { (name, button) ->
            assertTrue("$name button leaves the screen sideways", button.left >= screen.left && button.right <= screen.right)
            assertTrue("$name button overlaps the computer board", button.top >= grid(COMPUTER).bottom)
            assertTrue("$name button leaves the screen at the bottom", button.bottom <= screen.bottom)
        }
        assertTrue("buttons overlap", generate.right <= start.left || start.right <= generate.left)
        assertFalse("generate text is cut off", textIsCutOff(string(R.string.generate_ships_text)))
        assertFalse("start text is cut off", textIsCutOff(string(R.string.start_text)))
    }

    @Test
    fun computerTurnSpinnerIsSmall() {
        setGameScreen(isLandscape = false, uiState = GameUiState(phase = GamePhase.COMPUTER_TURN))
        val spinner = composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .getBoundsInRoot()
        assertTrue("spinner size was ${spinner.width}", spinner.width <= 20.dp)
    }

    // The gap between grids also holds the computer board's number column.
    private fun assertGapIsProportional() {
        val gridWidth = grid(PERSON).width
        val gap = grid(COMPUTER).left - grid(PERSON).right
        assertTrue("gap $gap too small for grid $gridWidth", gap >= gridWidth * 0.15f)
        assertTrue("gap $gap too large for grid $gridWidth", gap <= gridWidth * 0.35f)
    }

    private fun setGameScreen(
        isLandscape: Boolean,
        uiState: GameUiState = GameUiState(),
        width: Dp = if (isLandscape) PHONE_LONG_SIDE else PHONE_SHORT_SIDE,
        height: Dp = if (isLandscape) PHONE_SHORT_SIDE else PHONE_LONG_SIDE,
        fontScale: Float = 1f,
    ) {
        composeTestRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                orientation = if (isLandscape) {
                    Configuration.ORIENTATION_LANDSCAPE
                } else {
                    Configuration.ORIENTATION_PORTRAIT
                }
            }
            val density = Density(LocalDensity.current.density, fontScale)
            CompositionLocalProvider(LocalConfiguration provides configuration, LocalDensity provides density) {
                SeaBattleTheme {
                    Box(Modifier.requiredSize(width = width, height = height).testTag(SCREEN_TAG)) {
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

    private fun labelFontSize(text: String): TextUnit = textLayout(text).layoutInput.style.fontSize

    // Compares each line's own width, since centered text is laid out at the full available width.
    private fun textIsCutOff(text: String): Boolean {
        val layout = textLayout(text)
        val tooWide = (0 until layout.lineCount).any {
            layout.getLineRight(it) - layout.getLineLeft(it) > layout.size.width
        }
        return tooWide || layout.multiParagraph.height > layout.size.height
    }

    private fun textLayout(text: String): TextLayoutResult {
        val node = composeTestRule.onAllNodesWithText(text)[0].fetchSemanticsNode()
        val layouts = mutableListOf<TextLayoutResult>()
        node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
        return layouts.single()
    }

    private fun string(id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private val DpRect.width: Dp get() = right - left

    private companion object {
        const val PERSON = 0
        const val COMPUTER = 1
        const val SCREEN_TAG = "screen"

        // Pixel 8a window size
        val PHONE_SHORT_SIDE = 411.dp
        val PHONE_LONG_SIDE = 914.dp

        // A narrow phone, e.g. 320x640 dp
        val NARROW_PHONE_SHORT_SIDE = 320.dp
        val NARROW_PHONE_LONG_SIDE = 640.dp

        // A small phone, e.g. 5" at 640x360 dp
        val SMALL_PHONE_SHORT_SIDE = 360.dp
        val SMALL_PHONE_LONG_SIDE = 640.dp
    }
}
