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
    fun dialogsAreDrawnInThePenOnTheGameBackgroundInBothThemes() {
        listOf(LightColors to false, DarkColors to true).forEach { (colors, isDark) ->
            val scheme = materialColorSchemeFor(colors, isDark)
            assertEquals(colors.background, scheme.surfaceContainerHigh)
            assertEquals(colors.foreground, scheme.onSurfaceVariant)
            assertEquals(colors.foreground, scheme.primary)
        }
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
    fun dayPageShadesTheSelectedCellWithThePenAtThirtyPercent() {
        assertEquals(LightColors.foreground, LightColors.selected.copy(alpha = 1f))
        assertEquals(0.3f, LightColors.selected.alpha, 0.01f)
    }

    // The warm night pen hatches the ships, so a selection shaded in it would barely show.
    @Test
    fun nightPageShadesTheSelectedCellInBlueInkThatStandsOutFromTheHatching() {
        val selected = DarkColors.selected
        assertNotEquals(DarkColors.foreground, selected.copy(alpha = 1f))
        assertTrue("selection is not blue: $selected", selected.blue > selected.red && selected.blue > selected.green)
        assertTrue("selection is too faint: ${selected.alpha}", selected.alpha >= 0.45f)
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
