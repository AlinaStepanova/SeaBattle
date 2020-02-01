package com.avs.battleship.battle_field

import android.graphics.Point
import android.util.Log
import com.avs.battleship.SQUARES_COUNT
import com.avs.battleship.ships.*

class BattleField {

    private val battleField =
        Array(SQUARES_COUNT) { arrayOfNulls<Cell>(SQUARES_COUNT) }

    private val ships: List<Ship> = listOf(
        FourDeckShip(), ThreeDeckShip(), ThreeDeckShip(),
        TwoDeckShip(), TwoDeckShip(), TwoDeckShip(),
        OneDeckShip(), OneDeckShip(), OneDeckShip(), OneDeckShip()
    )

    private var isAdded = false

    init {
        initBattleShip()
        randomizeShips()
        printBattleField()
    }

    private fun initBattleShip() {
        for (i in battleField.indices) {
            for (j in battleField[i].indices) {
                battleField[i][j] = Cell(i, j)
            }
        }
    }

    private fun randomizeShips() {
        for (ship in ships) {
            isAdded = false
            while (!isAdded) {
                val point = getRandomPoint(ship)
                if (battleField[point.x][point.y]?.getCellState() == CellState.EMPTY) {
                    isAdded = if (ship.getShipOrientation() == Orientation.VERTICAL) {
                        (isUpCellEmpty(point) && isBottomCellEmpty(point, ship.getLength()) &&
                                isVerticalCellsEmpty(point, ship.getLength()) &&
                                isLeftSideEmpty(point, ship.getLength()) &&
                                isRightSideEmpty(point, ship.getLength()))
                    } else {
                        (isStartCellEmpty(point) && isEndCellEmpty(point, ship.getLength()) &&
                                isHorizontalCellsEmpty(point, ship.getLength()) &&
                                isTopSideEmpty(point, ship.getLength()) &&
                                isBottomSideEmpty(point, ship.getLength()))
                    }
                    if (isAdded) {
                        ship.setCellsCoordinates(point.x, point.y)
                        for (cell in ship.getShipCells()) {
                            battleField[cell.getI()][cell.getJ()]?.setCellState(cell.getCellState())
                        }
                    }
                }
            }
        }
    }

    private fun isEndCellEmpty(
        point: Point,
        length: Int
    ): Boolean {
        val copyPoint = Point(point.x, point.y)
        return ((copyPoint.y + length == SQUARES_COUNT - 1) || (copyPoint.y < SQUARES_COUNT - 1 &&
                battleField[copyPoint.x][copyPoint.y + length]?.getCellState() == CellState.EMPTY))
    }

    private fun isStartCellEmpty(point: Point): Boolean {
        val copyPoint = Point(point.x, point.y)
        return ((copyPoint.y == 0) || (copyPoint.y > 0 &&
                battleField[copyPoint.x][copyPoint.y - 1]?.getCellState() == CellState.EMPTY))
    }

    private fun isBottomCellEmpty(
        point: Point,
        length: Int
    ): Boolean {
        val copyPoint = Point(point.x, point.y)
        return ((copyPoint.x + length == SQUARES_COUNT - 1) || (copyPoint.x < SQUARES_COUNT - 1 &&
                battleField[copyPoint.x + length][copyPoint.y]?.getCellState() == CellState.EMPTY))
    }

    private fun isUpCellEmpty(point: Point): Boolean {
        val copyPoint = Point(point.x, point.y)
        return ((copyPoint.x == 0) || (copyPoint.x > 0 &&
                battleField[copyPoint.x - 1][copyPoint.y]?.getCellState() == CellState.EMPTY))
    }

    private fun isLeftSideEmpty(point: Point, length: Int): Boolean {
        val copyPoint = Point(point.x, point.y)
        var isEmpty = true
        if (copyPoint.y > 0) {
            for (i in 0 until length) {
                if (battleField[copyPoint.x + i][copyPoint.y - 1]?.getCellState() != CellState.EMPTY) {
                    isEmpty = false
                    break
                }
            }
        }
        return isEmpty
    }

    private fun isVerticalCellsEmpty(point: Point, length: Int): Boolean {
        val copyPoint = Point(point.x, point.y)
        var isEmpty = true
        for (i in 0 until length) {
            if (battleField[copyPoint.x + i][copyPoint.y]?.getCellState() != CellState.EMPTY) {
                isEmpty = false
                break
            }
        }
        return isEmpty
    }

    private fun isRightSideEmpty(point: Point, length: Int): Boolean {
        val copyPoint = Point(point.x, point.y)
        var isEmpty = true
        if (copyPoint.y < SQUARES_COUNT - 1) {
            for (i in 0 until length) {
                if (battleField[copyPoint.x + i][copyPoint.y + 1]?.getCellState() != CellState.EMPTY) {
                    isEmpty = false
                    break
                }
            }
        }
        return isEmpty
    }

    private fun isHorizontalCellsEmpty(point: Point, length: Int): Boolean {
        val copyPoint = Point(point.x, point.y)
        var isEmpty = true
        for (i in 0 until length) {
            if (battleField[copyPoint.x][copyPoint.y + i]?.getCellState() != CellState.EMPTY) {
                isEmpty = false
                break
            }
        }
        return isEmpty
    }

    private fun isTopSideEmpty(point: Point, length: Int): Boolean {
        val copyPoint = Point(point.x, point.y)
        var isEmpty = true
        if (copyPoint.x > 0) {
            for (i in 0 until length) {
                if (battleField[copyPoint.x - 1][copyPoint.y + i]?.getCellState() != CellState.EMPTY) {
                    isEmpty = false
                    break
                }
            }
        }
        return isEmpty
    }

    private fun isBottomSideEmpty(point: Point, length: Int): Boolean {
        val copyPoint = Point(point.x, point.y)
        var isEmpty = true
        if (copyPoint.x < SQUARES_COUNT - 1) {
            for (i in 0 until length) {
                if (battleField[copyPoint.x + 1][copyPoint.y + i]?.getCellState() != CellState.EMPTY) {
                    isEmpty = false
                    break
                }
            }
        }
        return isEmpty
    }

    private fun getRandomPoint(ship: Ship): Point {
        val point = Point()
        if (ship.getShipOrientation() == Orientation.HORIZONTAL) {
            point.x = (0 until SQUARES_COUNT).random()
            point.y = (0 until SQUARES_COUNT - ship.getLength() - 1).random()
        } else {
            point.x = (0 until SQUARES_COUNT - ship.getLength() - 1).random()
            point.y = (0 until SQUARES_COUNT).random()
        }
        return point
    }

    private fun printBattleField() {
        var result = "\n"
        for (i in battleField.indices) {
            for (j in battleField[i].indices) {
                result += battleField[i][j]?.getCellState()?.state + " "
            }
            result += "\n"
        }
        Log.d(this.javaClass.simpleName, result)
    }
}