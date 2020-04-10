package com.avs.sea_battle.battle_field

import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class CoordinateFTest {

    private lateinit var coordinateFirst: CoordinateF
    private lateinit var coordinateSecond: CoordinateF

    @Before
    fun setUp() {
        coordinateFirst = CoordinateF()
        coordinateSecond = CoordinateF(2.5F, 7.3F)
    }

    @Test
    fun getX() {
        assertEquals(coordinateFirst.x, 0.0F)
        assertEquals(coordinateSecond.x, 2.5F)
    }

    @Test
    fun setX() {
        coordinateSecond.x = 2.456F
        assertEquals(coordinateSecond.x, 2.456F)
    }

    @Test
    fun getY() {
        assertEquals(coordinateFirst.y, 0.0F)
        assertEquals(coordinateSecond.y, 7.3F)
    }

    @Test
    fun setY() {
        coordinateSecond.y = 9.0F
        assertEquals(coordinateSecond.y, 9.0F)
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