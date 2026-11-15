package com.avs.sea.battle.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.avs.sea.battle.FLEET
import com.avs.sea.battle.R
import com.avs.sea.battle.ui.theme.SeaBattleTheme

internal fun fleetTrackerCell(maxWidth: Dp): Dp = minOf(FLEET_MAX_CELL, maxWidth / FLEET_ROW_WIDTH_IN_CELLS)

// Space kept between a landscape fleet column and its board, and between it and the screen edge.
internal val FLEET_COLUMN_MARGIN = 8.dp

// A landscape fleet column lists one ship per line beside a board, as big as the side space and
// the grid height allow.
internal fun landscapeFleetColumnCell(sideSpace: Dp, gridHeight: Dp): Dp = minOf(
    LANDSCAPE_FLEET_MAX_CELL,
    (sideSpace - FLEET_COLUMN_MARGIN * 2) / FLEET.max(),
    gridHeight / FLEET_COLUMN_HEIGHT_IN_CELLS,
)

internal data class StrikeLine(val start: Offset, val end: Offset, val width: Float)

// The red pen line through a sunk ship. Round caps reach half the width past each end, so the
// ends are inset by that much and the line stays within the ship.
internal fun sunkStrike(shipWidth: Float, cell: Float): StrikeLine {
    val width = cell * SUNK_STROKE_TO_CELL
    return StrikeLine(Offset(width / 2, cell / 2), Offset(shipWidth - width / 2, cell / 2), width)
}

internal fun fleetTrackerTag(isOpponent: Boolean): String =
    if (isOpponent) "fleet_tracker_opponent" else "fleet_tracker_yours"

internal fun fleetShipTag(index: Int): String = "fleet_ship_$index"

// Lets tests see which ships are struck through; accessibility services don't read custom keys.
internal val SunkShipKey = SemanticsPropertyKey<Boolean>("SunkShip")
internal var SemanticsPropertyReceiver.sunkShip by SunkShipKey

// One side's fleet as small pencil ships, in a row or, beside a landscape board, a column with
// one ship per line. A sunk ship is struck through in the red of the hit crosses. Nothing is
// drawn before the battle starts.
@Composable
internal fun FleetTracker(
    ships: List<FleetShipUi>,
    isOpponent: Boolean,
    cell: Dp,
    vertical: Boolean,
    alignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
) {
    if (ships.isEmpty()) return
    val description = stringResource(
        if (isOpponent) R.string.fleet_opponent_afloat else R.string.fleet_yours_afloat,
        ships.count { !it.sunk },
        ships.size,
    )
    val tracker = modifier.testTag(fleetTrackerTag(isOpponent)).semantics { contentDescription = description }
    val gap = cell * SHIP_GAP_TO_CELL
    val drawShips = @Composable {
        ships.forEachIndexed { index, ship -> FleetShip(ship, cell, Modifier.testTag(fleetShipTag(index))) }
    }
    if (vertical) {
        Column(tracker, verticalArrangement = Arrangement.spacedBy(gap), horizontalAlignment = alignment) { drawShips() }
    } else {
        Row(tracker, horizontalArrangement = Arrangement.spacedBy(gap, alignment)) { drawShips() }
    }
}

@Composable
private fun FleetShip(ship: FleetShipUi, cell: Dp, modifier: Modifier = Modifier) {
    val colors = SeaBattleTheme.colors
    Canvas(modifier.size(width = cell * ship.size, height = cell).semantics { sunkShip = ship.sunk }) {
        val side = size.height
        val hatch = hatchStrokes(side)
        repeat(ship.size) { index ->
            val corner = Offset(index * side, 0f)
            hatch.forEach { (start, end) ->
                drawLine(colors.shipHatch, corner + start, corner + end, strokeWidth = 1f, cap = StrokeCap.Round)
            }
            drawRect(colors.foreground, topLeft = corner, size = Size(side, side), style = Stroke(width = 1f))
        }
        if (ship.sunk) {
            val strike = sunkStrike(size.width, side)
            drawLine(colors.hit, strike.start, strike.end, strokeWidth = strike.width, cap = StrokeCap.Round)
        }
    }
}

private val FLEET_MAX_CELL = 12.dp
private val LANDSCAPE_FLEET_MAX_CELL = 14.dp
private const val SHIP_GAP_TO_CELL = 0.5f

// A row is every ship's cells plus half a cell between ships; a column is one cell per ship.
private val FLEET_ROW_WIDTH_IN_CELLS = FLEET.sum() + (FLEET.size - 1) * SHIP_GAP_TO_CELL
private val FLEET_COLUMN_HEIGHT_IN_CELLS = FLEET.size + (FLEET.size - 1) * SHIP_GAP_TO_CELL

// Bolder than a hit cross relative to its cell, so the strike-through still reads on a small ship.
private const val SUNK_STROKE_TO_CELL = 0.18f
