package com.avs.sea.battle.battle_field

import kotlin.math.abs

object PlacementRules {

    // A ship fits if it stays on the board and keeps a one-cell gap to every other ship,
    // diagonals included.
    fun canPlace(candidate: ShipPlacement, others: List<ShipPlacement>): Boolean =
        candidate.isInsideBoard() && others.none { touches(candidate, it) }

    private fun touches(first: ShipPlacement, second: ShipPlacement): Boolean =
        first.cells().any { a -> second.cells().any { b -> abs(a.x - b.x) <= 1 && abs(a.y - b.y) <= 1 } }
}
