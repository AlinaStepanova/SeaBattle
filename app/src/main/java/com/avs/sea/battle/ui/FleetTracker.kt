package com.avs.sea.battle.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.avs.sea.battle.R
import com.avs.sea.battle.ui.theme.SeaBattleTheme

internal fun fleetTrackerCell(maxWidth: Dp): Dp = minOf(FLEET_MAX_CELL, maxWidth / FLEET_ROW_WIDTH_IN_CELLS)

internal fun fleetTrackerTag(isOpponent: Boolean): String =
    if (isOpponent) "fleet_tracker_opponent" else "fleet_tracker_yours"

internal fun fleetShipTag(index: Int): String = "fleet_ship_$index"

// Lets tests see which ships are struck through; accessibility services don't read custom keys.
internal val SunkShipKey = SemanticsPropertyKey<Boolean>("SunkShip")
internal var SemanticsPropertyReceiver.sunkShip by SunkShipKey

// One side's fleet as a row of small pencil ships; a sunk ship is struck through in the red of
// the hit crosses. Nothing is drawn before the battle starts.
@Composable
internal fun FleetTracker(
    ships: List<FleetShipUi>,
    isOpponent: Boolean,
    alignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
) {
    if (ships.isEmpty()) return
    val description = stringResource(
        if (isOpponent) R.string.fleet_opponent_afloat else R.string.fleet_yours_afloat,
        ships.count { !it.sunk },
        ships.size,
    )
    BoxWithConstraints(modifier) {
        val cell = fleetTrackerCell(maxWidth)
        Row(
            Modifier
                .fillMaxWidth()
                .testTag(fleetTrackerTag(isOpponent))
                .semantics { contentDescription = description },
            horizontalArrangement = Arrangement.spacedBy(cell / 2, alignment),
        ) {
            ships.forEachIndexed { index, ship -> FleetShip(ship, cell, Modifier.testTag(fleetShipTag(index))) }
        }
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
            drawLine(
                colors.hit,
                Offset(0f, side / 2),
                Offset(size.width, side / 2),
                strokeWidth = side * SUNK_STROKE_TO_CELL,
                cap = StrokeCap.Round,
            )
        }
    }
}

private val FLEET_MAX_CELL = 10.dp

// A row is 20 ship cells plus 9 gaps of half a cell between the 10 ships.
private const val FLEET_ROW_WIDTH_IN_CELLS = 24.5f

// Bolder than a hit cross relative to its cell, so the strike-through reads on a 6-10 dp ship.
private const val SUNK_STROKE_TO_CELL = 0.25f
