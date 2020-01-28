package com.avs.battleship.ships

import com.avs.battleship.ONE_DECK_SHIP_SIZE

class OneDeckShip : Ship() {

    init {
        initCells()
    }

    override fun getLength(): Int {
        return ONE_DECK_SHIP_SIZE
    }
}