package com.avs.battleship.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

abstract class SquareView : View {

    protected var squareWidth = 0.0
    protected var screenHeight = 0.0
    protected var screenWidth = 0.0
    protected lateinit var paint: Paint
    private val squaresCount = 10
    private val lineWidth = 1f

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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        squareWidth = (MeasureSpec.getSize(widthMeasureSpec).toDouble() / squaresCount)
        screenHeight = MeasureSpec.getSize(heightMeasureSpec).toDouble()
        screenWidth = MeasureSpec.getSize(widthMeasureSpec).toDouble()
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
                0f, (screenHeight - squareWidth * i).toFloat(), screenWidth.toFloat(),
                (screenHeight - squareWidth * i).toFloat(), paint
            )
        }
    }

    private fun drawVerticalLines(canvas: Canvas) {
        for (i in 1..squaresCount) {
            canvas.drawLine(
                i * squareWidth.toFloat(),
                0f,
                i * squareWidth.toFloat(),
                screenHeight.toFloat(),
                paint
            )
        }
    }
}