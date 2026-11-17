package com.avs.sea.battle.ui

import com.avs.sea.battle.FLEET
import org.junit.Assert.assertEquals
import org.junit.Test

class FleetShipsTest {

    @Test
    fun noneSunkListsTheWholeFleetAfloat() {
        assertEquals(ships(4 to false, 3 to false, 3 to false, 2 to false, 2 to false, 2 to false, 1 to false, 1 to false, 1 to false, 1 to false), fleetShips(emptyList()))
    }

    @Test
    fun sunkShipsAreTheFirstOfTheirSize() {
        assertEquals(
            ships(4 to false, 3 to false, 3 to false, 2 to true, 2 to true, 2 to false, 1 to true, 1 to false, 1 to false, 1 to false),
            fleetShips(listOf(2, 2, 1)),
        )
    }

    @Test
    fun allSunk() {
        assertEquals(ships(4 to true, 3 to true, 3 to true, 2 to true, 2 to true, 2 to true, 1 to true, 1 to true, 1 to true, 1 to true), fleetShips(FLEET))
    }

    private fun ships(vararg entries: Pair<Int, Boolean>) = entries.map { (size, sunk) -> FleetShipUi(size, sunk) }
}
