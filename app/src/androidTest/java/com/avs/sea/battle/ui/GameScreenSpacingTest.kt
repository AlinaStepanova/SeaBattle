package com.avs.sea.battle.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.avs.sea.battle.R
import com.avs.sea.battle.SQUARES_COUNT
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
        val gap = newGameButton().top - grid(COMPUTER).bottom
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
        val status = composeTestRule.onNodeWithText(string(GAME_OVER_STATUS)).getBoundsInRoot()
        val gap = grid(PERSON).top - status.bottom
        assertTrue("gap between status and board was $gap", gap <= 26.dp)
    }

    @Test
    fun landscapeButtonsSitRightBelowBoards() {
        setGameScreen(isLandscape = true)
        val gap = newGameButton().bottom - grid(PERSON).bottom
        assertTrue("space from board to button bottom was $gap", gap <= 48.dp)
    }

    @Test
    fun actionButtonsFitNarrowScreenWithLargeFont() {
        setGameScreen(
            isLandscape = false,
            uiState = PLACING_STATE,
            width = NARROW_PHONE_SHORT_SIDE,
            height = NARROW_PHONE_LONG_SIDE,
            fontScale = 2f,
        )
        val screen = composeTestRule.onNodeWithTag(SCREEN_TAG).getBoundsInRoot()
        val generate = generateButton()
        val start = composeTestRule.onNodeWithText(string(R.string.start_text)).getBoundsInRoot()
        listOf("generate" to generate, "start" to start).forEach { (name, button) ->
            assertTrue("$name button leaves the screen sideways", button.left >= screen.left && button.right <= screen.right)
            assertTrue("$name button overlaps the board", button.top >= grid(PERSON).bottom)
            assertTrue("$name button leaves the screen at the bottom", button.bottom <= screen.bottom)
        }
        assertTrue("buttons overlap", generate.bottom <= start.top)
        assertFalse("generate text is cut off", textIsCutOff(string(R.string.shuffle_ships_text)))
        assertFalse("start text is cut off", textIsCutOff(string(R.string.start_text)))
    }

    @Test
    fun portraitComputerGridSitsOnTheNotebookSquares() {
        setGameScreen(isLandscape = false)
        assertWholeSquares(grid(COMPUTER).top - grid(PERSON).top)
    }

    @Test
    fun smallPhonePortraitComputerGridSitsOnTheNotebookSquares() {
        setGameScreen(isLandscape = false, width = SMALL_PHONE_SHORT_SIDE, height = SMALL_PHONE_LONG_SIDE)
        assertWholeSquares(grid(COMPUTER).top - grid(PERSON).top)
    }

    @Test
    fun landscapeComputerGridSitsOnTheNotebookSquares() {
        setGameScreen(isLandscape = true)
        assertWholeSquares(grid(COMPUTER).left - grid(PERSON).left)
    }

    @Test
    fun smallPhoneLandscapeComputerGridSitsOnTheNotebookSquares() {
        setGameScreen(isLandscape = true, width = SMALL_PHONE_LONG_SIDE, height = SMALL_PHONE_SHORT_SIDE)
        assertWholeSquares(grid(COMPUTER).left - grid(PERSON).left)
    }

    @Test
    fun battleShowsNoActionButtons() {
        val screenState = mutableStateOf(GameUiState(phase = GamePhase.BATTLE, statusResId = null))
        setGameScreen(isLandscape = false, screenState = screenState)
        assertNoTextButtons()
        screenState.value = GameUiState(phase = GamePhase.COMPUTER_TURN, statusResId = null)
        composeTestRule.waitForIdle()
        assertNoTextButtons()
    }

    @Test
    fun portraitBoardsStayPutWhenTheGameEnds() = assertBoardsStayPutWhenTheGameEnds(isLandscape = false)

    @Test
    fun landscapeBoardsStayPutWhenTheGameEnds() = assertBoardsStayPutWhenTheGameEnds(isLandscape = true)

    @Test
    fun menuItemsAreAtLeastAsLargeAsTheStatusText() {
        setGameScreen(isLandscape = false)
        composeTestRule.onNodeWithContentDescription(string(R.string.more_settings)).performClick()
        val menuItem = textLayout(string(R.string.menu_share_text)).layoutInput.style.fontSize
        val status = textLayout(string(GAME_OVER_STATUS)).layoutInput.style.fontSize
        assertTrue("menu item is $menuItem, status is $status", menuItem.value >= status.value)
    }

    @Test
    fun placementShowsOnlyThePlayersBoard() {
        setGameScreen(isLandscape = false, uiState = GameUiState())
        assertEquals(1, composeTestRule.onAllNodesWithTag(BOARD_GRID_TAG).fetchSemanticsNodes().size)
    }

    @Test
    fun portraitPlacementBoardIsAsWideAsTheBattleBoards() {
        setGameScreen(isLandscape = false, uiState = GameUiState())
        val width = grid(PERSON).width
        assertTrue("board width was $width", width >= PHONE_SHORT_SIDE * 0.75f - 1.dp)
    }

    @Test
    fun landscapePlacementBoardAndButtonsFitOnScreen() {
        setGameScreen(isLandscape = true, uiState = PLACING_STATE, width = SMALL_PHONE_LONG_SIDE, height = SMALL_PHONE_SHORT_SIDE)
        val screen = composeTestRule.onNodeWithTag(SCREEN_TAG).getBoundsInRoot()
        val start = composeTestRule.onNodeWithText(string(R.string.start_text)).getBoundsInRoot()
        assertTrue("board leaves the screen", grid(PERSON).bottom <= screen.bottom)
        assertTrue("start button leaves the screen", start.bottom <= screen.bottom && start.right <= screen.right)
        assertTrue("generate button overlaps the board", generateButton().left >= grid(PERSON).right)
    }

    @Test
    fun portraitPlacementBoardIsCentredOnTheScreen() {
        // The test phone's status and navigation bars differ in height and would shift the centre.
        setGameScreen(isLandscape = false, uiState = PLACING_STATE, ignoreSystemBars = true)
        val screen = composeTestRule.onNodeWithTag(SCREEN_TAG).getBoundsInRoot()
        val offCentre = (labeledBoardTop() - screen.top) - (screen.bottom - grid(PERSON).bottom)
        assertTrue("board is off centre by $offCentre", kotlin.math.abs(offCentre.value) <= 1f)
    }

    @Test
    fun portraitPlacementHintIsCentredBetweenScreenTopAndBoard() {
        setGameScreen(isLandscape = false, uiState = PLACING_STATE, ignoreSystemBars = true)
        val screen = composeTestRule.onNodeWithTag(SCREEN_TAG).getBoundsInRoot()
        val hint = composeTestRule.onNodeWithText(string(PLACEMENT_HINT)).getBoundsInRoot()
        val offCentre = (hint.top - screen.top) - (labeledBoardTop() - hint.bottom)
        assertTrue("hint is off centre by $offCentre", kotlin.math.abs(offCentre.value) <= 1f)
    }

    @Test
    fun portraitPlacementHintIsLargerThanTheStatusText() {
        setGameScreen(isLandscape = false, uiState = PLACING_STATE)
        val portrait = labelFontSize(string(PLACEMENT_HINT))
        assertTrue("portrait hint was $portrait", portrait.value > 18f)
    }

    @Test
    fun landscapePlacementHintKeepsTheStatusSize() {
        setGameScreen(isLandscape = true, uiState = PLACING_STATE)
        assertEquals(18f, labelFontSize(string(PLACEMENT_HINT)).value, 0.01f)
    }

    @Test
    fun portraitPlacementHintFitsAboveTheBoardWithLargeFont() {
        setGameScreen(
            isLandscape = false,
            uiState = PLACING_STATE,
            width = NARROW_PHONE_SHORT_SIDE,
            height = NARROW_PHONE_LONG_SIDE,
            fontScale = 2f,
            ignoreSystemBars = true,
        )
        val screen = composeTestRule.onNodeWithTag(SCREEN_TAG).getBoundsInRoot()
        val hint = composeTestRule.onNodeWithText(string(PLACEMENT_HINT)).getBoundsInRoot()
        assertTrue("hint starts above the screen", hint.top >= screen.top)
        assertTrue("hint runs into the board", hint.bottom <= labeledBoardTop())
        assertFalse("hint text is cut off", textIsCutOff(string(PLACEMENT_HINT)))
    }

    @Test
    fun landscapePlacementButtonsAreStackedRightOfTheBoard() {
        setGameScreen(isLandscape = true, uiState = PLACING_STATE)
        val generate = generateButton()
        val start = composeTestRule.onNodeWithText(string(R.string.start_text)).getBoundsInRoot()
        assertTrue("generate button overlaps the board", generate.left > grid(PERSON).right)
        assertTrue("start is not below generate", start.top >= generate.bottom)
        assertEquals(generate.width.value, start.width.value, 1f)
    }

    @Test
    fun portraitPlacementButtonsAreStackedAndEquallyWideBelowTheBoard() {
        setGameScreen(isLandscape = false, uiState = PLACING_STATE)
        val generate = generateButton()
        val start = composeTestRule.onNodeWithText(string(R.string.start_text)).getBoundsInRoot()
        assertTrue("generate button overlaps the board", generate.top > grid(PERSON).bottom)
        assertTrue("start is not below generate", start.top >= generate.bottom)
        assertEquals(generate.width.value, start.width.value, 1f)
        assertEquals(generate.left.value, start.left.value, 1f)
    }

    @Test
    fun portraitPlacementButtonsAreLargerThanBattleButtons() {
        setGameScreen(isLandscape = false, uiState = PLACING_STATE)
        assertTrue(labelFontSize(string(R.string.start_text)).value > 20f)
    }

    @Test
    fun newGameConfirmationShowsBothChoices() {
        setGameScreen(isLandscape = false, uiState = GameUiState(phase = GamePhase.BATTLE, showNewGameConfirmation = true))
        composeTestRule.onNodeWithText(string(R.string.new_game_dialog_title)).assertExists()
        composeTestRule.onNodeWithText(string(R.string.dialog_yes)).assertExists()
        composeTestRule.onNodeWithText(string(R.string.dialog_no)).assertExists()
    }

    @Test
    fun menuOffersNewGame() {
        setGameScreen(isLandscape = false, uiState = GameUiState(phase = GamePhase.BATTLE))
        composeTestRule.onNodeWithContentDescription(string(R.string.more_settings)).performClick()
        composeTestRule.onNodeWithText(string(R.string.new_game_text)).assertExists()
    }

    private fun assertNoTextButtons() {
        val buttons = composeTestRule
            .onAllNodes(hasClickAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))
            .fetchSemanticsNodes()
        assertEquals(0, buttons.size)
    }

    // The game-over text and the New game button appear in slots the battle keeps free.
    private fun assertBoardsStayPutWhenTheGameEnds(isLandscape: Boolean) {
        val screenState = mutableStateOf(GameUiState(phase = GamePhase.BATTLE, statusResId = null))
        setGameScreen(isLandscape, screenState = screenState)
        val duringBattle = listOf(grid(PERSON), grid(COMPUTER))
        screenState.value = BATTLE_STATE
        composeTestRule.waitForIdle()
        listOf(grid(PERSON), grid(COMPUTER)).zip(duringBattle).forEach { (atGameOver, before) ->
            listOf(
                atGameOver.left - before.left,
                atGameOver.top - before.top,
                atGameOver.right - before.right,
                atGameOver.bottom - before.bottom,
            ).forEach { assertTrue("grid moved from $before to $atGameOver", kotlin.math.abs(it.value) <= 0.5f) }
        }
    }

    // One notebook square is one board cell; allow a pixel of rounding.
    private fun assertWholeSquares(distance: Dp) {
        val cell = grid(PERSON).width / SQUARES_COUNT
        val squares = distance / cell
        val offBy = cell * kotlin.math.abs(squares - kotlin.math.round(squares))
        assertTrue("grids are $squares squares apart", offBy <= 1.dp)
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
        uiState: GameUiState = BATTLE_STATE,
        width: Dp = if (isLandscape) PHONE_LONG_SIDE else PHONE_SHORT_SIDE,
        height: Dp = if (isLandscape) PHONE_SHORT_SIDE else PHONE_LONG_SIDE,
        fontScale: Float = 1f,
        ignoreSystemBars: Boolean = false,
        // Lets a test change the state after the screen is shown.
        screenState: State<GameUiState> = mutableStateOf(uiState),
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
                    val systemBars = if (ignoreSystemBars) {
                        Modifier.consumeWindowInsets(WindowInsets.safeDrawing)
                    } else {
                        Modifier
                    }
                    Box(Modifier.requiredSize(width = width, height = height).then(systemBars).testTag(SCREEN_TAG)) {
                        GameScreen(
                            uiState = screenState.value,
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
    }

    private fun grid(index: Int): DpRect =
        composeTestRule.onAllNodesWithTag(BOARD_GRID_TAG)[index].getBoundsInRoot()

    // The letter row above the grid belongs to the board.
    private fun labeledBoardTop(): Dp = grid(PERSON).top - LABEL_SIZE

    private fun newGameButton(): DpRect =
        composeTestRule.onAllNodesWithText(string(R.string.new_game_text))[0].getBoundsInRoot()

    private fun generateButton(): DpRect =
        composeTestRule.onNodeWithText(string(R.string.shuffle_ships_text)).getBoundsInRoot()

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

        // Game over shows both boards with a single action button below them.
        const val GAME_OVER_STATUS = R.string.status_game_over_you_win_text
        const val PLACEMENT_HINT = R.string.status_place_ships_text
        val BATTLE_STATE = GameUiState(phase = GamePhase.OVER, statusResId = GAME_OVER_STATUS)

        val PLACING_STATE = GameUiState()

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
