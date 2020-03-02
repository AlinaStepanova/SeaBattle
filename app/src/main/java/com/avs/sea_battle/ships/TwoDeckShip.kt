package com.avs.sea_battle.ships

import com.avs.sea_battle.TWO_DECK_SHIP_SIZE

class TwoDeckShip : Ship() {

    init {
        initCells()
    }

    override fun getLength(): Int {
        return TWO_DECK_SHIP_SIZE
    }
}