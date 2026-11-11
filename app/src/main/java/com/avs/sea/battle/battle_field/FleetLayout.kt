package com.avs.sea.battle.battle_field

import com.avs.sea.battle.FLEET
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.ships.Orientation
import kotlin.random.Random

sealed interface PlacementResult {
    data class Placed(val layout: FleetLayout) : PlacementResult

    // The cells the ship would have covered, limited to the board.
    data class Rejected(val cells: List<Coordinate>) : PlacementResult
}

// An immutable arrangement of the whole fleet; every change returns a new layout.
data class FleetLayout(val ships: List<ShipPlacement>) {

    fun cells(): List<Coordinate> = ships.flatMap { it.cells() }

    fun shipIndexAt(coordinate: Coordinate): Int? =
        ships.indexOfFirst { coordinate in it.cells() }.takeIf { it >= 0 }

    fun move(index: Int, target: Coordinate): PlacementResult = replace(index, ships[index].movedTo(target))

    fun rotate(index: Int): PlacementResult = replace(index, ships[index].rotated())

    private fun replace(index: Int, candidate: ShipPlacement): PlacementResult {
        val others = ships.filterIndexed { i, _ -> i != index }
        return if (PlacementRules.canPlace(candidate, others)) {
            PlacementResult.Placed(FleetLayout(ships.toMutableList().apply { set(index, candidate) }))
        } else {
            PlacementResult.Rejected(candidate.cells().filter { it.x in 0 until SQUARES_COUNT && it.y in 0 until SQUARES_COUNT })
        }
    }

    companion object {
        // Ships lined up along the edges of the board, as in the placement concept sketch.
        fun initial(): FleetLayout = FleetLayout(
            listOf(
                ShipPlacement(4, Coordinate(0, 1), Orientation.HORIZONTAL),
                ShipPlacement(3, Coordinate(0, 6), Orientation.HORIZONTAL),
                ShipPlacement(3, Coordinate(2, 0), Orientation.VERTICAL),
                ShipPlacement(2, Coordinate(6, 0), Orientation.VERTICAL),
                ShipPlacement(2, Coordinate(9, 1), Orientation.HORIZONTAL),
                ShipPlacement(2, Coordinate(9, 4), Orientation.HORIZONTAL),
                ShipPlacement(1, Coordinate(3, 9), Orientation.HORIZONTAL),
                ShipPlacement(1, Coordinate(5, 9), Orientation.HORIZONTAL),
                ShipPlacement(1, Coordinate(7, 9), Orientation.HORIZONTAL),
                ShipPlacement(1, Coordinate(9, 7), Orientation.HORIZONTAL),
            )
        )

        fun random(random: Random = Random.Default): FleetLayout {
            val placed = mutableListOf<ShipPlacement>()
            FLEET.forEach { size ->
                var candidate: ShipPlacement
                do {
                    candidate = randomPlacement(size, random)
                } while (!PlacementRules.canPlace(candidate, placed))
                placed.add(candidate)
            }
            return FleetLayout(placed)
        }

        private fun randomPlacement(size: Int, random: Random): ShipPlacement {
            val orientation = if (random.nextBoolean()) Orientation.VERTICAL else Orientation.HORIZONTAL
            val longSideRange = 0..SQUARES_COUNT - size
            val shortSideRange = 0 until SQUARES_COUNT
            val origin = if (orientation == Orientation.VERTICAL) {
                Coordinate(longSideRange.random(random), shortSideRange.random(random))
            } else {
                Coordinate(shortSideRange.random(random), longSideRange.random(random))
            }
            return ShipPlacement(size, origin, orientation)
        }
    }
}
