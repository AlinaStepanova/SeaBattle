package com.avs.sea.battle.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.avs.sea.battle.R
import com.avs.sea.battle.ui.theme.SeaBattleTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenLandscapeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            val landscape = Configuration(LocalConfiguration.current).apply {
                orientation = Configuration.ORIENTATION_LANDSCAPE
            }
            CompositionLocalProvider(LocalConfiguration provides landscape) {
                SeaBattleTheme {
                    // Pixel 8a landscape window size
                    Box(Modifier.requiredSize(width = 914.dp, height = 411.dp)) {
                        GameScreen(
                            uiState = GameUiState(),
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

    @Test
    fun menuButtonDoesNotCoverBoardGrids() {
        val grids = composeTestRule.onAllNodesWithTag(BOARD_GRID_TAG).fetchSemanticsNodes()
        assertEquals(2, grids.size)
        grids.indices.forEach { index ->
            val grid = composeTestRule.onAllNodesWithTag(BOARD_GRID_TAG)[index].getBoundsInRoot()
            assertFalse("menu overlaps board grid $index", menuBounds().overlaps(grid))
        }
    }

    @Test
    fun menuButtonDoesNotCoverColumnLabels() {
        val labels = composeTestRule.onAllNodesWithText("J")
        val count = labels.fetchSemanticsNodes().size
        assertEquals(2, count)
        repeat(count) { index ->
            assertFalse(
                "menu overlaps column label J of board $index",
                menuBounds().overlaps(labels[index].getBoundsInRoot())
            )
        }
    }

    private fun menuBounds(): DpRect {
        val description = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.more_settings)
        return composeTestRule.onNodeWithContentDescription(description).getBoundsInRoot()
    }

    private fun DpRect.overlaps(other: DpRect): Boolean =
        left < other.right && other.left < right && top < other.bottom && other.top < bottom
}
