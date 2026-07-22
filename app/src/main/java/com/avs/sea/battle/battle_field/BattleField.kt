package com.avs.sea.battle.battle_field

import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.ships.*

class BattleField : BaseBattleField() {

    private lateinit var ships: List<Ship>

    fun randomizeShips() {
        ships = listOf(
            FourDeckShip(), ThreeDeckShip(), ThreeDeckShip(),
            TwoDeckShip(), TwoDeckShip(), TwoDeckShip(),
            OneDeckShip(), OneDeckShip(), OneDeckShip(), OneDeckShip()
        )
        var isAdded: Boolean
        for (ship in ships) {
            isAdded = false
            while (!isAdded) {
                val coordinate = getRandomCoordinate(ship)
                if (isPlacementAreaEmpty(coordinate, ship)) {
                    ship.setCellsCoordinates(coordinate.x, coordinate.y)
                    for (cell in ship.getShipCells()) {
                        battleField[cell.getX()][cell.getY()]?.setCellState(cell.getCellState())
                    }
                    isAdded = true
                }
            }
        }
    }

    fun handleShot(coordinate: Coordinate?): Pair<Boolean, ArrayList<Coordinate>> {
        var isShipHit = false
        var killedShipCoordinates: ArrayList<Coordinate> = ArrayList()
        if (coordinate != null && coordinate.x in 0 until SQUARES_COUNT && coordinate.y in 0 until SQUARES_COUNT) {
            if (battleField[coordinate.x][coordinate.y]?.getCellState() == CellState.EMPTY) {
                battleField[coordinate.x][coordinate.y]?.setCellState(CellState.SHOT_FAILURE)
            } else {
                battleField[coordinate.x][coordinate.y]?.setCellState(CellState.SHOT_SUCCESS)
                isShipHit = true
                val ship = getShipByCoordinate(coordinate)
                ship?.let {
                    it.setShotSuccessState(coordinate)
                    if (it.isDead()) {
                        markNeighbours(ship)
                        killedShipCoordinates = getShipCoordinates(ship)
                    }
                }
            }
        }
        return isShipHit to killedShipCoordinates
    }

    private fun markNeighbours(ship: Ship) {
        if (ship.getShipOrientation() == Orientation.VERTICAL) {
            markVerticalNeighbours(ship)
        } else {
            markHorizontalNeighbours(ship)
        }
    }

    private fun markHorizontalNeighbours(ship: Ship) {
        for (cell in ship.getShipCells()) {
            if (cell.getX() != 0) battleField[cell.getX() - 1][cell.getY()]?.setCellState(CellState.SHOT_FAILURE)
            if (cell.getX() != battleField.size - 1) battleField[cell.getX() + 1][cell.getY()]?.setCellState(
                CellState.SHOT_FAILURE
            )
        }
        val fistCell = ship.getShipCells().first()
        if (fistCell.getY() != 0) {
            battleField[fistCell.getX()][fistCell.getY() - 1]?.setCellState(CellState.SHOT_FAILURE)
            if (fistCell.getX() != 0) {
                battleField[fistCell.getX() - 1][fistCell.getY() - 1]?.setCellState(CellState.SHOT_FAILURE)
            }
            if (fistCell.getX() != battleField.size - 1) {
                battleField[fistCell.getX() + 1][fistCell.getY() - 1]?.setCellState(CellState.SHOT_FAILURE)
            }
        }
        val lastCell = ship.getShipCells().last()
        if (lastCell.getY() != battleField.size - 1) {
            battleField[lastCell.getX()][lastCell.getY() + 1]?.setCellState(CellState.SHOT_FAILURE)
            if (lastCell.getX() != 0) {
                battleField[lastCell.getX() - 1][lastCell.getY() + 1]?.setCellState(CellState.SHOT_FAILURE)
            }
            if (lastCell.getX() != battleField.size - 1) {
                battleField[lastCell.getX() + 1][lastCell.getY() + 1]?.setCellState(CellState.SHOT_FAILURE)
            }
        }
    }

