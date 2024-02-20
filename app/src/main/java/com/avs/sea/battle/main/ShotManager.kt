package com.avs.sea.battle.main

import com.avs.sea.battle.*
import com.avs.sea.battle.battle_field.BaseBattleField
import com.avs.sea.battle.battle_field.Cell
import com.avs.sea.battle.battle_field.CellState.*
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.ships.Orientation

class ShotManager {

    private val battleField = BaseBattleField()

    private var firstCell = Cell()
    private var secondCell = Cell()
    private var thirdCell = Cell()
    private var fourthCell = Cell()

    private var ships = mutableListOf(
        FOUR_DECK_SHIP_SIZE, THREE_DECK_SHIP_SIZE, THREE_DECK_SHIP_SIZE,
        TWO_DECK_SHIP_SIZE, TWO_DECK_SHIP_SIZE, TWO_DECK_SHIP_SIZE,
        ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE
    )

    fun getShipsSize(): Int = ships.size

    fun getBattleField(): BaseBattleField = battleField

    fun getFirstCell(): Cell = firstCell

    fun getSecondCell(): Cell = secondCell

    fun getThirdCell(): Cell = thirdCell

    fun getFourthCell(): Cell = fourthCell

    fun getCoordinateToShot(): Coordinate {
        var coordinate = Coordinate()
        if (firstCell.isState(EMPTY) || firstCell.isState(SHOT_FAILURE)) {
            coordinate = getRandomCoordinate()
            firstCell = Cell(coordinate.x, coordinate.y)
        } else if (firstCell.isState(SHOT_SUCCESS)
            && (secondCell.isState(EMPTY) || secondCell.isState(SHOT_FAILURE))
        ) {
            coordinate = shotSecondCell(coordinate)
        } else if (firstCell.isState(SHOT_SUCCESS) && secondCell.isState(SHOT_SUCCESS)
            && (thirdCell.isState(EMPTY) || thirdCell.isState(SHOT_FAILURE))
        ) {
            coordinate = shotThirdCell(coordinate)
        } else if (firstCell.isState(SHOT_SUCCESS) && secondCell.isState(SHOT_SUCCESS)
            && thirdCell.isState(SHOT_SUCCESS) && (fourthCell.isState(EMPTY)
                    || fourthCell.isState(SHOT_FAILURE))
        ) {
            coordinate = shotFourthCell(coordinate)
        } else {
            coordinate = resetValuesAfterShipIsDead(
                FOUR_DECK_SHIP_SIZE, mutableListOf(
                    firstCell.getCoordinate(), secondCell.getCoordinate(),
                    thirdCell.getCoordinate(), fourthCell.getCoordinate()
                )
            )
        }
        return coordinate
    }

    private fun shotFourthCell(coordinate: Coordinate): Coordinate {
        var coordinateFourth = coordinate
        if (ships.contains(FOUR_DECK_SHIP_SIZE)) {
            coordinateFourth = checkNeighbourCells(firstCell, thirdCell)
        }
        if (coordinateFourth.x == -1) {
            coordinateFourth = resetValuesAfterShipIsDead(
                THREE_DECK_SHIP_SIZE, mutableListOf(
                    firstCell.getCoordinate(), secondCell.getCoordinate(), thirdCell.getCoordinate()
                )
            )
        } else {
            fourthCell = Cell(coordinateFourth.x, coordinateFourth.y)
        }
        return coordinateFourth
    }

    private fun shotThirdCell(coordinate: Coordinate): Coordinate {
        var coordinateThird = coordinate
        if (ships.contains(THREE_DECK_SHIP_SIZE) || ships.contains(FOUR_DECK_SHIP_SIZE)
        ) {
            coordinateThird = checkNeighbourCells(firstCell, secondCell)
        }
        if (coordinateThird.x == -1) {
            coordinateThird = resetValuesAfterShipIsDead(
                TWO_DECK_SHIP_SIZE,
                mutableListOf(firstCell.getCoordinate(), secondCell.getCoordinate())
            )
        } else {
            thirdCell = Cell(coordinateThird.x, coordinateThird.y)
        }
        return coordinateThird
    }

    private fun shotSecondCell(coordinate: Coordinate): Coordinate {
        var coordinateSecond = coordinate
        if (ships.contains(TWO_DECK_SHIP_SIZE)
            || ships.contains(THREE_DECK_SHIP_SIZE)
            || ships.contains(FOUR_DECK_SHIP_SIZE)
        ) {
            coordinateSecond = getNextCoordinateToShot(firstCell)
        }
        if (coordinateSecond.x == -1) {
            coordinateSecond = resetValuesAfterShipIsDead(
                ONE_DECK_SHIP_SIZE,
                mutableListOf(firstCell.getCoordinate())
            )
        } else {
            secondCell = Cell(coordinateSecond.x, coordinateSecond.y)
        }
        return coordinateSecond
    }

    fun resetValuesAfterShipIsDead(
        deadShip: Int,
        cells: MutableList<Coordinate>
    ): Coordinate {
        ships.remove(deadShip)
        markEdgeCells(cells)
        resetCells()
        return getRandomCoordinate()
    }

