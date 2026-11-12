package com.avs.sea.battle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.avs.sea.battle.R

val NeuchaFontFamily = FontFamily(Font(R.font.neucha))

data class SeaBattleColors(
    val background: Color,
    val foreground: Color,
    val selected: Color,
    val shipHatch: Color,
    val hit: Color,
    val rejected: Color,
    val pageLine: Color,
)

// A squared school-notebook page: everything is drawn in one pen on faint squares, and hits
// are crossed out with a red pen. Dark mode is the same page under a dim lamp.
private val BlueBallpoint = Color(0xFF2A4DA8)
private val LampLitPen = Color(0xFFE6DFCF)
// The warm pen is too close to the ship hatching to mark a selection, so night uses blue ink.
private val LampLitBlueInk = Color(0xFF8FB4FF)

internal val LightColors = SeaBattleColors(
    background = Color(0xFFFCFBF6),
    foreground = BlueBallpoint,
    selected = BlueBallpoint.copy(alpha = 0.3f),
    shipHatch = BlueBallpoint.copy(alpha = 0.7f),
    hit = Color(0xFFB23A30),
    rejected = Color(0xFFB23A30).copy(alpha = 0.35f),
    pageLine = Color(0xFF608CBE).copy(alpha = 0.3f),
)

internal val DarkColors = SeaBattleColors(
    background = Color(0xFF2C2924),
    foreground = LampLitPen,
    selected = LampLitBlueInk.copy(alpha = 0.5f),
    shipHatch = LampLitPen.copy(alpha = 0.65f),
    hit = Color(0xFFE08A7E),
    rejected = Color(0xFFE08A7E).copy(alpha = 0.35f),
    pageLine = Color(0xFFCDC3AA).copy(alpha = 0.13f),
)

private val LocalSeaBattleColors = staticCompositionLocalOf { LightColors }

object SeaBattleTheme {
    val colors: SeaBattleColors
        @Composable get() = LocalSeaBattleColors.current
}

@Composable
fun SeaBattleTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val colors = if (isDark) DarkColors else LightColors
    MaterialTheme(colorScheme = materialColorSchemeFor(colors, isDark)) {
        CompositionLocalProvider(LocalSeaBattleColors provides colors, content = content)
    }
}

// Material components (the overflow DropdownMenu, the new game AlertDialog) read their colors
// from MaterialTheme, so the game palette is mapped onto it to keep them in sync with the
// day/night theme instead of Material's default purple.
internal fun materialColorSchemeFor(colors: SeaBattleColors, isDark: Boolean): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        background = colors.background,
        onBackground = colors.foreground,
        surface = colors.background,
        onSurface = colors.foreground,
        surfaceContainer = colors.background,
        surfaceContainerHigh = colors.background,
        onSurfaceVariant = colors.foreground,
        primary = colors.foreground,
    )
}
