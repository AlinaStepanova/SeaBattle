package com.avs.sea.battle.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.avs.sea.battle.R
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.battle_field.CoordinateF
import com.avs.sea.battle.main.MainViewModel

abstract class SquareView : View {

    private val lineWidth = 1f
    private var circleRadius = 1f
    private var screenHeight = 0f
    private var screenWidth = 0f
    private lateinit var paint: Paint
    protected lateinit var paintShipSquare: Paint
    protected var squareWidth = 0f
    protected lateinit var viewModel: MainViewModel

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        circleRadius = getFloatValue(context)
        paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = lineWidth
        paintShipSquare = Paint()
        paintShipSquare.color = ContextCompat.getColor(context, R.color.greyTransparent)
    }

    private fun getFloatValue(context: Context): Float {
        val out = TypedValue()
        context.resources.getValue(R.dimen.circle_radius, out, true)
        return out.float
    }

    fun provideViewModel(mainViewModel: MainViewModel) {
        this.viewModel = mainViewModel
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        squareWidth = (MeasureSpec.getSize(widthMeasureSpec).toFloat() / SQUARES_COUNT)
        screenHeight = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        screenWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        paint.color = Color.BLACK
        paint.strokeWidth = lineWidth
        drawHorizontalLines(canvas!!)
        drawVerticalLines(canvas)
    }

    private fun drawHorizontalLines(canvas: Canvas) {
        paint.strokeWidth = lineWidth
        for (i in 1..SQUARES_COUNT) {
            canvas.drawLine(
                0f, (screenHeight - squareWidth * i), screenWidth,
                (screenHeight - squareWidth * i), paint
            )
        }
    }

    private fun drawVerticalLines(canvas: Canvas) {
        paint.strokeWidth = lineWidth
        for (i in 1..SQUARES_COUNT) {
            canvas.drawLine(
                i * squareWidth,
                0f,
                i * squareWidth,
                screenHeight,
                paint
            )
        }
    }

    fun Canvas.drawDot(coordinate: Coordinate) {
        val coordinateF: CoordinateF = getCircleCoordinate(coordinate.x, coordinate.y)
        this.drawCircle(coordinateF.y, coordinateF.x, circleRadius, paint)
    }

    fun Canvas.drawCross(i: Int, j: Int) {
        paint.strokeWidth = lineWidth * 2
        this.drawLines(getCrossCoordinates(i, j), paint)
    }

    fun Canvas.drawSquare(i: Int, j: Int, paint: Paint) {
        this.drawPath(getSingleSquarePath(i, j), paint)
    }

    private fun getSingleSquarePath(
        i: Int,
        j: Int
    ): Path {
        val path = Path()
        val delta: Float = (j * squareWidth)
        path.moveTo(i * squareWidth, delta)
        path.lineTo(i * squareWidth, delta + squareWidth)
        path.lineTo((i * squareWidth + squareWidth), delta + squareWidth)
        path.lineTo((i * squareWidth + squareWidth), delta)
        path.close()
        return path
    }

    private fun getCircleCoordinate(
        i: Int,
        j: Int
    ): CoordinateF {
        return CoordinateF(
            (i * squareWidth + squareWidth / 2),
            (j * squareWidth + squareWidth / 2)
        )
    }

    private fun getCrossCoordinates(
        i: Int,
        j: Int
    ): FloatArray {
        val pts = FloatArray(8)
        pts[0] = (i * squareWidth)
        pts[1] = (j * squareWidth)
        pts[2] = (i * squareWidth + squareWidth)
        pts[3] = (j * squareWidth + squareWidth)

        pts[4] = (i * squareWidth + squareWidth)
        pts[5] = (j * squareWidth)
        pts[6] = (i * squareWidth)
        pts[7] = (j * squareWidth + squareWidth)
        return pts
    }
}
