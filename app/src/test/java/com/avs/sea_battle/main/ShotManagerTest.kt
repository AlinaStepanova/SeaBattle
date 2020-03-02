package com.avs.sea_battle.main

import com.avs.sea_battle.battle_field.Coordinate
import com.avs.sea_battle.ships.Orientation
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class ShotManagerTest {

    lateinit var shotManager: ShotManager

    @Before
    fun setUp() {
        shotManager = ShotManager()
    }

    @Test
    fun getMaxPoint() {
        assertEquals(shotManager.getMaxCoordinate(mutableListOf(
            Coordinate(5, 4),
            Coordinate(3, 4),
            Coordinate(4, 4)), Orientation.VERTICAL), Coordinate(5, 4))
        assertEquals(shotManager.getMaxCoordinate(mutableListOf(
            Coordinate(5, 4),
            Coordinate(5, 3),
            Coordinate(5, 5),
            Coordinate(5, 6)), Orientation.HORIZONTAL), Coordinate(5, 6))
    }

    @Test
    fun getMinPoint() {
        assertEquals(shotManager.getMinCoordinate(mutableListOf(
            Coordinate(5, 4),
            Coordinate(3, 4),
            Coordinate(4, 4)), Orientation.VERTICAL), Coordinate(3, 4))
        assertEquals(shotManager.getMinCoordinate(mutableListOf(
            Coordinate(5, 4),
            Coordinate(5, 3),
            Coordinate(5, 5),
            Coordinate(5, 6)), Orientation.HORIZONTAL), Coordinate(5, 3))
    }
}