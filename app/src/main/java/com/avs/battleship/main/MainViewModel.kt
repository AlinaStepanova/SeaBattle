package com.avs.battleship.main

import android.graphics.Point
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avs.battleship.R
import com.avs.battleship.battle_field.BattleField

class MainViewModel : ViewModel() {

    private var _status = MutableLiveData<Int>()
    val status: LiveData<Int>
        get() = _status
    private var _personShips = MutableLiveData<ArrayList<Point>>()
    val personShips: LiveData<ArrayList<Point>>
        get() = _personShips
    private var personBattleField = BattleField()
    private var computerBattleField = BattleField()

    init {
        _status.value = R.string.welcome_text
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
        _personShips.value = personBattleField.getShipsCoordinates()
    }

    fun handlePCAreaClick(point: Point) {

    }

    private fun makeFire() {

    }

    private fun startGame() {

    }
}