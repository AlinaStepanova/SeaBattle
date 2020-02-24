package com.avs.battleship.main

import android.graphics.Point
import com.avs.battleship.*
import com.avs.battleship.battle_field.BaseBattleField
import com.avs.battleship.battle_field.Cell
import com.avs.battleship.battle_field.CellState.*
import com.avs.battleship.ships.Orientation

class ShotManager {

    private val battleField = BaseBattleField()

    private var firstCell = Cell()
    private var secondCell = Cell()
    private var thirdCell = Cell()
    private var fourthCell = Cell()

    private var shipsLength = mutableListOf(
        FOUR_DECK_SHIP_SIZE, THREE_DECK_SHIP_SIZE, THREE_DECK_SHIP_SIZE,
        TWO_DECK_SHIP_SIZE, TWO_DECK_SHIP_SIZE, TWO_DECK_SHIP_SIZE,
        ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE
    )

    fun getPointToShot(): Point {
        //todo assign to (-1, -1) here
        var point: Point
        if (firstCell.isState(EMPTY) || firstCell.isState(SHOT_FAILURE)) {
            point = getRandomPoint()
            firstCell = Cell(point.x, point.y)
        } else if (firstCell.isState(SHOT_SUCCESS)
            && (secondCell.isState(EMPTY) || secondCell.isState(SHOT_FAILURE))
        ) {
            point = if (shipsLength.contains(TWO_DECK_SHIP_SIZE)
                || shipsLength.contains(THREE_DECK_SHIP_SIZE)
                || shipsLength.contains(FOUR_DECK_SHIP_SIZE)
            ) {
                getNextPointToShot(firstCell)
            } else {
                //todo remove
                Point(-1, -1)
            }
            if (point.x == -1) {
                shipsLength.remove(ONE_DECK_SHIP_SIZE)
                //todo mark edge cells for one deck ship
                point = getRandomPoint()
            } else {
                secondCell = Cell(point.x, point.y)
            }
        } else if (firstCell.isState(SHOT_SUCCESS) && secondCell.isState(SHOT_SUCCESS)
            && (thirdCell.isState(EMPTY) || thirdCell.isState(SHOT_FAILURE))
        ) {
            point = if (shipsLength.contains(THREE_DECK_SHIP_SIZE)
                || shipsLength.contains(FOUR_DECK_SHIP_SIZE)
            ) {
                checkNeighbourCells(firstCell, secondCell)
            } else {
                Point(-1, -1)
            }
            if (point.x == -1) {
                shipsLength.remove(TWO_DECK_SHIP_SIZE)
                markEdgeCells(mutableListOf(firstCell.getPoint(), secondCell.getPoint()))
                resetValues()
                point = getRandomPoint()
            } else {
                thirdCell = Cell(point.x, point.y)
            }
        } else if (firstCell.isState(SHOT_SUCCESS) && secondCell.isState(SHOT_SUCCESS)
            && thirdCell.isState(SHOT_SUCCESS)
            && (fourthCell.isState(EMPTY) || fourthCell.isState(SHOT_FAILURE))
        ) {
            point = if (shipsLength.contains(FOUR_DECK_SHIP_SIZE)) {
                checkNeighbourCells(firstCell, thirdCell)
            } else {
                Point(-1, -1)
            }
            if (point.x == -1) {
                shipsLength.remove(THREE_DECK_SHIP_SIZE)
                markEdgeCells(
                    mutableListOf(
                        firstCell.getPoint(),
                        secondCell.getPoint(),
                        thirdCell.getPoint()
                    )
                )
                resetValues()
                point = getRandomPoint()
            } else {
                fourthCell = Cell(point.x, point.y)
            }
        } else {
            shipsLength.remove(FOUR_DECK_SHIP_SIZE)
            markEdgeCells(
                mutableListOf(
                    firstCell.getPoint(), secondCell.getPoint(),
                    thirdCell.getPoint(), fourthCell.getPoint()
                )
            )
            resetValues()
            point = getRandomPoint()
        }
        return point
    }