    fun markEdgeCells(cells: MutableList<Coordinate>) {
        when {
            cells.size == 1 -> {
                val coordinate = Cell(cells[0].x, cells[0].y)
                markHorizontalNeighbours(coordinate)
                markVerticalNeighbours(coordinate)
            }
            cells[0].x == cells[1].x -> {
                val max = getMaxCoordinate(cells, Orientation.HORIZONTAL)
                val min = getMinCoordinate(cells, Orientation.HORIZONTAL)
                battleField.setCellState(Coordinate(min.x, min.y - 1), SHOT_FAILURE)
                battleField.setCellState(Coordinate(max.x, max.y + 1), SHOT_FAILURE)
            }
            cells[0].y == cells[1].y -> {
                val max = getMaxCoordinate(cells, Orientation.VERTICAL)
                val min = getMinCoordinate(cells, Orientation.VERTICAL)
                battleField.setCellState(Coordinate(min.x - 1, min.y), SHOT_FAILURE)
                battleField.setCellState(Coordinate(max.x + 1, max.y), SHOT_FAILURE)
            }
        }
    }

    fun getMaxCoordinate(list: MutableList<Coordinate>, orientation: Orientation): Coordinate {
        return if (orientation == Orientation.VERTICAL) {
            list.maxBy { it.x }
        } else {
            list.maxBy { it.y }
        }
    }

    fun getMinCoordinate(list: MutableList<Coordinate>, orientation: Orientation): Coordinate {
        return if (orientation == Orientation.VERTICAL) {
            list.minBy { it.x }
        } else {
            list.minBy { it.y }
        }
    }

    fun checkNeighbourCells(cell1: Cell, cell2: Cell): Coordinate {
        var coordinate = Coordinate()
        if (cell1.getX() == cell2.getX()) {
            coordinate = checkVerticalCoordinates(cell2.getCoordinate(), cell1.getCoordinate())
        } else if (cell1.getY() == cell2.getY()) {
            coordinate = checkHorizontalCoordinates(cell2.getCoordinate(), cell1.getCoordinate())
        }
        return coordinate
    }

    fun checkHorizontalCoordinates(
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

    fun checkVerticalCoordinates(
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

    fun resetCells() {
        firstCell.setCellState(EMPTY)
        secondCell.setCellState(EMPTY)
        thirdCell.setCellState(EMPTY)
        fourthCell.setCellState(EMPTY)
    }

    fun getNextCoordinateToShot(cell: Cell): Coordinate {
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

    fun getRandomCoordinate(): Coordinate {
        firstCell = Cell()
        do {
            firstCell.setCoordinates(
                (0 until SQUARES_COUNT).random(),
                (0 until SQUARES_COUNT).random()
            )
        } while (!battleField.isCellFreeToBeSelected(firstCell.getCoordinate()))
        return firstCell.getCoordinate()
    }

    fun handleShot(shipState: Pair<Boolean, ArrayList<Coordinate>>) {
        val shipHit = shipState.first
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
        if (shipState.second.isNotEmpty()) {
            resetValuesAfterShipIsDead(shipState.second.size, shipState.second)
        }
    }

    private fun markNeighbours(cell: Cell) {
        if (firstCell.getX() == secondCell.getX()) {
            markVerticalNeighbours(cell)
        } else if (firstCell.getY() == secondCell.getY()) {
            markHorizontalNeighbours(cell)
        }
    }

    fun markHorizontalNeighbours(cell: Cell) {
        battleField.setCellState(Coordinate(cell.getX(), cell.getY() - 1), SHOT_FAILURE)
        battleField.setCellState(Coordinate(cell.getX(), cell.getY() + 1), SHOT_FAILURE)
        battleField.setCellState(Coordinate(cell.getX() - 1, cell.getY() - 1), SHOT_FAILURE)
        battleField.setCellState(Coordinate(cell.getX() - 1, cell.getY() + 1), SHOT_FAILURE)
        battleField.setCellState(Coordinate(cell.getX() + 1, cell.getY() - 1), SHOT_FAILURE)
        battleField.setCellState(Coordinate(cell.getX() + 1, cell.getY() + 1), SHOT_FAILURE)
    }

    fun markVerticalNeighbours(cell: Cell) {
        battleField.setCellState(Coordinate(cell.getX() - 1, cell.getY()), SHOT_FAILURE)
        battleField.setCellState(Coordinate(cell.getX() + 1, cell.getY()), SHOT_FAILURE)
        battleField.setCellState(Coordinate(cell.getX() - 1, cell.getY() - 1), SHOT_FAILURE)
        battleField.setCellState(Coordinate(cell.getX() + 1, cell.getY() - 1), SHOT_FAILURE)
        battleField.setCellState(Coordinate(cell.getX() - 1, cell.getY() + 1), SHOT_FAILURE)
        battleField.setCellState(Coordinate(cell.getX() + 1, cell.getY() + 1), SHOT_FAILURE)
    }

    fun updateBattleField(shipHit: Boolean, currentCell: Cell) {
        currentCell.setCellState(if (shipHit) SHOT_SUCCESS else SHOT_FAILURE)
        battleField.setCellState(currentCell.getCoordinate(), currentCell.getCellState())
    }

    fun isLeftCellAvailable(coordinate: Coordinate): Boolean {
        val leftCell = Cell(coordinate.x, coordinate.y - 1)
        return battleField.isCellFreeToBeSelected(leftCell.getCoordinate())
    }

    fun isRightCellAvailable(coordinate: Coordinate): Boolean {
        val rightCell = Cell(coordinate.x, coordinate.y + 1)
        return battleField.isCellFreeToBeSelected(rightCell.getCoordinate())
    }

    fun isTopCellAvailable(coordinate: Coordinate): Boolean {
        val topCell = Cell(coordinate.x - 1, coordinate.y)
        return battleField.isCellFreeToBeSelected(topCell.getCoordinate())
    }

    fun isBottomCellAvailable(coordinate: Coordinate): Boolean {
        val bottomCell = Cell(coordinate.x + 1, coordinate.y)
        return battleField.isCellFreeToBeSelected(bottomCell.getCoordinate())
    }
}