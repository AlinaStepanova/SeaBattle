package com.avs.battleship.main

import com.avs.battleship.*
import com.avs.battleship.battle_field.BaseBattleField
import com.avs.battleship.battle_field.Cell
import com.avs.battleship.battle_field.CellState.*
import com.avs.battleship.battle_field.Coordinate
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

    fun getCoordinateToShot(): Coordinate {
        var coordinate = Coordinate()
        if (firstCell.isState(EMPTY) || firstCell.isState(SHOT_FAILURE)) {
            coordinate = getRandomCoordinate()
            firstCell = Cell(coordinate.x, coordinate.y)
        } else if (firstCell.isState(SHOT_SUCCESS)
            && (secondCell.isState(EMPTY) || secondCell.isState(SHOT_FAILURE))
        ) {
            if (shipsLength.contains(TWO_DECK_SHIP_SIZE)
                || shipsLength.contains(THREE_DECK_SHIP_SIZE)
                || shipsLength.contains(FOUR_DECK_SHIP_SIZE)
            ) {
                coordinate = getNextCoordinateToShot(firstCell)
            }
            if (coordinate.x == -1) {
                shipsLength.remove(ONE_DECK_SHIP_SIZE)
                markHorizontalNeighbours(firstCell)
                markVerticalNeighbours(firstCell)
                coordinate = getRandomCoordinate()
            } else {
                secondCell = Cell(coordinate.x, coordinate.y)
            }
        } else if (firstCell.isState(SHOT_SUCCESS) && secondCell.isState(SHOT_SUCCESS)
            && (thirdCell.isState(EMPTY) || thirdCell.isState(SHOT_FAILURE))
        ) {
            if (shipsLength.contains(THREE_DECK_SHIP_SIZE)
                || shipsLength.contains(FOUR_DECK_SHIP_SIZE)
            ) {
                coordinate = checkNeighbourCells(firstCell, secondCell)
            }
            if (coordinate.x == -1) {
                shipsLength.remove(TWO_DECK_SHIP_SIZE)
                markEdgeCells(mutableListOf(firstCell.getCoordinate(), secondCell.getCoordinate()))
                resetValues()
                coordinate = getRandomCoordinate()
            } else {
                thirdCell = Cell(coordinate.x, coordinate.y)
            }
        } else if (firstCell.isState(SHOT_SUCCESS) && secondCell.isState(SHOT_SUCCESS)
            && thirdCell.isState(SHOT_SUCCESS)
            && (fourthCell.isState(EMPTY) || fourthCell.isState(SHOT_FAILURE))
        ) {
            if (shipsLength.contains(FOUR_DECK_SHIP_SIZE)) {
                coordinate = checkNeighbourCells(firstCell, thirdCell)
            }
            if (coordinate.x == -1) {
                shipsLength.remove(THREE_DECK_SHIP_SIZE)
                markEdgeCells(
                    mutableListOf(
                        firstCell.getCoordinate(),
                        secondCell.getCoordinate(),
                        thirdCell.getCoordinate()
                    )
                )
                resetValues()
                coordinate = getRandomCoordinate()
            } else {
                fourthCell = Cell(coordinate.x, coordinate.y)
            }
        } else {
            shipsLength.remove(FOUR_DECK_SHIP_SIZE)
            markEdgeCells(
                mutableListOf(
                    firstCell.getCoordinate(), secondCell.getCoordinate(),
                    thirdCell.getCoordinate(), fourthCell.getCoordinate()
                )
            )
            resetValues()
            coordinate = getRandomCoordinate()
        }
        return coordinate
    }

    private fun markEdgeCells(cells: MutableList<Coordinate>) {
        if (firstCell.getX() == secondCell.getX()) {
            val maxCoordinate = getMaxCoordinate(cells, Orientation.HORIZONTAL)
            val minCoordinate = getMinCoordinate(cells, Orientation.HORIZONTAL)
            battleField.setCellState(Coordinate(minCoordinate.x, minCoordinate.y - 1), SHOT_FAILURE)
            battleField.setCellState(Coordinate(maxCoordinate.x, maxCoordinate.y + 1), SHOT_FAILURE)
        } else if (firstCell.getY() == secondCell.getY()) {
            val maxCoordinate = getMaxCoordinate(cells, Orientation.VERTICAL)
            val minCoordinate = getMinCoordinate(cells, Orientation.VERTICAL)
            battleField.setCellState(Coordinate(minCoordinate.x - 1, minCoordinate.y), SHOT_FAILURE)
            battleField.setCellState(Coordinate(maxCoordinate.x + 1, maxCoordinate.y), SHOT_FAILURE)
        }
    }

    fun getMaxCoordinate(list: MutableList<Coordinate>, orientation: Orientation): Coordinate {
        return if (orientation == Orientation.VERTICAL) {
            list.maxBy { it.x }!!
        } else {
            list.maxBy { it.y }!!
        }
    }

    fun getMinCoordinate(list: MutableList<Coordinate>, orientation: Orientation): Coordinate {
        return if (orientation == Orientation.VERTICAL) {
            list.minBy { it.x }!!
        } else {
            list.minBy { it.y }!!
        }
    }

    private fun checkNeighbourCells(cell1: Cell, cell2: Cell): Coordinate {
        var coordinate = Coordinate()
        if (cell1.getX() == cell2.getX()) {
            coordinate = checkVerticalCoordinates(cell2.getCoordinate(), cell1.getCoordinate())
        } else if (cell1.getY() == cell2.getY()) {
            coordinate = checkHorizontalCoordinates(cell2.getCoordinate(), cell1.getCoordinate())
        }
        return coordinate
    }

    private fun checkHorizontalCoordinates(
        coordinateFirst: Coordinate,
        coordinateSecond: Coordinate
    ): Coordinate {
        var coordinate = Coordinate()
        when {
            isTopCellAvailable(coordinateFirst) -> {
                coordinate = Coordinate(coordinateFirst.x - 1, coordinateFirst.y)
            }
            isBottomCellAvailable(coordinateFirst) -> {
                coordinate = Coordinate(coordinateFirst.x + 1, coordinateFirst.y)
            }
            isTopCellAvailable(coordinateSecond) -> {
                coordinate = Coordinate(coordinateSecond.x - 1, coordinateSecond.y)
            }
            isBottomCellAvailable(coordinateSecond) -> {
                coordinate = Coordinate(coordinateSecond.x + 1, coordinateSecond.y)
            }
        }
        return coordinate
    }

    private fun checkVerticalCoordinates(
        coordinateFirst: Coordinate,
        coordinateSecond: Coordinate
    ): Coordinate {
        var coordinate = Coordinate()
        when {
            isLeftCellAvailable(coordinateFirst) -> {
                coordinate = Coordinate(coordinateFirst.x, coordinateFirst.y - 1)
            }
            isRightCellAvailable(coordinateFirst) -> {
                coordinate = Coordinate(coordinateFirst.x, coordinateFirst.y + 1)
            }
            isLeftCellAvailable(coordinateSecond) -> {
                coordinate = Coordinate(coordinateSecond.x, coordinateSecond.y - 1)
            }
            isRightCellAvailable(coordinateSecond) -> {
                coordinate = Coordinate(coordinateSecond.x, coordinateSecond.y + 1)
            }
        }
        return coordinate
    }

    private fun resetValues() {
        firstCell.setCellState(EMPTY)
        secondCell.setCellState(EMPTY)
        thirdCell.setCellState(EMPTY)
        fourthCell.setCellState(EMPTY)
    }

    private fun getNextCoordinateToShot(cell: Cell): Coordinate {
        var coordinate = Coordinate()
        when {
            isLeftCellAvailable(cell.getCoordinate()) -> {
                coordinate = Coordinate(cell.getX(), cell.getY() - 1)
            }
            isRightCellAvailable(cell.getCoordinate()) -> {
                coordinate = Coordinate(cell.getX(), cell.getY() + 1)
            }
            isTopCellAvailable(cell.getCoordinate()) -> {
                coordinate = Coordinate(cell.getX() - 1, cell.getY())
            }
            isBottomCellAvailable(cell.getCoordinate()) -> {
                coordinate = Coordinate(cell.getX() + 1, cell.getY())
            }
        }
        return coordinate
    }

    private fun getRandomCoordinate(): Coordinate {
        firstCell = Cell()
        do {
            firstCell.setCoordinates(
                (0 until SQUARES_COUNT).random(),
                (0 until SQUARES_COUNT).random()
            )
        } while (!battleField.isCellFreeToBeSelected(firstCell.getCoordinate()))
        return firstCell.getCoordinate()
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
        battleField.setCellState(Coordinate(cell.getX(), cell.getY() - 1), SHOT_FAILURE)
        battleField.setCellState(Coordinate(cell.getX(), cell.getY() + 1), SHOT_FAILURE)
    }

    private fun markVerticalNeighbours(cell: Cell) {
        battleField.setCellState(Coordinate(cell.getX() - 1, cell.getY()), SHOT_FAILURE)
        battleField.setCellState(Coordinate(cell.getX() + 1, cell.getY()), SHOT_FAILURE)
    }

    private fun updateBattleField(shipHit: Boolean, currentCell: Cell) {
        currentCell.setCellState(if (shipHit) SHOT_SUCCESS else SHOT_FAILURE)
        battleField.setCellState(currentCell.getCoordinate(), currentCell.getCellState())
        battleField.printBattleField()
    }

    private fun isLeftCellAvailable(coordinate: Coordinate): Boolean {
        val leftCell = Cell(coordinate.x, coordinate.y - 1)
        return battleField.isCellFreeToBeSelected(leftCell.getCoordinate())
    }

    private fun isRightCellAvailable(coordinate: Coordinate): Boolean {
        val rightCell = Cell(coordinate.x, coordinate.y + 1)
        return battleField.isCellFreeToBeSelected(rightCell.getCoordinate())
    }

    private fun isTopCellAvailable(coordinate: Coordinate): Boolean {
        val topCell = Cell(coordinate.x - 1, coordinate.y)
        return battleField.isCellFreeToBeSelected(topCell.getCoordinate())
    }

    private fun isBottomCellAvailable(coordinate: Coordinate): Boolean {
        val bottomCell = Cell(coordinate.x + 1, coordinate.y)
        return battleField.isCellFreeToBeSelected(bottomCell.getCoordinate())
    }
}