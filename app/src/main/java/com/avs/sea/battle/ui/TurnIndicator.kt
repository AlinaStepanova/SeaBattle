package com.avs.sea.battle.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.avs.sea.battle.R
import com.avs.sea.battle.ui.theme.SeaBattleTheme
import kotlin.math.sqrt

internal val TURN_ARROW_SIZE = 24.dp

// Clear space kept between the arrows and the boards on either side.
private val TURN_ARROW_MARGIN = 8.dp

// The board layouts keep at least this much room between the grids for the arrows.
internal val TURN_INDICATOR_MIN_GAP = TURN_ARROW_SIZE + TURN_ARROW_MARGIN * 2

internal enum class ArrowDirection { UP, DOWN, LEFT, RIGHT }

internal const val TURN_INDICATOR_TAG = "turn_indicator"

internal fun turnArrowTag(direction: ArrowDirection): String = "turn_arrow_${direction.name}"

// Two arrows in the gap between the boards: one pointing at the computer's board, one at the
// player's. The one pointing at the board being shot at is drawn bold; the other stays faint.
// Nothing is drawn during placement or at game over, but the gap keeps its size.
@Composable
internal fun TurnIndicator(phase: GamePhase, isLandscape: Boolean, modifier: Modifier = Modifier) {
    val active = activeArrowDirection(phase, isLandscape)
    if (active == null) {
        Box(modifier)
        return
    }
    val towardComputer = if (isLandscape) ArrowDirection.RIGHT else ArrowDirection.DOWN
    val towardPlayer = if (isLandscape) ArrowDirection.LEFT else ArrowDirection.UP
    val turn = stringResource(if (active == towardComputer) R.string.turn_yours_text else R.string.turn_opponent_text)
    // TalkBack users get the turn change the battle status text used to give them.
    Box(
        modifier
            .testTag(TURN_INDICATOR_TAG)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = turn
            },
        contentAlignment = Alignment.Center,
    ) {
        if (isLandscape) {
            Column(verticalArrangement = Arrangement.spacedBy(TURN_ARROW_SPACING)) {
                TurnArrow(towardComputer, active == towardComputer)
                TurnArrow(towardPlayer, active == towardPlayer)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(TURN_ARROW_SPACING)) {
                TurnArrow(towardComputer, active == towardComputer)
                TurnArrow(towardPlayer, active == towardPlayer)
            }
        }
    }
}

@Composable
private fun TurnArrow(direction: ArrowDirection, isActive: Boolean) {
    val emphasis by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(TURN_SWITCH_MILLIS),
        label = "turnArrow",
    )
    val pen = SeaBattleTheme.colors.foreground
    Canvas(
        Modifier
            .size(TURN_ARROW_SIZE)
            .testTag(turnArrowTag(direction))
            .semantics { selected = isActive }
    ) {
        val color = pen.copy(alpha = FAINT_ARROW_ALPHA + (1f - FAINT_ARROW_ALPHA) * emphasis)
        val stroke = size.width * (FAINT_ARROW_STROKE + (ACTIVE_ARROW_STROKE - FAINT_ARROW_STROKE) * emphasis)
        turnArrowStrokes(size.width, direction).forEach { (start, end) ->
            drawLine(color, start, end, stroke, cap = StrokeCap.Round)
        }
    }
}

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
private val TURN_ARROW_SPACING = 14.dp
private const val TURN_SWITCH_MILLIS = 200
private const val FAINT_ARROW_ALPHA = 0.25f
private const val FAINT_ARROW_STROKE = 0.06f
private const val ACTIVE_ARROW_STROKE = 0.1f
