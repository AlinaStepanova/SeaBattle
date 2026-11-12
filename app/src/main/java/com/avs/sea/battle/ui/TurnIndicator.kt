package com.avs.sea.battle.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

// The board layouts keep at least this much room between the grids for an arrow.
internal val TURN_ARROW_SIZE = 28.dp

internal enum class ArrowDirection { UP, DOWN, LEFT, RIGHT }

// The active arrow points at the board being shot at: the computer's board (below in portrait,
// to the right in landscape) on the player's turn, the player's own board on the computer's turn.
internal fun activeArrowDirection(phase: GamePhase, isLandscape: Boolean): ArrowDirection? = when (phase) {
    GamePhase.BATTLE -> if (isLandscape) ArrowDirection.RIGHT else ArrowDirection.DOWN
    GamePhase.COMPUTER_TURN -> if (isLandscape) ArrowDirection.LEFT else ArrowDirection.UP
    GamePhase.PLACING, GamePhase.OVER -> null
}

// A pen-drawn arrow in a size x size box: a centred shaft, then two head strokes running back
// from the tip at 45 degrees.
internal fun turnArrowStrokes(size: Float, direction: ArrowDirection): List<Pair<Offset, Offset>> {
    val along = when (direction) {
        ArrowDirection.UP -> Offset(0f, -1f)
        ArrowDirection.DOWN -> Offset(0f, 1f)
        ArrowDirection.LEFT -> Offset(-1f, 0f)
        ArrowDirection.RIGHT -> Offset(1f, 0f)
    }
    val center = Offset(size / 2, size / 2)
    val halfShaft = size * ARROW_SHAFT_TO_BOX / 2
    val tip = center + along * halfShaft
    val headLeg = size * ARROW_HEAD_TO_BOX / sqrt(2f)
    val back = -along * headLeg
    val side = Offset(-along.y, along.x) * headLeg
    return listOf(center - along * halfShaft to tip, tip to tip + back + side, tip to tip + back - side)
}

private const val ARROW_SHAFT_TO_BOX = 0.8f
private const val ARROW_HEAD_TO_BOX = 0.3f
