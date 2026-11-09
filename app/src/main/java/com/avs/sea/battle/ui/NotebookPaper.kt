package com.avs.sea.battle.ui

import androidx.compose.ui.geometry.Offset

// Positions of the notebook's square lines along one axis: one cell apart, passing through
// [origin] (the corner of the player's board), and covering the whole screen from 0 to [extent].
internal fun pageLinePositions(origin: Float, cell: Float, extent: Float): List<Float> {
    if (cell <= 0f) return emptyList()
    val first = ((origin % cell) + cell) % cell
    return generateSequence(first) { it + cell }.takeWhile { it <= extent }.toList()
}

// Diagonal pen strokes a quarter of a cell apart, clipped to the cell, the way a ship is
// shaded in on paper.
internal fun hatchStrokes(cell: Float): List<Pair<Offset, Offset>> {
    val spacing = cell / 4
    return (1 until 8).map { i ->
        val sum = i * spacing
        if (sum <= cell) {
            Offset(0f, sum) to Offset(sum, 0f)
        } else {
            Offset(sum - cell, cell) to Offset(cell, sum - cell)
        }
    }
}
