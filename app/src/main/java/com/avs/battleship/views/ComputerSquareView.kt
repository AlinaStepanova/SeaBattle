package com.avs.battleship.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.avs.battleship.R
import com.avs.battleship.SQUARES_COUNT

class ComputerSquareView : SquareView {

    private lateinit var pointsCoordinates: ArrayList<Point>
    private lateinit var crossesCoordinates: ArrayList<Point>
    private lateinit var paintSelected: Paint
    private var selectedSquare: Point? = null

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
        paintSelected = Paint()
        paintSelected.color = ContextCompat.getColor(context, R.color.blue_selected)
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
        if (selectedSquare != null) {
            canvas?.drawSquare(selectedSquare!!.x, selectedSquare!!.y, paintSelected)
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
        if (i == SQUARES_COUNT) i--
        if (j == SQUARES_COUNT) j--
        return Point(i, j)
    }

    fun getSelectedPoint(selectedSquare: Point) {
        this.selectedSquare = Point(selectedSquare.y, selectedSquare.x)
        invalidate()
    }
}