    private fun markEdgeCells(cells: MutableList<Point>) {
        if (firstCell.getX() == secondCell.getX()) {
            val maxPoint = getMaxPoint(cells, Orientation.HORIZONTAL)
            val minPoint = getMinPoint(cells, Orientation.HORIZONTAL)
            battleField.setCellState(Point(minPoint.x, minPoint.y - 1), SHOT_FAILURE)
            battleField.setCellState(Point(maxPoint.x, maxPoint.y + 1), SHOT_FAILURE)
        } else if (firstCell.getY() == secondCell.getY()) {
            val maxPoint = getMaxPoint(cells, Orientation.VERTICAL)
            val minPoint = getMinPoint(cells, Orientation.VERTICAL)
            battleField.setCellState(Point(minPoint.x - 1, minPoint.y), SHOT_FAILURE)
            battleField.setCellState(Point(maxPoint.x + 1, maxPoint.y), SHOT_FAILURE)
        }
    }

    private fun getMaxPoint(list: MutableList<Point>, orientation: Orientation): Point {
        return if (orientation == Orientation.VERTICAL) {
            list.maxBy { it.x }!!
        } else {
            list.maxBy { it.y }!!
        }
    }

    private fun getMinPoint(list: MutableList<Point>, orientation: Orientation): Point {
        return if (orientation == Orientation.VERTICAL) {
            list.minBy { it.x }!!
        } else {
            list.minBy { it.y }!!
        }
    }

    private fun checkNeighbourCells(cell1: Cell, cell2: Cell): Point {
        var point = Point(-1, -1)
        if (cell1.getX() == cell2.getX()) {
            point = checkVerticalPoints(cell2.getPoint(), cell1.getPoint())
        } else if (cell1.getY() == cell2.getY()) {
            point = checkHorizontalPoints(cell2.getPoint(), cell1.getPoint())
        }
        return point
    }

    private fun checkHorizontalPoints(pointFirst: Point, pointSecond: Point): Point {
        var point = Point(-1, -1)
        when {
            isTopCellAvailable(pointFirst) -> {
                point = Point(pointFirst.x - 1, pointFirst.y)
            }
            isBottomCellAvailable(pointFirst) -> {
                point = Point(pointFirst.x + 1, pointFirst.y)
            }
            isTopCellAvailable(pointSecond) -> {
                point = Point(pointSecond.x - 1, pointSecond.y)
            }
            isBottomCellAvailable(pointSecond) -> {
                point = Point(pointSecond.x + 1, pointSecond.y)
            }
        }
        return point
    }

    private fun checkVerticalPoints(pointFirst: Point, pointSecond: Point): Point {
        var point = Point(-1, -1)
        when {
            isLeftCellAvailable(pointFirst) -> {
                point = Point(pointFirst.x, pointFirst.y - 1)
            }
            isRightCellAvailable(pointFirst) -> {
                point = Point(pointFirst.x, pointFirst.y + 1)
            }
            isLeftCellAvailable(pointSecond) -> {
                point = Point(pointSecond.x, pointSecond.y - 1)
            }
            isRightCellAvailable(pointSecond) -> {
                point = Point(pointSecond.x, pointSecond.y + 1)
            }
        }
        return point
    }

    private fun resetValues() {
        //todo assign to (-1, -1)
        firstCell.setCellState(EMPTY)
        secondCell.setCellState(EMPTY)
        thirdCell.setCellState(EMPTY)
        fourthCell.setCellState(EMPTY)
    }

    private fun getNextPointToShot(cell: Cell): Point {
        var point = Point(-1, -1)
        when {
            isLeftCellAvailable(cell.getPoint()) -> {
                point = Point(cell.getX(), cell.getY() - 1)
            }
            isRightCellAvailable(cell.getPoint()) -> {
                point = Point(cell.getX(), cell.getY() + 1)
            }
            isTopCellAvailable(cell.getPoint()) -> {
                point = Point(cell.getX() - 1, cell.getY())
            }
            isBottomCellAvailable(cell.getPoint()) -> {
                point = Point(cell.getX() + 1, cell.getY())
            }
        }
        return point
    }

