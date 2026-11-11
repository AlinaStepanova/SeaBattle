package com.avs.sea.battle.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeaBattleThemeTest {

    @Test
    fun darkMaterialSchemeDrawsMenusOnTheDarkGameBackground() {
        val scheme = materialColorSchemeFor(DarkColors, isDark = true)
        assertEquals(DarkColors.background, scheme.surfaceContainer)
        assertEquals(DarkColors.foreground, scheme.onSurface)
    }

    @Test
    fun lightMaterialSchemeDrawsMenusOnTheLightGameBackground() {
        val scheme = materialColorSchemeFor(LightColors, isDark = false)
        assertEquals(LightColors.background, scheme.surfaceContainer)
        assertEquals(LightColors.foreground, scheme.onSurface)
    }

    @Test
    fun materialSchemeUsesGameBackgroundForPlainSurfaces() {
        val scheme = materialColorSchemeFor(DarkColors, isDark = true)
        assertEquals(DarkColors.background, scheme.surface)
        assertEquals(DarkColors.background, scheme.background)
        assertEquals(DarkColors.foreground, scheme.onBackground)
    }

    @Test
    fun lightPageIsDrawnInBlueBallpointOnPaper() {
        assertEquals(Color(0xFF2A4DA8), LightColors.foreground)
        assertTrue(LightColors.background.luminance() > 0.9f)
    }

    @Test
    fun darkPageIsLitByADimLampAndDrawnInWarmLightPen() {
        assertEquals(Color(0xFFE6DFCF), DarkColors.foreground)
        assertTrue(DarkColors.background.luminance() < 0.05f)
    }

    @Test
    fun hitsAreCrossedOutWithARedPenInBothThemes() {
        assertEquals(Color(0xFFB23A30), LightColors.hit)
        assertEquals(Color(0xFFE08A7E), DarkColors.hit)
        listOf(LightColors, DarkColors).forEach { assertNotEquals(it.foreground, it.hit) }
    }

    @Test
    fun selectedCellIsShadedWithThePenAtThirtyPercent() {
        listOf(LightColors, DarkColors).forEach {
            assertEquals(it.foreground, it.selected.copy(alpha = 1f))
            assertEquals(0.3f, it.selected.alpha, 0.01f)
        }
    }

    @Test
    fun shipsAreHatchedWithThePen() {
        listOf(LightColors, DarkColors).forEach {
            assertEquals(it.foreground, it.shipHatch.copy(alpha = 1f))
            assertTrue(it.shipHatch.alpha in 0.6f..0.75f)
        }
    }

    @Test
    fun notebookSquaresAreFainterThanAnythingDrawnInPen() {
        listOf(LightColors, DarkColors).forEach {
            assertTrue(it.pageLine.alpha in 0.05f..0.35f)
            assertTrue(it.pageLine.alpha < it.shipHatch.alpha)
        }
    }

    @Test
    fun refusedPlacementsFlashInAFaintRedPenInBothThemes() {
        listOf(LightColors, DarkColors).forEach {
            assertEquals(it.hit.copy(alpha = 0.35f), it.rejected)
            assertNotEquals(it.selected, it.rejected)
        }
    }
}
