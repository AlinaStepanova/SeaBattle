package com.avs.sea.battle.ui.theme

import org.junit.Assert.assertEquals
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
}
