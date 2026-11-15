package com.avs.sea.battle.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.avs.sea.battle.R
import com.avs.sea.battle.ui.theme.SeaBattleTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FleetTrackerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aSunkShipIsStruckThrough() {
        setTracker(fleetShips(listOf(3)))
        assertSunk(1, true)
        assertSunk(0, false)
        assertSunk(2, false)
    }

    @Test
    fun shipsKeepTheirLengths() {
        setTracker(fleetShips(emptyList()))
        val fourDeck = composeTestRule.onNodeWithTag(fleetShipTag(0)).getBoundsInRoot()
        val oneDeck = composeTestRule.onNodeWithTag(fleetShipTag(9)).getBoundsInRoot()
        // Each ship's width is rounded to whole pixels on its own.
        assertEquals(4 * (oneDeck.right - oneDeck.left).value, (fourDeck.right - fourDeck.left).value, 1f)
    }

    @Test
    fun rowReadsTheAfloatCount() {
        setTracker(fleetShips(listOf(3)))
        val expected = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.fleet_opponent_afloat, 9, 10)
        composeTestRule.onNodeWithTag(fleetTrackerTag(isOpponent = true)).assertContentDescriptionEquals(expected)
    }

    @Test
    fun emptyFleetDrawsNothing() {
        setTracker(emptyList())
        assertEquals(0, composeTestRule.onAllNodes(hasTestTag(fleetShipTag(0))).fetchSemanticsNodes().size)
    }

    private fun assertSunk(index: Int, sunk: Boolean) {
        composeTestRule.onNodeWithTag(fleetShipTag(index)).assert(SemanticsMatcher.expectValue(SunkShipKey, sunk))
    }

    private fun setTracker(ships: List<FleetShipUi>) {
        composeTestRule.setContent {
            SeaBattleTheme {
                Box(Modifier.width(300.dp)) {
                    FleetTracker(ships, isOpponent = true, alignment = Alignment.Start)
                }
            }
        }
    }
}
