package com.avs.battleship.ships

import com.avs.battleship.FOUR_DECK_SHIP_SIZE

class FourDeckShip : Ship() {

    init {
        initCells()
    }

    override fun getLength(): Int {
        return FOUR_DECK_SHIP_SIZE
    }
}