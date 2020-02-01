package com.avs.battleship.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ComputerSquareView : SquareView {

    private lateinit var pointsList : ArrayList<Point>

    constructor(context: Context) : super(context) { init() }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {init()}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {init()}

    private fun init() {
        pointsList = arrayListOf()
        setOnTouchListener(OnTouchListener(getCustomOnTouchListener()))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (pointsList.isNotEmpty()) {
            for (point in pointsList) {
                canvas?.drawDot(point)
            }
        }
    }

    private fun getCustomOnTouchListener(): (View, MotionEvent) -> Boolean {
        return { _: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    viewModel.handlePCAreaClick(convertUICoordinates(event.x, event.y))
                }
            }
            true
        }
    }

    private fun convertUICoordinates(x: Float, y: Float): Point {
        var i = y.toInt() / squareWidth.toInt()
        var j = x.toInt() / squareWidth.toInt()
        if (i == 10) i--
        if (j == 10) j--
        return Point(i, j)
    }
}