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

    init {
        initBattleShip()
    }

    fun initBattleShip() {
        for (i in battleField.indices) {
            for (j in battleField[i].indices) {
                battleField[i][j] = Cell(i, j)
            }
        }
    }

    fun randomizeShips() {
        var isAdded: Boolean
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
        printBattleField()
    }

    fun handleShot(point: Point?) : Boolean {
        var isShipHit = false
        if (point != null) {
            if (battleField[point.x][point.y]?.getCellState() == CellState.EMPTY) {
                battleField[point.x][point.y]?.setCellState(CellState.SHOT_FAILURE)
            } else {
                battleField[point.x][point.y]?.setCellState(CellState.SHOT_SUCCESS)
                isShipHit = true
            }
        }
        return isShipHit
    }

    fun isCellFreeToBeSelected(point: Point): Boolean {
        return (point.x in 0..SQUARES_COUNT
                && point.y in 0..SQUARES_COUNT
                && (battleField[point.x][point.y]?.getCellState() == CellState.EMPTY
                || battleField[point.x][point.y]?.getCellState() == CellState.SHIP))
    }

    fun getShipsCoordinates(): ArrayList<Point> {
        val shipsCoordinates = arrayListOf<Point>()
        for (i in battleField.indices) {
            for (j in battleField[i].indices) {
                if (battleField[i][j]?.getCellState() == CellState.SHIP) {
                    shipsCoordinates.add(Point(i, j))
                }
            }
        }
        return shipsCoordinates
    }

    fun getDotsCoordinates(): ArrayList<Point> {
        val dotsCoordinates = arrayListOf<Point>()
        for (i in battleField.indices) {
            for (j in battleField[i].indices) {
                if (battleField[i][j]?.getCellState() == CellState.SHOT_FAILURE) {
                    dotsCoordinates.add(Point(i, j))
                }
            }
        }
        return dotsCoordinates
    }

    fun getCrossesCoordinates(): ArrayList<Point> {
        val crossesCoordinates = arrayListOf<Point>()
        for (i in battleField.indices) {
            for (j in battleField[i].indices) {
                if (battleField[i][j]?.getCellState() == CellState.SHOT_SUCCESS) {
                    crossesCoordinates.add(Point(i, j))
                }
            }
        }
        return crossesCoordinates
    }

    private fun isStartCellEmpty(point: Point): Boolean {
        return ((point.y == 0) || (point.y > 0 &&
                battleField[point.x][point.y - 1]?.getCellState() == CellState.EMPTY))
    }

    private fun isEndCellEmpty(
        point: Point,
        length: Int
    ): Boolean {
        return ((point.y + length == SQUARES_COUNT - 1) || (point.y < SQUARES_COUNT - 1 &&
                battleField[point.x][point.y + length]?.getCellState() == CellState.EMPTY))
    }

    private fun isUpCellEmpty(point: Point): Boolean {
        return ((point.x == 0) || (point.x > 0 &&
                battleField[point.x - 1][point.y]?.getCellState() == CellState.EMPTY))
    }

    private fun isBottomCellEmpty(
        point: Point,
        length: Int
    ): Boolean {
        return ((point.x + length == SQUARES_COUNT - 1) || (point.x < SQUARES_COUNT - 1 &&
                battleField[point.x + length][point.y]?.getCellState() == CellState.EMPTY))
    }

    private fun isLeftSideEmpty(point: Point, length: Int): Boolean {
        var isEmpty = true
        if (point.y > 0) {
            for (i in 0 until length) {
                if (battleField[point.x + i][point.y - 1]?.getCellState() != CellState.EMPTY) {
                    isEmpty = false
                    break
                }
            }
        }
        return isEmpty
    }

    private fun isRightSideEmpty(point: Point, length: Int): Boolean {
        var isEmpty = true
        if (point.y < SQUARES_COUNT - 1) {
            for (i in 0 until length) {
                if (battleField[point.x + i][point.y + 1]?.getCellState() != CellState.EMPTY) {
                    isEmpty = false
                    break
                }
            }
        }
        return isEmpty
    }

    private fun isVerticalCellsEmpty(point: Point, length: Int): Boolean {
        var isEmpty = true
        for (i in 0 until length) {
            if (battleField[point.x + i][point.y]?.getCellState() != CellState.EMPTY) {
                isEmpty = false
                break
            }
        }
        return isEmpty
    }

    private fun isHorizontalCellsEmpty(point: Point, length: Int): Boolean {
        var isEmpty = true
        for (i in 0 until length) {
            if (battleField[point.x][point.y + i]?.getCellState() != CellState.EMPTY) {
                isEmpty = false
                break
            }
        }
        return isEmpty
    }

    private fun isTopSideEmpty(point: Point, length: Int): Boolean {
        var isEmpty = true
        if (point.x > 0) {
            for (i in 0 until length) {
                if (battleField[point.x - 1][point.y + i]?.getCellState() != CellState.EMPTY) {
                    isEmpty = false
                    break
                }
            }
        }
        return isEmpty
    }

    private fun isBottomSideEmpty(point: Point, length: Int): Boolean {
        var isEmpty = true
        if (point.x < SQUARES_COUNT - 1) {
            for (i in 0 until length) {
                if (battleField[point.x + 1][point.y + i]?.getCellState() != CellState.EMPTY) {
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