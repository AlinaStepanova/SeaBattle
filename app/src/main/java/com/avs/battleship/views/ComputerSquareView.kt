package com.avs.battleship.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.avs.battleship.MAX_SHIP_SIZE

class ComputerSquareView : SquareView {

    private lateinit var pointsCoordinates: ArrayList<Point>
    private lateinit var crossesCoordinates: ArrayList<Point>

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
        pointsCoordinates = arrayListOf()
        crossesCoordinates = arrayListOf()
        setOnTouchListener(OnTouchListener(getCustomOnTouchListener()))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (pointsCoordinates.isNotEmpty()) {
            for (point in pointsCoordinates) {
                canvas?.drawDot(point)
            }
        }
        if (crossesCoordinates.isNotEmpty()) {
            for (cross in crossesCoordinates) {
                canvas?.drawCross(cross.x, cross.y)
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
        if (i == MAX_SHIP_SIZE) i--
        if (j == MAX_SHIP_SIZE) j--
        return Point(i, j)
    }
}