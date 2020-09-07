package com.avs.sea.battle.battle_field

import android.util.Log
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
                if (battleField[coordinate.x][coordinate.y]?.getCellState() == CellState.EMPTY) {
                    val length = ship.getLength()
                    isAdded = if (ship.getShipOrientation() == Orientation.VERTICAL) {
                        (isUpCellEmpty(coordinate)
                                && isUpCornerCellEmpty(coordinate)
                                && isBottomCornerCellEmpty(coordinate, length)
                                && isBottomCellEmpty(coordinate, length)
                                && isVerticalCellsEmpty(coordinate, length)
                                && isLeftSideEmpty(coordinate, length)
                                && isRightSideEmpty(coordinate, length))
                    } else {
                        (isStartCellEmpty(coordinate) && isEndCellEmpty(coordinate, length)
                                && isStartCornerCellEmpty(coordinate)
                                && isEndCornerCellEmpty(coordinate, length)
                                && isHorizontalCellsEmpty(coordinate, length)
                                && isTopSideEmpty(coordinate, length)
                                && isBottomSideEmpty(coordinate, length))
                    }
                    if (isAdded) {
                        ship.setCellsCoordinates(coordinate.x, coordinate.y)
                        for (cell in ship.getShipCells()) {
                            battleField[cell.getX()][cell.getY()]?.setCellState(cell.getCellState())
                        }
                    }
                }
            }
        }
        printBattleField()
    }

    fun handleShot(coordinate: Coordinate?): Boolean {
        var isShipHit = false
        if (coordinate != null && coordinate.x in 0 until SQUARES_COUNT && coordinate.y in 0 until SQUARES_COUNT) {
            if (battleField[coordinate.x][coordinate.y]?.getCellState() == CellState.EMPTY) {
                battleField[coordinate.x][coordinate.y]?.setCellState(CellState.SHOT_FAILURE)
            } else {
                battleField[coordinate.x][coordinate.y]?.setCellState(CellState.SHOT_SUCCESS)
                defineShipByCoordinate(coordinate)
                isShipHit = true
            }
        }
        return isShipHit
    }

    private fun defineShipByCoordinate(coordinate: Coordinate) {
        for (ship in ships) {
            if (coordinate.x in ship.getRowCoordinates() && coordinate.y in ship.getColumnCoordinates()) {
                ship.setShotSuccessState(coordinate)
                break
            }
        }
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

    private fun isStartCellEmpty(coordinate: Coordinate): Boolean {
        return ((coordinate.y == 0) || (coordinate.y > 0 &&
                battleField[coordinate.x][coordinate.y - 1]?.getCellState() == CellState.EMPTY))
    }

    private fun isEndCellEmpty(
        coordinate: Coordinate,
        length: Int
    ): Boolean {
        return ((coordinate.y + length == SQUARES_COUNT - 1) || (coordinate.y < SQUARES_COUNT - 1 &&
                battleField[coordinate.x][coordinate.y + length]?.getCellState() == CellState.EMPTY))
    }

    private fun isEndCornerCellEmpty(coordinate: Coordinate, length: Int): Boolean {
        return (coordinate.y + length == SQUARES_COUNT - 1)
                || (((coordinate.x == 0) || (coordinate.y < SQUARES_COUNT - 1 && coordinate.x > 0 &&
                battleField[coordinate.x - 1][coordinate.y + length]?.getCellState() == CellState.EMPTY))
                && ((coordinate.x == SQUARES_COUNT - 1)
                || (coordinate.y < SQUARES_COUNT - 1 && coordinate.x < SQUARES_COUNT - 1 &&
                battleField[coordinate.x + 1][coordinate.y + length]?.getCellState() == CellState.EMPTY)))
    }

    private fun isStartCornerCellEmpty(coordinate: Coordinate): Boolean {
        return (coordinate.y == 0) || (((coordinate.x == 0) || (coordinate.y > 0 && coordinate.x > 0 &&
                battleField[coordinate.x - 1][coordinate.y - 1]?.getCellState() == CellState.EMPTY))
                && ((coordinate.x == SQUARES_COUNT - 1) || (coordinate.y > 0 && coordinate.x < SQUARES_COUNT - 1 &&
                battleField[coordinate.x + 1][coordinate.y - 1]?.getCellState() == CellState.EMPTY)))
    }

    private fun isUpCellEmpty(coordinate: Coordinate): Boolean {
        return ((coordinate.x == 0) || (coordinate.x > 0 &&
                battleField[coordinate.x - 1][coordinate.y]?.getCellState() == CellState.EMPTY))
    }

    private fun isBottomCellEmpty(
        coordinate: Coordinate,
        length: Int
    ): Boolean {
        return ((coordinate.x + length == SQUARES_COUNT - 1) || (coordinate.x < SQUARES_COUNT - 1 &&
                battleField[coordinate.x + length][coordinate.y]?.getCellState() == CellState.EMPTY))
    }

    private fun isUpCornerCellEmpty(coordinate: Coordinate): Boolean {
        return ((coordinate.x == 0) ||
                (((coordinate.y == 0) || (coordinate.x > 0 && coordinate.y > 0 &&
                        battleField[coordinate.x - 1][coordinate.y - 1]?.getCellState() == CellState.EMPTY))
                        && ((coordinate.y == SQUARES_COUNT - 1) || (coordinate.x > 0 && coordinate.y < SQUARES_COUNT - 1 &&
                        battleField[coordinate.x - 1][coordinate.y + 1]?.getCellState() == CellState.EMPTY))))
    }

    private fun isBottomCornerCellEmpty(coordinate: Coordinate, length: Int): Boolean {
        return (coordinate.x + length == SQUARES_COUNT) ||
                (((coordinate.y == 0) || (coordinate.x + length < SQUARES_COUNT - 1 && coordinate.y > 0 &&
                        battleField[coordinate.x + length][coordinate.y - 1]?.getCellState() == CellState.EMPTY))
                        && ((coordinate.y == SQUARES_COUNT - 1) ||
                        (coordinate.x + length < SQUARES_COUNT - 1 && coordinate.y < SQUARES_COUNT - 1 &&
                                battleField[coordinate.x + length][coordinate.y + 1]?.getCellState() == CellState.EMPTY)))
    }

    private fun isLeftSideEmpty(coordinate: Coordinate, length: Int): Boolean {
        var isEmpty = true
        if (coordinate.y > 0) {
            for (i in 0 until length) {
                if (battleField[coordinate.x + i][coordinate.y - 1]?.getCellState() != CellState.EMPTY) {
                    isEmpty = false
                    break
                }
            }
        }
        return isEmpty
    }

    private fun isRightSideEmpty(coordinate: Coordinate, length: Int): Boolean {
        var isEmpty = true
        if (coordinate.y < SQUARES_COUNT - 1) {
            for (i in 0 until length) {
                if (battleField[coordinate.x + i][coordinate.y + 1]?.getCellState() != CellState.EMPTY) {
                    isEmpty = false
                    break
                }
            }
        }
        return isEmpty
    }

    private fun isVerticalCellsEmpty(coordinate: Coordinate, length: Int): Boolean {
        var isEmpty = true
        for (i in 0 until length) {
            if (battleField[coordinate.x + i][coordinate.y]?.getCellState() != CellState.EMPTY) {
                isEmpty = false
                break
            }
        }
        return isEmpty
    }

    private fun isHorizontalCellsEmpty(coordinate: Coordinate, length: Int): Boolean {
        var isEmpty = true
        for (i in 0 until length) {
            if (battleField[coordinate.x][coordinate.y + i]?.getCellState() != CellState.EMPTY) {
                isEmpty = false
                break
            }
        }
        return isEmpty
    }

    private fun isTopSideEmpty(coordinate: Coordinate, length: Int): Boolean {
        var isEmpty = true
        if (coordinate.x > 0) {
            for (i in 0 until length) {
                if (battleField[coordinate.x - 1][coordinate.y + i]?.getCellState() != CellState.EMPTY) {
                    isEmpty = false
                    break
                }
            }
        }
        return isEmpty
    }

    private fun isBottomSideEmpty(coordinate: Coordinate, length: Int): Boolean {
        var isEmpty = true
        if (coordinate.x < SQUARES_COUNT - 1) {
            for (i in 0 until length) {
                if (battleField[coordinate.x + 1][coordinate.y + i]?.getCellState() != CellState.EMPTY) {
                    isEmpty = false
                    break
                }
            }
        }
        return isEmpty
    }

    private fun getRandomCoordinate(ship: Ship): Coordinate {
        val coordinate = Coordinate()
        if (ship.getShipOrientation() == Orientation.HORIZONTAL) {
            coordinate.x = (0 until SQUARES_COUNT).random()
            coordinate.y = (0 until SQUARES_COUNT - ship.getLength() - 1).random()
        } else {
            coordinate.x = (0 until SQUARES_COUNT - ship.getLength() - 1).random()
            coordinate.y = (0 until SQUARES_COUNT).random()
        }
        return coordinate
    }
}