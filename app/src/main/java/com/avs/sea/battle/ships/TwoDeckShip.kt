package com.avs.sea.battle.ships

import com.avs.sea.battle.TWO_DECK_SHIP_SIZE

class TwoDeckShip : Ship() {

    init {
        initCells()
    }

    override fun getLength(): Int {
        return TWO_DECK_SHIP_SIZE
    }
}