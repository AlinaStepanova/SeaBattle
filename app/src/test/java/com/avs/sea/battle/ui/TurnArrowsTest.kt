package com.avs.sea.battle.ui

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.hypot

class TurnArrowsTest {

    @Test
    fun activeArrowPointsAtTheBoardBeingShotAt() {
        assertEquals(ArrowDirection.DOWN, activeArrowDirection(GamePhase.BATTLE, isLandscape = false))
        assertEquals(ArrowDirection.UP, activeArrowDirection(GamePhase.COMPUTER_TURN, isLandscape = false))
        assertEquals(ArrowDirection.RIGHT, activeArrowDirection(GamePhase.BATTLE, isLandscape = true))
        assertEquals(ArrowDirection.LEFT, activeArrowDirection(GamePhase.COMPUTER_TURN, isLandscape = true))
        listOf(false, true).forEach { isLandscape ->
            assertNull(activeArrowDirection(GamePhase.PLACING, isLandscape))
            assertNull(activeArrowDirection(GamePhase.OVER, isLandscape))
        }
    }

    @Test
    fun shaftIsCentredAndSpansEightyPercent() {
        assertStroke(Offset(50f, 10f), Offset(50f, 90f), turnArrowStrokes(100f, ArrowDirection.DOWN)[0])
        assertStroke(Offset(10f, 50f), Offset(90f, 50f), turnArrowStrokes(100f, ArrowDirection.RIGHT)[0])
    }

    @Test
    fun headMeetsAtTheTipOnTheSideTheArrowPoints() {
        val tips = mapOf(
            ArrowDirection.DOWN to Offset(50f, 90f),
            ArrowDirection.UP to Offset(50f, 10f),
            ArrowDirection.RIGHT to Offset(90f, 50f),
            ArrowDirection.LEFT to Offset(10f, 50f),
        )
        tips.forEach { (direction, tip) ->
            val (shaft, headLeft, headRight) = turnArrowStrokes(100f, direction)
            assertOffset(tip, shaft.second)
            assertOffset(tip, headLeft.first)
            assertOffset(tip, headRight.first)
        }
    }

    @Test
    fun headStrokesAreMirroredAt45Degrees() {
        ArrowDirection.entries.forEach { direction ->
            val (shaft, headLeft, headRight) = turnArrowStrokes(100f, direction)
            listOf(headLeft, headRight).forEach { (tip, end) ->
                assertEquals("$direction", 30f, hypot(end.x - tip.x, end.y - tip.y), 0.01f)
                assertEquals("$direction", abs(end.x - tip.x), abs(end.y - tip.y), 0.01f)
                // The head runs back along the shaft, not past the tip.
                val back = (end.x - tip.x) * (shaft.first.x - tip.x) + (end.y - tip.y) * (shaft.first.y - tip.y)
                assertTrue("$direction", back > 0f)
            }
            val leftEnd = headLeft.second
            val rightEnd = headRight.second
            if (direction == ArrowDirection.UP || direction == ArrowDirection.DOWN) {
                assertEquals("$direction", 50f, (leftEnd.x + rightEnd.x) / 2, 0.01f)
                assertEquals("$direction", leftEnd.y, rightEnd.y, 0.01f)
            } else {
                assertEquals("$direction", 50f, (leftEnd.y + rightEnd.y) / 2, 0.01f)
                assertEquals("$direction", leftEnd.x, rightEnd.x, 0.01f)
            }
        }
    }

    private fun assertStroke(start: Offset, end: Offset, stroke: Pair<Offset, Offset>) {
        assertOffset(start, stroke.first)
        assertOffset(end, stroke.second)
    }

    private fun assertOffset(expected: Offset, actual: Offset) {
        assertEquals("x of $actual", expected.x, actual.x, 0.01f)
        assertEquals("y of $actual", expected.y, actual.y, 0.01f)
    }
}