    private fun getRandomPoint(): Point {
        firstCell = Cell()
        do {
            firstCell.setCoordinates(
                (0 until SQUARES_COUNT).random(),
                (0 until SQUARES_COUNT).random()
            )
        } while (!battleField.isCellFreeToBeSelected(firstCell.getPoint()))
        return firstCell.getPoint()
    }

    fun handleShot(shipHit: Boolean) {
        if (firstCell.isState(EMPTY) || firstCell.isState(SHOT_FAILURE)) {
            updateBattleField(shipHit, firstCell)
        } else if (firstCell.isState(SHOT_SUCCESS)
            && (secondCell.isState(EMPTY) || secondCell.isState(SHOT_FAILURE))
        ) {
            updateBattleField(shipHit, secondCell)
            if (shipHit) {
                markNeighbours(firstCell)
                markNeighbours(secondCell)
            }
        } else if (firstCell.isState(SHOT_SUCCESS) && secondCell.isState(SHOT_SUCCESS)
            && (thirdCell.isState(EMPTY) || thirdCell.isState(SHOT_FAILURE))
        ) {
            updateBattleField(shipHit, thirdCell)
            if (shipHit) {
                markNeighbours(thirdCell)
            }
        } else if (firstCell.isState(SHOT_SUCCESS) && secondCell.isState(SHOT_SUCCESS)
            && thirdCell.isState(SHOT_SUCCESS) && (fourthCell.isState(EMPTY)
                    || fourthCell.isState(SHOT_FAILURE))
        ) {
            updateBattleField(shipHit, fourthCell)
            if (shipHit) {
                markNeighbours(fourthCell)
            }
        }
    }

    private fun markNeighbours(cell: Cell) {
        if (firstCell.getX() == secondCell.getX()) {
            markVerticalNeighbours(cell)
        } else if (firstCell.getY() == secondCell.getY()) {
            markHorizontalNeighbours(cell)
        }
    }

    private fun markHorizontalNeighbours(cell: Cell) {
        battleField.setCellState(Point(cell.getX(), cell.getY() - 1), SHOT_FAILURE)
        battleField.setCellState(Point(cell.getX(), cell.getY() + 1), SHOT_FAILURE)
    }

    private fun markVerticalNeighbours(cell: Cell) {
        battleField.setCellState(Point(cell.getX() - 1, cell.getY()), SHOT_FAILURE)
        battleField.setCellState(Point(cell.getX() + 1, cell.getY()), SHOT_FAILURE)
    }

    private fun updateBattleField(shipHit: Boolean, currentCell: Cell) {
        currentCell.setCellState(if (shipHit) SHOT_SUCCESS else SHOT_FAILURE)
        battleField.setCellState(currentCell.getPoint(), currentCell.getCellState())
        battleField.printBattleField()
    }

    private fun isLeftCellAvailable(point: Point): Boolean {
        val leftCell = Cell(point.x, point.y - 1)
        return battleField.isCellFreeToBeSelected(leftCell.getPoint())
    }

    private fun isRightCellAvailable(point: Point): Boolean {
        val rightCell = Cell(point.x, point.y + 1)
        return battleField.isCellFreeToBeSelected(rightCell.getPoint())
    }

    private fun isTopCellAvailable(point: Point): Boolean {
        val topCell = Cell(point.x - 1, point.y)
        return battleField.isCellFreeToBeSelected(topCell.getPoint())
    }

    private fun isBottomCellAvailable(point: Point): Boolean {
        val bottomCell = Cell(point.x + 1, point.y)
        return battleField.isCellFreeToBeSelected(bottomCell.getPoint())
    }
}