package com.avs.sea_battle.ships

import com.avs.sea_battle.FOUR_DECK_SHIP_SIZE

class FourDeckShip : Ship() {

    init {
        initCells()
    }

    override fun getLength(): Int {
        return FOUR_DECK_SHIP_SIZE
    }
}