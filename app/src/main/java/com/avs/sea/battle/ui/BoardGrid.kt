package com.avs.sea.battle.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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

internal val LABEL_SIZE = 20.dp

@Composable
fun LabeledBoard(
    ships: List<Coordinate>,
    crosses: List<Coordinate>,
    dots: List<Coordinate>,
    selected: Coordinate?,
    onCellTap: ((Coordinate) -> Unit)?,
    modifier: Modifier = Modifier,
    style: BoardStyle = BoardStyle.Portrait,
    onGridPlaced: (Rect) -> Unit = {},
) {
    val labelStyle = TextStyle(
        color = SeaBattleTheme.colors.foreground,
        fontFamily = NeuchaFontFamily,
        fontSize = style.labelFontSize,
        textAlign = TextAlign.Center,
    )
    // The grid draws and maps taps left to right, so its labels must not mirror in RTL locales.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
            val boardSide = boardSideFor(maxWidth, maxHeight)
            Column {
                Row(Modifier.padding(start = LABEL_SIZE).size(width = boardSide, height = LABEL_SIZE)) {
                    LETTER_IDS.forEach { id ->
                        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            BasicText(text = stringResource(id), style = labelStyle)
                        }
                    }
                }
                Row {
                    Column(Modifier.size(width = LABEL_SIZE, height = boardSide)) {
                        NUMBER_IDS.forEach { id ->
                            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                        modifier = Modifier
                            .size(boardSide)
                            .onGloballyPositioned { onGridPlaced(it.boundsInRoot()) },
                        style = style,
                    )
                }
            }
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
    style: BoardStyle = BoardStyle.Portrait,
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
    Canvas(modifier = modifier.aspectRatio(1f).testTag(BOARD_GRID_TAG).then(tapModifier)) {
        val cell = size.width / SQUARES_COUNT
        val stroke = 1f
        val hatch = hatchStrokes(cell)
        ships.forEach { ship ->
            val corner = Offset(ship.y * cell, ship.x * cell)
            hatch.forEach { (start, end) ->
                drawLine(colors.shipHatch, corner + start, corner + end, stroke * 1.5f, cap = StrokeCap.Round)
            }
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
                radius = dotRadiusFor(cell, style),
                center = Offset(dot.y * cell + cell / 2, dot.x * cell + cell / 2),
            )
        }
        crosses.forEach { cross ->
            val left = cross.y * cell
            val top = cross.x * cell
            drawLine(colors.hit, Offset(left, top), Offset(left + cell, top + cell), stroke * 2.5f)
            drawLine(colors.hit, Offset(left + cell, top), Offset(left, top + cell), stroke * 2.5f)
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

internal const val BOARD_GRID_TAG = "board_grid"

// The letter row sits above the board and the number column to its left, so both
// labels take LABEL_SIZE away from the space available for the square board.
internal fun boardSideFor(maxWidth: Dp, maxHeight: Dp): Dp =
    (minOf(maxWidth, maxHeight) - LABEL_SIZE).coerceAtLeast(0.dp)

// Landscape boards are shorter, so they get smaller labels; portrait boards are wide enough
// for larger dots.
data class BoardStyle(val labelFontSize: TextUnit, val dotRadiusToCell: Float) {
    companion object {
        val Portrait = BoardStyle(labelFontSize = 18.sp, dotRadiusToCell = 0.08f)
        val Landscape = BoardStyle(labelFontSize = 15.sp, dotRadiusToCell = 0.05f)
    }
}

internal fun dotRadiusFor(cellSize: Float, style: BoardStyle): Float = cellSize * style.dotRadiusToCell
