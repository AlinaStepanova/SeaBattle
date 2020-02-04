package com.avs.battleship.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.util.AttributeSet

class PersonSquareView : SquareView {

    private lateinit var shipsCoordinates: ArrayList<Point>
    private lateinit var crossesCoordinates: ArrayList<Point>
    private lateinit var dotsCoordinates: ArrayList<Point>

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
        shipsCoordinates = arrayListOf()
        dotsCoordinates = arrayListOf()
        crossesCoordinates = arrayListOf()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (shipsCoordinates.isNotEmpty()) {
            for (ship in shipsCoordinates) {
                canvas?.drawSquare(ship.y, ship.x, paintShipSquare)
            }
        }
        if (dotsCoordinates.isNotEmpty()) {
            for (point in dotsCoordinates) {
                canvas?.drawDot(point)
            }
        }
        if (crossesCoordinates.isNotEmpty()) {
            for (cross in crossesCoordinates) {
                canvas?.drawCross(cross.y, cross.x)
            }
        }
    }

    fun getShipsCoordinates(coordinates: ArrayList<Point>) {
        this.shipsCoordinates = coordinates
        invalidate()
    }

    fun getCrossesCoordinates(coordinates: java.util.ArrayList<Point>) {
        this.crossesCoordinates = coordinates
        invalidate()
    }

    fun getDotsCoordinates(coordinates: java.util.ArrayList<Point>) {
        this.dotsCoordinates = coordinates
        invalidate()
    }
}