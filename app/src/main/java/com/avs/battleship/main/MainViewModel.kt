package com.avs.battleship.main

import android.graphics.Point
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avs.battleship.R
import com.avs.battleship.battle_field.BattleField

class MainViewModel : ViewModel() {

    var status = MutableLiveData<Int>()
    var computerShips = MutableLiveData<ArrayList<Point>>()

    init {
        status.value = R.string.welcome_text
    }

    var battleField = BattleField()

    override fun onCleared() {
        super.onCleared()
    }

    fun handleUIEventById(id: Int) {
        when (id) {
            R.id.viewGenerate -> {
                generateShips()
            }
            R.id.viewStart -> {
                startGame()
            }
            R.id.viewFire -> {
                makeFire()
            }
        }
    }

    private fun generateShips() {
        battleField.initBattleShip()
        battleField.randomizeShips()
        computerShips.value = battleField.getShipsCoordinates()
    }

    fun handlePCAreaClick(point: Point) {

    }

    private fun makeFire() {

    }

    private fun startGame() {

    }
}