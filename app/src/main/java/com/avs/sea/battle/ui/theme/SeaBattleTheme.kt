package com.avs.sea.battle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
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

private val LightColors = SeaBattleColors(
    background = Color(0xFFFAFAFA),
    foreground = Color.Black,
    selected = Color(0x99C1C0C0),
    shipFill = Color(0x99808080),
)

private val DarkColors = SeaBattleColors(
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
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    CompositionLocalProvider(LocalSeaBattleColors provides colors, content = content)
}
