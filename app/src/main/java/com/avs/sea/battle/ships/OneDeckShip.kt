package com.avs.sea.battle.ships

import com.avs.sea.battle.ONE_DECK_SHIP_SIZE

class OneDeckShip : Ship() {

    init {
        initCells()
    }

    override fun getLength(): Int {
        return ONE_DECK_SHIP_SIZE
    }
}