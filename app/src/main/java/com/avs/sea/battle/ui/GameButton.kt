package com.avs.sea.battle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avs.sea.battle.ui.theme.NeuchaFontFamily
import com.avs.sea.battle.ui.theme.SeaBattleTheme

@Composable
fun GameButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val colors = SeaBattleTheme.colors
    BasicText(
        text = text,
        style = TextStyle(
            color = colors.foreground,
            fontSize = 20.sp,
            fontFamily = NeuchaFontFamily,
        ),
        modifier = modifier
            .background(if (pressed) colors.selected else colors.background)
            .border(1.dp, colors.foreground)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Preview
@Composable
private fun GameButtonPreview() {
    SeaBattleTheme { GameButton(text = "Generate ships", onClick = {}) }
}
