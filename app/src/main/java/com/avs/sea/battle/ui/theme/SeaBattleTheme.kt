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
    val shipFill: Color,
)

internal val LightColors = SeaBattleColors(
    background = Color(0xFFFAFAFA),
    foreground = Color.Black,
    selected = Color(0x99C1C0C0),
    shipFill = Color(0x99808080),
)

internal val DarkColors = SeaBattleColors(
    background = Color(0xFF303030),
    foreground = Color.White,
    selected = Color(0x99C1C0C0),
    shipFill = Color(0x99808080),
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

// Material components (the overflow DropdownMenu) read their colors from MaterialTheme,
// so the game palette is mapped onto it to keep menus in sync with the day/night theme.
internal fun materialColorSchemeFor(colors: SeaBattleColors, isDark: Boolean): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        background = colors.background,
        onBackground = colors.foreground,
        surface = colors.background,
        onSurface = colors.foreground,
        surfaceContainer = colors.background,
    )
}
