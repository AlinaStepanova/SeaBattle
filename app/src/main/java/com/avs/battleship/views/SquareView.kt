package com.avs.battleship.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.avs.battleship.main.MainViewModel
import com.avs.battleship.R
import com.avs.battleship.SQUARES_COUNT

abstract class SquareView : View {

    private val lineWidth = 1f
    private val circleRadius = 4f
    private var screenHeight = 0f
    private var screenWidth = 0f
    private lateinit var paint: Paint
    private lateinit var paintSquare: Paint
    protected var squareWidth = 0f
    protected lateinit var viewModel: MainViewModel

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = lineWidth
        paintSquare = Paint()
        paintSquare.color = ContextCompat.getColor(context, R.color.greyTransparent)
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
        for (i in 1..SQUARES_COUNT) {
            canvas.drawLine(
                0f, (screenHeight - squareWidth * i), screenWidth,
                (screenHeight - squareWidth * i), paint
            )
        }
    }

    private fun drawVerticalLines(canvas: Canvas) {
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

    fun Canvas.drawDot(point: Point) {
        val pointF: PointF = getCirclePoint(point.x, point.y)
        this.drawCircle(pointF.x, pointF.y, circleRadius, paint)
    }

    fun Canvas.drawCross(i: Int, j: Int) {
        this.drawLines(getCrossCoordinates(i, j), paint)
    }

    fun Canvas.drawSquare(i: Int, j: Int) {
        this.drawPath(getSingleSquarePath(i, j), paintSquare)
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

    private fun getCirclePoint(
        i: Int,
        j: Int
    ): PointF {
        return PointF(
            (i * squareWidth + squareWidth / 2),
            (j * squareWidth + squareWidth / 2)
        )
    }

    protected fun getCrossCoordinates(
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
