package com.avs.battleship.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.avs.battleship.battle_field.Coordinate

class PersonSquareView : SquareView {

    private lateinit var shipsCoordinates: ArrayList<Coordinate>
    private lateinit var crossesCoordinates: ArrayList<Coordinate>
    private lateinit var dotsCoordinates: ArrayList<Coordinate>

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
            for (coordinate in dotsCoordinates) {
                canvas?.drawDot(coordinate)
            }
        }
        if (crossesCoordinates.isNotEmpty()) {
            for (cross in crossesCoordinates) {
                canvas?.drawCross(cross.y, cross.x)
            }
        }
    }

    fun getShipsCoordinates(coordinates: ArrayList<Coordinate>) {
        this.shipsCoordinates = coordinates
        invalidate()
    }

    fun getCrossesCoordinates(coordinates: ArrayList<Coordinate>) {
        this.crossesCoordinates = coordinates
        invalidate()
    }

    fun getDotsCoordinates(coordinates: ArrayList<Coordinate>) {
        this.dotsCoordinates = coordinates
        invalidate()
    }
}