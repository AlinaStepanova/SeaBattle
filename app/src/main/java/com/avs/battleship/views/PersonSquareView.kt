package com.avs.battleship.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.util.AttributeSet

class PersonSquareView : SquareView {

    private lateinit var shipsCoordinates: ArrayList<Point>

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
    }

    fun getShipsCoordinates(coordinates: ArrayList<Point>) {
        this.shipsCoordinates = coordinates
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (shipsCoordinates.isNotEmpty()) {
            for (ship in shipsCoordinates) {
                canvas?.drawSquare(ship.y, ship.x)
            }
        }
    }
}