package com.avs.battleship.ships

import com.avs.battleship.TWO_DECK_SHIP_SIZE

class TwoDeckShip : Ship() {

    init {
        initCells()
    }

    override fun getLength(): Int {
        return TWO_DECK_SHIP_SIZE
    }
}