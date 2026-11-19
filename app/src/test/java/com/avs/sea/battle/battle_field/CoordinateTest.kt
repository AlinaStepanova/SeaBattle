package com.avs.sea.battle.battle_field

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CoordinateTest {

    private lateinit var coordinateFirst: Coordinate
    private lateinit var coordinateSecond: Coordinate

    @Before
    fun setUp() {
        coordinateFirst = Coordinate()
        coordinateSecond = Coordinate(5, 7)
    }

    @Test
    fun getX() {
        assertEquals(coordinateFirst.x, -1)
        assertEquals(coordinateSecond.x, 5)
    }

    @Test
    fun setX() {
        coordinateSecond.x = 2
        assertEquals(coordinateSecond.x, 2)
    }

    @Test
    fun getY() {
        assertEquals(coordinateFirst.y, -1)
        assertEquals(coordinateSecond.y, 7)
    }

    @Test
    fun setY() {
        coordinateSecond.y = 9
        assertEquals(coordinateSecond.y, 9)
    }

    @Test
    fun testHashCode() {
        assertNotSame(coordinateFirst.hashCode(), coordinateSecond.hashCode())
    }

    @Test
    fun testEquals() {
        assertFalse(coordinateFirst == coordinateSecond)
    }

    @Test
    fun cornersAndInnerCellsAreOnTheBoard() {
        listOf(Coordinate(0, 0), Coordinate(0, 9), Coordinate(9, 0), Coordinate(9, 9), coordinateSecond)
            .forEach { assertTrue("$it", it.isOnBoard()) }
    }

    @Test
    fun cellsJustPastAnyEdgeAreOffTheBoard() {
        listOf(Coordinate(-1, 0), Coordinate(0, -1), Coordinate(10, 0), Coordinate(0, 10), coordinateFirst)
            .forEach { assertFalse("$it", it.isOnBoard()) }
    }
}