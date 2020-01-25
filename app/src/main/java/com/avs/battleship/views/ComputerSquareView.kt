package com.avs.battleship.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ComputerSquareView : SquareView {

    private lateinit var pointsList : ArrayList<PointF>

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
                canvas?.drawCircle(point.x, point.y, circleRadius, paint)
            }
        }
    }

    private fun getCustomOnTouchListener(): (View, MotionEvent) -> Boolean {
        return { _: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    viewModel.handlePCAreaClick(event.x, event.y)
                }
            }
            true
        }
    }
}