package com.avs.sea.battle.battle_field

import junit.framework.Assert.*
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
}