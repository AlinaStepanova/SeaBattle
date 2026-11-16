package com.avs.sea.battle.battle_field

import com.avs.sea.battle.FOUR_DECK_SHIP_SIZE
import com.avs.sea.battle.ONE_DECK_SHIP_SIZE
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.THREE_DECK_SHIP_SIZE
import com.avs.sea.battle.TWO_DECK_SHIP_SIZE
import org.junit.Assert.*
import org.junit.Test

class BattleFieldTest {

    // Indices into FleetLayout.initial(): the 4-deck at B1-E1, the 2-deck at A7-A8 and the
    // 1-decks at J4 and J6.
    private companion object {
        const val INITIAL_FOUR_DECK = 0
        const val INITIAL_TWO_DECK = 3
        const val INITIAL_FIRST_ONE_DECK = 6
        const val INITIAL_SECOND_ONE_DECK = 7
    }

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

    @Test
    fun placeShipsPutsShipsExactlyOnTheLayoutCells() {
        val layout = FleetLayout.initial()
        val battleField = BattleField()
        battleField.placeShips(layout)
        assertEquals(layout.cells().map { it.x to it.y }.toSet(), getShipCells(battleField))
    }

    @Test
    fun placeShipsReplacesThePreviousFleet() {
        val battleField = placeRandomFleet()
        battleField.placeShips(FleetLayout.initial())
        assertEquals(FleetLayout.initial().cells().size, getShipCells(battleField).size)
        assertEquals(expectedFleetSizes, getShips(getShipCells(battleField)).map { it.size }.sorted())
    }

    @Test
    fun shotAtWaterIsAMiss() {
        assertEquals(ShotResult.Miss, initialFleetField().handleShot(Coordinate(5, 5)))
    }

    // The 3-deck at A3-A5 is vertical.
    @Test
    fun shotAtAShipIsAHitUntilItsLastCellSinksIt() {
        val battleField = initialFleetField()
        assertEquals(ShotResult.Hit, battleField.handleShot(Coordinate(2, 0)))
        assertEquals(ShotResult.Hit, battleField.handleShot(Coordinate(3, 0)))
        assertEquals(
            ShotResult.Sunk(listOf(Coordinate(2, 0), Coordinate(3, 0), Coordinate(4, 0))),
            battleField.handleShot(Coordinate(4, 0)),
        )
    }

    // Callers only shoot free cells, but the model must not turn a repeated shot into a hit either.
    @Test
    fun shootingAMissAgainChangesNothing() {
        val battleField = initialFleetField()
        battleField.handleShot(Coordinate(5, 5))
        assertEquals(ShotResult.Miss, battleField.handleShot(Coordinate(5, 5)))
        assertEquals(listOf(Coordinate(5, 5)), battleField.getDotsCoordinates())
        assertEquals(emptyList<Coordinate>(), battleField.getCrossesCoordinates())
    }

    @Test
    fun shootingAHitAgainNeitherHitsNorSinks() {
        val battleField = initialFleetField()
        val fourDeckCell = FleetLayout.initial().ships[INITIAL_FOUR_DECK].cells().first()
        battleField.handleShot(fourDeckCell)
        repeat(FOUR_DECK_SHIP_SIZE) { assertEquals(ShotResult.Miss, battleField.handleShot(fourDeckCell)) }
        assertEquals(listOf(fourDeckCell), battleField.getCrossesCoordinates())
        assertEquals(emptyList<Int>(), battleField.sunkShipSizes())
    }

    @Test
    fun shootingAMarkedCellAroundASunkShipIsNotAHit() {
        val battleField = initialFleetField()
        sink(battleField, INITIAL_FIRST_ONE_DECK)
        val marked = battleField.getDotsCoordinates().first()
        assertEquals(ShotResult.Miss, battleField.handleShot(marked))
        assertTrue(marked in battleField.getDotsCoordinates())
        assertEquals(FleetLayout.initial().ships[INITIAL_FIRST_ONE_DECK].cells(), battleField.getCrossesCoordinates())
    }

    @Test
    fun noShipIsSunkBeforeAnyShot() {
        assertEquals(emptyList<Int>(), initialFleetField().sunkShipSizes())
    }

    @Test
    fun aHitThatDoesNotSinkCountsNothing() {
        val battleField = initialFleetField()
        battleField.handleShot(Coordinate(0, 1))
        assertEquals(emptyList<Int>(), battleField.sunkShipSizes())
    }

    @Test
    fun aSunkShipIsCountedBySize() {
        val battleField = initialFleetField()
        sink(battleField, INITIAL_TWO_DECK)
        assertEquals(listOf(2), battleField.sunkShipSizes())
    }

    @Test
    fun severalSunkShipsAreAllCounted() {
        val battleField = initialFleetField()
        listOf(INITIAL_FOUR_DECK, INITIAL_FIRST_ONE_DECK, INITIAL_SECOND_ONE_DECK).forEach { sink(battleField, it) }
        assertEquals(listOf(1, 1, 4), battleField.sunkShipSizes().sorted())
    }

    @Test
    fun allShipsSunkGivesTheWholeFleet() {
        val battleField = initialFleetField()
        FleetLayout.initial().cells().forEach { battleField.handleShot(it) }
        assertEquals(expectedFleetSizes, battleField.sunkShipSizes().sorted())
    }

    private fun initialFleetField() = BattleField().apply { placeShips(FleetLayout.initial()) }

    private fun sink(battleField: BattleField, shipIndex: Int) {
        FleetLayout.initial().ships[shipIndex].cells().forEach { battleField.handleShot(it) }
    }

    @Test
    fun manuallyPlacedFleetIsOverOnlyWhenEveryShipSinks() {
        val battleField = BattleField()
        val layout = FleetLayout.initial()
        battleField.placeShips(layout)
        layout.cells().dropLast(1).forEach { battleField.handleShot(it) }
        assertFalse(battleField.isGameOver())
        battleField.handleShot(layout.cells().last())
        assertTrue(battleField.isGameOver())
    }
}
