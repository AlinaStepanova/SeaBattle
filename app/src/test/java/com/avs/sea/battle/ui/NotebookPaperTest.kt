package com.avs.sea.battle.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotebookPaperTest {

    @Test
    fun pageLinesRunThroughTheBoardCorner() {
        val lines = pageLinePositions(origin = 37f, cell = 10f, extent = 100f)
        assertTrue(lines.any { kotlin.math.abs(it - 37f) < 0.001f })
    }

    @Test
    fun pageLinesAreOneCellApart() {
        val lines = pageLinePositions(origin = 37f, cell = 10f, extent = 100f)
        lines.zipWithNext { a, b -> assertEquals(10f, b - a, 0.001f) }
    }

    @Test
    fun pageLinesCoverTheScreenEdgeToEdge() {
        val lines = pageLinePositions(origin = 250f, cell = 30f, extent = 300f)
        assertEquals(10f, lines.first(), 0.001f)
        assertEquals(280f, lines.last(), 0.001f)
    }

    @Test
    fun noPageLinesWithoutACellSize() {
        assertEquals(emptyList<Float>(), pageLinePositions(origin = 0f, cell = 0f, extent = 300f))
    }

    @Test
    fun shipHatchStrokesStayInsideTheCell() {
        val cell = 40f
        hatchStrokes(cell).forEach { (start, end) ->
            listOf(start, end).forEach {
                assertTrue(it.x in 0f..cell && it.y in 0f..cell)
            }
        }
    }

    @Test
    fun shipHatchStrokesAreParallelAndEvenlySpaced() {
        val strokes = hatchStrokes(40f)
        assertTrue("expected a few pen strokes, got ${strokes.size}", strokes.size in 5..8)
        strokes.forEach { (start, end) -> assertEquals(start.x + start.y, end.x + end.y, 0.001f) }
        strokes.map { it.first.x + it.first.y }.zipWithNext { a, b -> assertEquals(10f, b - a, 0.001f) }
    }
}
