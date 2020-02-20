package com.avs.battleship.main

import android.graphics.Point
import android.util.Log
import com.avs.battleship.*
import com.avs.battleship.battle_field.BaseBattleField
import com.avs.battleship.battle_field.Cell
import com.avs.battleship.battle_field.CellState.*

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
        var point: Point
        if (firstCell.isState(EMPTY) || firstCell.isState(SHOT_FAILURE)) {
            point = getRandomPoint()
            firstCell = Cell(point.x, point.y)
        } else if (firstCell.isState(SHOT_SUCCESS)
            && (secondCell.isState(EMPTY) || secondCell.isState(SHOT_FAILURE))
        ) {
            point = getNextPointToShot(firstCell)
            if (point.x == -1) {
                shipsLength.remove(ONE_DECK_SHIP_SIZE)
                point = getRandomPoint()
            } else {
                secondCell = Cell(point.x, point.y)
            }
        } else if (firstCell.isState(SHOT_SUCCESS) && secondCell.isState(SHOT_SUCCESS)
            && (thirdCell.isState(EMPTY) || thirdCell.isState(SHOT_FAILURE))
        ) {
            point = checkNeighbourCells(firstCell, secondCell)
            if (point.x == -1) {
                shipsLength.remove(TWO_DECK_SHIP_SIZE)
                resetValues()
                /*if (firstCell.getI() == secondCell.getI()) {
                    if (firstCell.getJ() < secondCell.getJ()) {
                        battleField.setCellState(Point(firstCell.getI(), firstCell.getJ() - 1), SHOT_FAILURE)
                        battleField.setCellState(Point(secondCell.getI(), secondCell.getJ() + 1), SHOT_FAILURE)
                    } else {
                        battleField.setCellState(Point(secondCell.getI(), secondCell.getJ() - 1), SHOT_FAILURE)
                        battleField.setCellState(Point(firstCell.getI(), firstCell.getJ() + 1), SHOT_FAILURE)
                    }
                } else if (firstCell.getJ() == secondCell.getJ()) {
                    if (firstCell.getI() < secondCell.getI()) {
                        battleField.setCellState(Point(firstCell.getI() - 1, firstCell.getJ()), SHOT_FAILURE)
                        battleField.setCellState(Point(secondCell.getI() + 1, secondCell.getJ()), SHOT_FAILURE)
                    } else {
                        battleField.setCellState(Point(secondCell.getI() - 1, secondCell.getJ()), SHOT_FAILURE)
                        battleField.setCellState(Point(firstCell.getI() + 1, firstCell.getJ()), SHOT_FAILURE)
                    }
                }*/
                point = getRandomPoint()
            } else {
                thirdCell = Cell(point.x, point.y)
            }
        } else if (firstCell.isState(SHOT_SUCCESS) && secondCell.isState(SHOT_SUCCESS)
            && thirdCell.isState(SHOT_SUCCESS)
        ) {
            point = checkNeighbourCells(firstCell, thirdCell)
            if (point.x == -1) {
                shipsLength.remove(THREE_DECK_SHIP_SIZE)
                resetValues()
                point = getRandomPoint()
            } else {
                fourthCell = Cell(point.x, point.y)
            }
        } else {
            resetValues()
            point = getRandomPoint()
        }
        return point
    }

    private fun checkNeighbourCells(cell1: Cell, cell2: Cell): Point {
        var point = Point(-1, -1)
        if (cell1.getI() == cell2.getI()) {
            point = checkVerticalPoints(cell2.getPoint(), cell1.getPoint())
        } else if (cell1.getJ() == cell2.getJ()) {
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
        firstCell.setCellState(EMPTY)
        secondCell.setCellState(EMPTY)
        thirdCell.setCellState(EMPTY)
        fourthCell.setCellState(EMPTY)
    }

    private fun getNextPointToShot(cell: Cell): Point {
        var point = Point(-1, -1)
        when {
            isLeftCellAvailable(cell.getPoint()) -> {
                point = Point(cell.getI(), cell.getJ() - 1)
            }
            isRightCellAvailable(cell.getPoint()) -> {
                point = Point(cell.getI(), cell.getJ() + 1)
            }
            isTopCellAvailable(cell.getPoint()) -> {
                point = Point(cell.getI() - 1, cell.getJ())
            }
            isBottomCellAvailable(cell.getPoint()) -> {
                point = Point(cell.getI() + 1, cell.getJ())
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
                shipsLength.remove(FOUR_DECK_SHIP_SIZE)
                markNeighbours(fourthCell)
            }
        }
    }

    private fun markNeighbours(cell: Cell) {
        if (firstCell.getI() == secondCell.getI()) {
            markVerticalNeighbours(cell)
        } else if (firstCell.getJ() == secondCell.getJ()) {
            markHorizontalNeighbours(cell)
        }
    }

    private fun markHorizontalNeighbours(cell: Cell) {
        battleField.setCellState(Point(cell.getI(), cell.getJ() - 1), SHOT_FAILURE)
        battleField.setCellState(Point(cell.getI(), cell.getJ() + 1), SHOT_FAILURE)
    }

    private fun markVerticalNeighbours(cell: Cell) {
        battleField.setCellState(Point(cell.getI() - 1, cell.getJ()), SHOT_FAILURE)
        battleField.setCellState(Point(cell.getI() + 1, cell.getJ()), SHOT_FAILURE)
    }

    private fun updateBattleField(shipHit: Boolean, currentCell: Cell) {
        currentCell.setCellState(if (shipHit) SHOT_SUCCESS else SHOT_FAILURE)
        battleField.setCellState(currentCell.getPoint(), currentCell.getCellState())
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