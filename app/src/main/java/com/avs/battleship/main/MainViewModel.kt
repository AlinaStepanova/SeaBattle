package com.avs.battleship.main

import android.graphics.Point
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avs.battleship.R
import com.avs.battleship.battle_field.BattleField

class MainViewModel : ViewModel() {

    var status = MutableLiveData<Int>()
    var personShips = MutableLiveData<ArrayList<Point>>()
    private var personBattleField = BattleField()
    private var computerBattleField: BattleField

    init {
        status.value = R.string.welcome_text
        computerBattleField = BattleField()
        computerBattleField.randomizeShips()
    }

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
        personBattleField.initBattleShip()
        personBattleField.randomizeShips()
        personShips.value = personBattleField.getShipsCoordinates()
    }

    fun handlePCAreaClick(point: Point) {

    }

    private fun makeFire() {

    }

    private fun startGame() {

    }
}