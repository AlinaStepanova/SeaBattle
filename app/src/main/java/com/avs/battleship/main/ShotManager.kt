package com.avs.battleship.main

import android.graphics.Point
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

    private val shipsLength: List<Int> = listOf(
        FOUR_DECK_SHIP_SIZE, THREE_DECK_SHIP_SIZE, THREE_DECK_SHIP_SIZE,
        TWO_DECK_SHIP_SIZE, TWO_DECK_SHIP_SIZE, TWO_DECK_SHIP_SIZE,
        ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE
    )

    fun getPointToShot(): Point {
        var point = Point(-1, -1)
        if (firstCell.isState(EMPTY) || firstCell.isState(SHOT_FAILURE)) {
            point = getRandomPoint()
            firstCell = Cell(point.x, point.y)
        } else if (firstCell.isState(SHOT_SUCCESS)
            && secondCell.isState(EMPTY) || secondCell.isState(SHOT_FAILURE)
        ) {
            point = getNextPointToShot(firstCell)
            secondCell = Cell(point.x, point.y)
        } else if (firstCell.isState(SHOT_SUCCESS) && secondCell.isState(SHOT_SUCCESS)
            && (thirdCell.isState(EMPTY) || thirdCell.isState(SHOT_FAILURE))
        ) {
            if (firstCell.getI() == secondCell.getI()) {
                //think here
                if (isLeftCellAvailable(secondCell.getPoint())) {
                    point = Point(secondCell.getI(), secondCell.getJ() - 1)
                } else if (isRightCellAvailable(secondCell.getPoint())) {
                    point = Point(secondCell.getI(), secondCell.getJ() + 1)
                }
            } else if (firstCell.getJ() == secondCell.getJ()) {
                if (isTopCellAvailable(secondCell.getPoint())) {
                    point = Point(secondCell.getI() - 1, this.secondCell.getJ())
                } else if (isBottomCellAvailable(secondCell.getPoint())) {
                    point = Point(secondCell.getI() + 1, secondCell.getJ())
                }
            }
            if (point.x == -1) {
                // create method
                firstCell.setCellState(EMPTY)
                secondCell.setCellState(EMPTY)
                thirdCell.setCellState(EMPTY)
                fourthCell.setCellState(EMPTY)
                point = getRandomPoint()
            } else {
                thirdCell = Cell(point.x, point.y)
            }
        } else if (firstCell.isState(SHOT_SUCCESS) && secondCell.isState(SHOT_SUCCESS)
            && thirdCell.isState(SHOT_SUCCESS)
        ) {
            if (firstCell.getI() == secondCell.getI()) {
                if (isLeftCellAvailable(thirdCell.getPoint())) {
                    point = Point(thirdCell.getI(), thirdCell.getJ() - 1)
                } else if (isRightCellAvailable(thirdCell.getPoint())) {
                    point = Point(thirdCell.getI(), thirdCell.getJ() + 1)
                } else if (isLeftCellAvailable(firstCell.getPoint())) {
                    point = Point(firstCell.getI(), firstCell.getJ() - 1)
                } else if (isRightCellAvailable(firstCell.getPoint())) {
                    point = Point(firstCell.getI(), firstCell.getJ() + 1)
                }
            } else if (thirdCell.getJ() == thirdCell.getJ()) {
                if (isTopCellAvailable(thirdCell.getPoint())) {
                    point = Point(thirdCell.getI() - 1, thirdCell.getJ())
                } else if (isBottomCellAvailable(thirdCell.getPoint())) {
                    point = Point(thirdCell.getI() + 1, thirdCell.getJ())
                } else if (isTopCellAvailable(firstCell.getPoint())) {
                    point = Point(firstCell.getI() - 1, firstCell.getJ())
                } else if (isBottomCellAvailable(firstCell.getPoint())) {
                    point = Point(firstCell.getI() + 1, firstCell.getJ())
                }
            }
            if (point.x == -1) {
                firstCell.setCellState(EMPTY)
                secondCell.setCellState(EMPTY)
                thirdCell.setCellState(EMPTY)
                fourthCell.setCellState(EMPTY)
                point = getRandomPoint()
            } else {
                fourthCell = Cell(point.x, point.y)
            }
        } else {
            firstCell.setCellState(EMPTY)
            secondCell.setCellState(EMPTY)
            thirdCell.setCellState(EMPTY)
            fourthCell.setCellState(EMPTY)
            point = getRandomPoint()
        }
        return point
    }

    private fun getNextPointToShot(cell: Cell): Point {
        var point: Point? = null
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
        return point ?: getRandomPoint()
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
            firstCell.setCellState(if (shipHit) SHOT_SUCCESS else SHOT_FAILURE)
            battleField.setCellState(firstCell.getPoint(), firstCell.getCellState())
        } else if (firstCell.isState(SHOT_SUCCESS)
            && (secondCell.isState(EMPTY) || secondCell.isState(SHOT_FAILURE))
        ) {
            secondCell.setCellState(if (shipHit) SHOT_SUCCESS else SHOT_FAILURE)
            battleField.setCellState(secondCell.getPoint(), secondCell.getCellState())
        } else if (firstCell.isState(SHOT_SUCCESS) && secondCell.isState(SHOT_SUCCESS)
            && (thirdCell.isState(EMPTY) || thirdCell.isState(SHOT_FAILURE))
        ) {
            thirdCell.setCellState(if (shipHit) SHOT_SUCCESS else SHOT_FAILURE)
            battleField.setCellState(thirdCell.getPoint(), thirdCell.getCellState())
        } else if (firstCell.isState(SHOT_SUCCESS) && secondCell.isState(SHOT_SUCCESS)
            && thirdCell.isState(SHOT_SUCCESS) && (fourthCell.isState(EMPTY)
                    || fourthCell.isState(SHOT_FAILURE))
        ) {
            fourthCell.setCellState(if (shipHit) SHOT_SUCCESS else SHOT_FAILURE)
            battleField.setCellState(fourthCell.getPoint(), fourthCell.getCellState())
        }
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