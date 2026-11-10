package com.avs.sea.battle.battle_field

import com.avs.sea.battle.FOUR_DECK_SHIP_SIZE
import com.avs.sea.battle.ONE_DECK_SHIP_SIZE
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.THREE_DECK_SHIP_SIZE
import com.avs.sea.battle.TWO_DECK_SHIP_SIZE
import org.junit.Assert.*
import org.junit.Test

class BattleFieldTest {

    private val expectedFleetSizes = listOf(
        ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE,
        TWO_DECK_SHIP_SIZE, TWO_DECK_SHIP_SIZE, TWO_DECK_SHIP_SIZE,
        THREE_DECK_SHIP_SIZE, THREE_DECK_SHIP_SIZE, FOUR_DECK_SHIP_SIZE
    )

    private fun placeRandomFleet(): BattleField {
        val battleField = BattleField()
        battleField.randomizeShips()
        return battleField
    }

    private fun getShipCells(battleField: BattleField): Set<Pair<Int, Int>> {
        val cells = battleField.getCellsArray()
        val shipCells = mutableSetOf<Pair<Int, Int>>()
        for (i in 0 until SQUARES_COUNT) {
            for (j in 0 until SQUARES_COUNT) {
                if (cells[i][j]?.getCellState() == CellState.SHIP) {
                    shipCells.add(i to j)
                }
            }
        }
        return shipCells
    }

    private fun getShips(shipCells: Set<Pair<Int, Int>>): List<Set<Pair<Int, Int>>> {
        val ships = mutableListOf<Set<Pair<Int, Int>>>()
        val visited = mutableSetOf<Pair<Int, Int>>()
        for (cell in shipCells) {
            if (cell in visited) continue
            val ship = mutableSetOf<Pair<Int, Int>>()
            val queue = ArrayDeque(listOf(cell))
            while (queue.isNotEmpty()) {
                val (x, y) = queue.removeFirst()
                if (!visited.add(x to y)) continue
                ship.add(x to y)
                listOf(x - 1 to y, x + 1 to y, x to y - 1, x to y + 1)
                    .filter { it in shipCells && it !in visited }
                    .forEach { queue.add(it) }
            }
            ships.add(ship)
        }
        return ships
    }

    @Test
    fun randomizeShipsPlacesWholeFleet() {
        repeat(20) {
            val shipCells = getShipCells(placeRandomFleet())
            assertEquals(expectedFleetSizes.sum(), shipCells.size)
            val shipSizes = getShips(shipCells).map { it.size }.sorted()
            assertEquals(expectedFleetSizes, shipSizes)
        }
    }

    @Test
    fun randomizeShipsPlacesShipsAsStraightLines() {
        repeat(20) {
            val ships = getShips(getShipCells(placeRandomFleet()))
            for (ship in ships) {
                val rows = ship.map { it.first }.distinct()
                val cols = ship.map { it.second }.distinct()
                assertTrue(
                    "Ship is not a straight line: $ship",
                    rows.size == 1 || cols.size == 1
                )
            }
        }
    }

    @Test
    fun randomizeShipsNeverPlacesTouchingShips() {
        repeat(20) {
            val shipCells = getShipCells(placeRandomFleet())
            val ships = getShips(shipCells)
            for (ship in ships) {
                for ((x, y) in ship) {
                    for (i in x - 1..x + 1) {
                        for (j in y - 1..y + 1) {
                            val neighbour = i to j
                            if (neighbour in shipCells && neighbour !in ship) {
                                fail("Ships touch at $neighbour: $ships")
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun randomizeShipsCanReachEveryCell() {
        val uncovered = mutableSetOf<Pair<Int, Int>>()
        for (i in 0 until SQUARES_COUNT) {
            for (j in 0 until SQUARES_COUNT) {
                uncovered.add(i to j)
            }
        }
        repeat(500) {
            uncovered.removeAll(getShipCells(placeRandomFleet()))
            if (uncovered.isEmpty()) return
        }
        fail("Cells never occupied by any ship after 500 random fleets: $uncovered")
    }
}
