package com.avs.battleship.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.avs.battleship.MainViewModel

abstract class SquareView : View {

    private val squaresCount = 10
    private val lineWidth = 1f
    protected val circleRadius = 4f
    protected var squareWidth = 0f
    protected var screenHeight = 0f
    protected var screenWidth = 0f
    protected lateinit var paint: Paint
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
    }

    fun provideViewModel(mainViewModel: MainViewModel) {
        this.viewModel = mainViewModel
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        squareWidth = (MeasureSpec.getSize(widthMeasureSpec).toFloat() / squaresCount)
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
        for (i in 1..squaresCount) {
            canvas.drawLine(
                0f, (screenHeight - squareWidth * i), screenWidth,
                (screenHeight - squareWidth * i), paint
            )
        }
    }

    private fun drawVerticalLines(canvas: Canvas) {
        for (i in 1..squaresCount) {
            canvas.drawLine(
                i * squareWidth,
                0f,
                i * squareWidth,
                screenHeight,
                paint
            )
        }
    }

    protected fun getSingleSquarePath(
        i: Int,
        j: Int
    ): Path? {
        val path = Path()
        val delta: Float = (j * squareWidth)
        path.moveTo(i * squareWidth, delta)
        path.lineTo(i * squareWidth, delta + squareWidth)
        path.lineTo((i * squareWidth + squareWidth), delta + squareWidth)
        path.lineTo((i * squareWidth + squareWidth), delta)
        path.close()
        return path
    }

    protected fun getCirclePoint(
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
