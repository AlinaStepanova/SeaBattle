package com.avs.battleship.ships

import com.avs.battleship.THREE_DECK_SHIP_SIZE

class ThreeDeckShip : Ship() {

    init {
        initCells()
    }

    override fun getLength(): Int {
        return THREE_DECK_SHIP_SIZE
    }
}