package com.avs.sea_battle.battle_field

import com.avs.sea_battle.SQUARES_COUNT
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import com.avs.sea_battle.battle_field.CellState.*

class BaseBattleFieldTest {

    lateinit var baseBattleField: BaseBattleField

    @Before
    fun setUp() {
        baseBattleField = BaseBattleField()
    }

    @Test
    fun initBattleShip() {
        assertEquals(baseBattleField.getCellsArray().size, SQUARES_COUNT)
        assertTrue(baseBattleField.getCellsArray().isNotEmpty())
        assertEquals(baseBattleField.getCellsArray()[3][9], Cell(3, 9))
        assertEquals(baseBattleField.getCellsArray()[6][4], Cell(6, 4))
    }

    @Test
    fun setCellState() {
        baseBattleField.setCellState(Coordinate(1, 1), SHOT_FAILURE)
        assertEquals(baseBattleField.getCellsArray()[1][1]?.getCellState(), SHOT_FAILURE)
        baseBattleField.setCellState(Coordinate(1, 1), SHOT_SUCCESS)
        assertEquals(baseBattleField.getCellsArray()[1][1]?.getCellState(), SHOT_SUCCESS)
        baseBattleField.setCellState(Coordinate(2, 2), SHIP)
        assertEquals(baseBattleField.getCellsArray()[2][2]?.getCellState(), SHIP)
    }

    @Test
    fun isCellFreeToBeSelected() {
        baseBattleField.setCellState(Coordinate(1, 1), SHIP)
        assertTrue(baseBattleField.isCellFreeToBeSelected(Coordinate(1, 1)))
        baseBattleField.setCellState(Coordinate(1, 1), SHOT_FAILURE)
        assertFalse(baseBattleField.isCellFreeToBeSelected(Coordinate(1, 1)))
        baseBattleField.setCellState(Coordinate(1, 2), EMPTY)
        assertTrue(baseBattleField.isCellFreeToBeSelected(Coordinate(1, 2)))
        baseBattleField.setCellState(Coordinate(1, 2), SHOT_FAILURE)
        assertFalse(baseBattleField.isCellFreeToBeSelected(Coordinate(1, 2)))
        baseBattleField.setCellState(Coordinate(1, 2), SHOT_SUCCESS)
        assertFalse(baseBattleField.isCellFreeToBeSelected(Coordinate(1, 2)))
    }
}