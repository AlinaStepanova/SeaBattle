package com.avs.sea.battle.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avs.sea.battle.R
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.ui.theme.NeuchaFontFamily
import com.avs.sea.battle.ui.theme.SeaBattleTheme

private val LETTER_IDS = listOf(
    R.string.letter_a, R.string.letter_b, R.string.letter_c, R.string.letter_d,
    R.string.letter_e, R.string.letter_f, R.string.letter_g, R.string.letter_h,
    R.string.letter_i, R.string.letter_j,
)

private val NUMBER_IDS = listOf(
    R.string.number_1, R.string.number_2, R.string.number_3, R.string.number_4,
    R.string.number_5, R.string.number_6, R.string.number_7, R.string.number_8,
    R.string.number_9, R.string.number_10,
)

private val LABEL_SIZE = 20.dp

@Composable
fun LabeledBoard(
    ships: List<Coordinate>,
    crosses: List<Coordinate>,
    dots: List<Coordinate>,
    selected: Coordinate?,
    onCellTap: ((Coordinate) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val labelStyle = TextStyle(
        color = SeaBattleTheme.colors.foreground,
        fontFamily = NeuchaFontFamily,
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
    )
    Column(modifier = modifier) {
        Row(Modifier.fillMaxWidth().padding(start = LABEL_SIZE)) {
            LETTER_IDS.forEach { id ->
                BasicText(
                    text = stringResource(id),
                    style = labelStyle,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.width(LABEL_SIZE).aspectRatio(1f / SQUARES_COUNT)) {
                NUMBER_IDS.forEach { id ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        BasicText(text = stringResource(id), style = labelStyle)
                    }
                }
            }
            BoardGrid(
                ships = ships,
                crosses = crosses,
                dots = dots,
                selected = selected,
                onCellTap = onCellTap,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun BoardGrid(
    ships: List<Coordinate>,
    crosses: List<Coordinate>,
    dots: List<Coordinate>,
    selected: Coordinate?,
    onCellTap: ((Coordinate) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = SeaBattleTheme.colors
    val tapModifier = if (onCellTap != null) {
        Modifier.pointerInput(onCellTap) {
            detectTapGestures { offset ->
                val cell = size.width.toFloat() / SQUARES_COUNT
                val row = (offset.y / cell).toInt().coerceIn(0, SQUARES_COUNT - 1)
                val column = (offset.x / cell).toInt().coerceIn(0, SQUARES_COUNT - 1)
                onCellTap(Coordinate(row, column))
            }
        }
    } else {
        Modifier
    }
    Canvas(modifier = modifier.aspectRatio(1f).then(tapModifier)) {
        val cell = size.width / SQUARES_COUNT
        val stroke = 1f
        ships.forEach { ship ->
            drawRect(
                color = colors.shipFill,
                topLeft = Offset(ship.y * cell, ship.x * cell),
                size = Size(cell, cell),
            )
        }
        selected?.let { sel ->
            drawRect(
                color = colors.selected,
                topLeft = Offset(sel.y * cell, sel.x * cell),
                size = Size(cell, cell),
            )
        }
        for (i in 0..SQUARES_COUNT) {
            drawLine(colors.foreground, Offset(0f, i * cell), Offset(size.width, i * cell), stroke)
            drawLine(colors.foreground, Offset(i * cell, 0f), Offset(i * cell, size.height), stroke)
        }
        dots.forEach { dot ->
            drawCircle(
                color = colors.foreground,
                radius = 4.dp.toPx(),
                center = Offset(dot.y * cell + cell / 2, dot.x * cell + cell / 2),
            )
        }
        crosses.forEach { cross ->
            val left = cross.y * cell
            val top = cross.x * cell
            drawLine(colors.foreground, Offset(left, top), Offset(left + cell, top + cell), stroke * 2.5f)
            drawLine(colors.foreground, Offset(left + cell, top), Offset(left, top + cell), stroke * 2.5f)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LabeledBoardPreview() {
    SeaBattleTheme {
        LabeledBoard(
            ships = listOf(Coordinate(0, 0), Coordinate(0, 1), Coordinate(5, 5)),
            crosses = listOf(Coordinate(3, 3)),
            dots = listOf(Coordinate(7, 7)),
            selected = Coordinate(9, 9),
            onCellTap = null,
        )
    }
}
