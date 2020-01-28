package com.avs.battleship.ships

import com.avs.battleship.MAX_SHIP_SIZE

class FourDeckShip : Ship() {

    init {
        initCells()
    }

    override fun getLength(): Int {
        return MAX_SHIP_SIZE
    }
}