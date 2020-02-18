package com.avs.battleship.main

import android.graphics.Point
import android.util.Log
import com.avs.battleship.*
import com.avs.battleship.battle_field.BaseBattleField
import com.avs.battleship.battle_field.Cell
import com.avs.battleship.battle_field.CellState

class ShotManager {

    private val battleField = BaseBattleField()

    private var latestCell = Cell()
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
        if (latestCell.getCellState() == CellState.EMPTY || latestCell.getCellState() == CellState.SHOT_FAILURE) {
            point = getRandomPoint()
            latestCell = Cell(point.x, point.y)
        } else if (latestCell.getCellState() == CellState.SHOT_SUCCESS
            && secondCell.getCellState() == CellState.EMPTY || secondCell.getCellState() == CellState.SHOT_FAILURE) {
            point = getNextPointToShot(latestCell)
            secondCell = Cell(point.x, point.y)
        } else if (latestCell.getCellState() == CellState.SHOT_SUCCESS
            && secondCell.getCellState() == CellState.SHOT_SUCCESS
            && (thirdCell.getCellState() == CellState.EMPTY || thirdCell.getCellState() == CellState.SHOT_FAILURE)) {
            if (latestCell.getI() == secondCell.getI()) {
                //think here
                if (isLeftCellAvailable(secondCell.getPoint())) {
                    point = Point(secondCell.getI(), secondCell.getJ() - 1)
                } else if (isRightCellAvailable(secondCell.getPoint())) {
                    point = Point(secondCell.getI(), secondCell.getJ() + 1)
                }
            } else if (latestCell.getJ() == secondCell.getJ()) {
                if (isTopCellAvailable(secondCell.getPoint())) {
                    point = Point(secondCell.getI() - 1, this.secondCell.getJ())
                } else if (isBottomCellAvailable(secondCell.getPoint())) {
                    point = Point(secondCell.getI() + 1, secondCell.getJ())
                }
            }
            if (point.x == -1) {
                // create method
                latestCell.setCellState(CellState.EMPTY)
                secondCell.setCellState(CellState.EMPTY)
                thirdCell.setCellState(CellState.EMPTY)
                fourthCell.setCellState(CellState.EMPTY)
                point = getRandomPoint()
            } else {
                thirdCell = Cell(point.x, point.y)
            }
        } else if (latestCell.getCellState() == CellState.SHOT_SUCCESS
            && secondCell.getCellState() == CellState.SHOT_SUCCESS && thirdCell.getCellState() == CellState.SHOT_SUCCESS) {
            if (latestCell.getI() == secondCell.getI()) {
                if (isLeftCellAvailable(thirdCell.getPoint())) {
                    point = Point(thirdCell.getI(), thirdCell.getJ() - 1)
                } else if (isRightCellAvailable(thirdCell.getPoint())) {
                    point = Point(thirdCell.getI(), thirdCell.getJ() + 1)
                } else if (isLeftCellAvailable(latestCell.getPoint())) {
                    point = Point(latestCell.getI(), latestCell.getJ() - 1)
                } else if (isRightCellAvailable(latestCell.getPoint())) {
                    point = Point(latestCell.getI(), latestCell.getJ() + 1)
                }
            } else if (thirdCell.getJ() == thirdCell.getJ()) {
                if (isTopCellAvailable(thirdCell.getPoint())) {
                    point = Point(thirdCell.getI() - 1, thirdCell.getJ())
                } else if (isBottomCellAvailable(thirdCell.getPoint())) {
                    point = Point(thirdCell.getI() + 1, thirdCell.getJ())
                } else if (isTopCellAvailable(latestCell.getPoint())) {
                    point = Point(latestCell.getI() - 1, latestCell.getJ())
                } else if (isBottomCellAvailable(latestCell.getPoint())) {
                    point = Point(latestCell.getI() + 1, latestCell.getJ())
                }
            }
            if (point.x == -1) {
                latestCell.setCellState(CellState.EMPTY)
                secondCell.setCellState(CellState.EMPTY)
                thirdCell.setCellState(CellState.EMPTY)
                fourthCell.setCellState(CellState.EMPTY)
                point = getRandomPoint()
            } else {
                fourthCell = Cell(point.x, point.y)
            }
        } else {
            latestCell.setCellState(CellState.EMPTY)
            secondCell.setCellState(CellState.EMPTY)
            thirdCell.setCellState(CellState.EMPTY)
            fourthCell.setCellState(CellState.EMPTY)
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
        latestCell = Cell()
        do {
            latestCell.setCoordinates(
                (0 until SQUARES_COUNT).random(),
                (0 until SQUARES_COUNT).random()
            )
        } while (!battleField.isCellFreeToBeSelected(latestCell.getPoint()))
        return latestCell.getPoint()
    }

    fun handleShot(shipHit: Boolean) {
        if (latestCell.getCellState() == CellState.EMPTY || latestCell.getCellState() == CellState.SHOT_FAILURE) {
            latestCell.setCellState(if (shipHit) CellState.SHOT_SUCCESS else CellState.SHOT_FAILURE)
            battleField.setCellState(latestCell.getPoint(), latestCell.getCellState())
        } else if (latestCell.getCellState() == CellState.SHOT_SUCCESS
            && (secondCell.getCellState() == CellState.EMPTY || secondCell.getCellState() == CellState.SHOT_FAILURE)
        ) {
            secondCell.setCellState(if (shipHit) CellState.SHOT_SUCCESS else CellState.SHOT_FAILURE)
            battleField.setCellState(secondCell.getPoint(), secondCell.getCellState())
        } else if (latestCell.getCellState() == CellState.SHOT_SUCCESS
            && secondCell.getCellState() == CellState.SHOT_SUCCESS
            && (thirdCell.getCellState() == CellState.EMPTY || thirdCell.getCellState() == CellState.SHOT_FAILURE)
        ) {
            thirdCell.setCellState(if (shipHit) CellState.SHOT_SUCCESS else CellState.SHOT_FAILURE)
            battleField.setCellState(thirdCell.getPoint(), thirdCell.getCellState())
        } else if (latestCell.getCellState() == CellState.SHOT_SUCCESS
            && secondCell.getCellState() == CellState.SHOT_SUCCESS && thirdCell.getCellState() == CellState.SHOT_SUCCESS
            && (fourthCell.getCellState() == CellState.EMPTY || fourthCell.getCellState() == CellState.SHOT_FAILURE)
        ) {
            fourthCell.setCellState(if (shipHit) CellState.SHOT_SUCCESS else CellState.SHOT_FAILURE)
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