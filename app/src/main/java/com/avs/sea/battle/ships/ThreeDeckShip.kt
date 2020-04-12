package com.avs.sea.battle.ships

import com.avs.sea.battle.THREE_DECK_SHIP_SIZE

class ThreeDeckShip : Ship() {

    init {
        initCells()
    }

    override fun getLength(): Int {
        return THREE_DECK_SHIP_SIZE
    }
}