    private fun markVerticalNeighbours(ship: Ship) {
        for (cell in ship.getShipCells()) {
            if (cell.getY() != 0) battleField[cell.getX()][cell.getY() - 1]?.setCellState(CellState.SHOT_FAILURE)
            if (cell.getY() != battleField.size - 1) battleField[cell.getX()][cell.getY() + 1]?.setCellState(
                CellState.SHOT_FAILURE
            )
        }
        val fistCell = ship.getShipCells().first()
        if (fistCell.getX() != 0) {
            battleField[fistCell.getX() - 1][fistCell.getY()]?.setCellState(CellState.SHOT_FAILURE)
            if (fistCell.getY() != 0) {
                battleField[fistCell.getX() - 1][fistCell.getY() - 1]?.setCellState(CellState.SHOT_FAILURE)
            }
            if (fistCell.getY() != battleField.size - 1) {
                battleField[fistCell.getX() - 1][fistCell.getY() + 1]?.setCellState(CellState.SHOT_FAILURE)
            }
        }
        val lastCell = ship.getShipCells().last()
        if (lastCell.getX() != battleField.size - 1) {
            battleField[lastCell.getX() + 1][lastCell.getY()]?.setCellState(CellState.SHOT_FAILURE)
            if (lastCell.getY() != 0) {
                battleField[lastCell.getX() + 1][lastCell.getY() - 1]?.setCellState(CellState.SHOT_FAILURE)
            }
            if (lastCell.getY() != battleField.size - 1) {
                battleField[lastCell.getX() + 1][lastCell.getY() + 1]?.setCellState(CellState.SHOT_FAILURE)
            }
        }
    }

    private fun getShipByCoordinate(coordinate: Coordinate): Ship? {
        for (ship in ships) {
            if (coordinate.x in ship.getRowCoordinates() && coordinate.y in ship.getColumnCoordinates()) {
                return ship
            }
        }
        return null
    }

    fun isGameOver(): Boolean {
        var result = true
        for (ship in ships) {
            if (!ship.isDead()) {
                result = false
                break
            }
        }
        return result
    }

    fun getShipsCoordinates(): ArrayList<Coordinate> {
        val shipsCoordinates = arrayListOf<Coordinate>()
        for (i in battleField.indices) {
            for (j in battleField[i].indices) {
                if (battleField[i][j]?.getCellState() == CellState.SHIP) {
                    shipsCoordinates.add(Coordinate(i, j))
                }
            }
        }
        return shipsCoordinates
    }

    private fun getShipCoordinates(ship: Ship): ArrayList<Coordinate> {
        val shipsCoordinates = arrayListOf<Coordinate>()
        ship.getShipCells().forEach { cell ->
            shipsCoordinates.add(cell.getCoordinate())
        }
        return shipsCoordinates
    }

    fun getDotsCoordinates(): ArrayList<Coordinate> {
        val dotsCoordinates = arrayListOf<Coordinate>()
        for (i in battleField.indices) {
            for (j in battleField[i].indices) {
                if (battleField[i][j]?.getCellState() == CellState.SHOT_FAILURE) {
                    dotsCoordinates.add(Coordinate(i, j))
                }
            }
        }
        return dotsCoordinates
    }

    fun getCrossesCoordinates(): ArrayList<Coordinate> {
        val crossesCoordinates = arrayListOf<Coordinate>()
        for (i in battleField.indices) {
            for (j in battleField[i].indices) {
                if (battleField[i][j]?.getCellState() == CellState.SHOT_SUCCESS) {
                    crossesCoordinates.add(Coordinate(i, j))
                }
            }
        }
        return crossesCoordinates
    }

    // A ship can be placed only if every cell it will occupy, plus the one-cell
    // border around them, is empty - ships must not touch, even diagonally.
    private fun isPlacementAreaEmpty(coordinate: Coordinate, ship: Ship): Boolean {
        val isVertical = ship.getShipOrientation() == Orientation.VERTICAL
        val lastX = if (isVertical) coordinate.x + ship.getLength() - 1 else coordinate.x
        val lastY = if (isVertical) coordinate.y else coordinate.y + ship.getLength() - 1
        for (i in (coordinate.x - 1).coerceAtLeast(0)..(lastX + 1).coerceAtMost(SQUARES_COUNT - 1)) {
            for (j in (coordinate.y - 1).coerceAtLeast(0)..(lastY + 1).coerceAtMost(SQUARES_COUNT - 1)) {
                if (battleField[i][j]?.getCellState() != CellState.EMPTY) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRandomCoordinate(ship: Ship): Coordinate {
        val coordinate = Coordinate()
        if (ship.getShipOrientation() == Orientation.HORIZONTAL) {
            coordinate.x = (0 until SQUARES_COUNT).random()
            coordinate.y = (0..SQUARES_COUNT - ship.getLength()).random()
        } else {
            coordinate.x = (0..SQUARES_COUNT - ship.getLength()).random()
            coordinate.y = (0 until SQUARES_COUNT).random()
        }
        return coordinate
    }
}
