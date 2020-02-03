package com.avs.battleship.main

import android.graphics.Point
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avs.battleship.R
import com.avs.battleship.battle_field.BattleField

class MainViewModel : ViewModel() {

    private var activePlayer = Player.NONE
    private var _selectedPoint = MutableLiveData<Point>()
    val selectedPoint: LiveData<Point>
        get() = _selectedPoint
    private var _status = MutableLiveData<Int>()
    val status: LiveData<Int>
        get() = _status
    private var _personShips = MutableLiveData<ArrayList<Point>>()
    val personShips: LiveData<ArrayList<Point>>
        get() = _personShips
    private var _computerFailShots = MutableLiveData<ArrayList<Point>>()
    val computerFailedShots: LiveData<ArrayList<Point>>
        get() = _computerFailShots
    private var _computerSuccessfulShots = MutableLiveData<ArrayList<Point>>()
    val computerSuccessfulShots: LiveData<ArrayList<Point>>
        get() = _computerSuccessfulShots
    private var _startGameEvent = MutableLiveData<Boolean>()
    val startGameEvent: LiveData<Boolean>
        get() = _startGameEvent
    private var personBattleField = BattleField()
    private var computerBattleField = BattleField()

    init {
        _status.value = R.string.status_welcome_text
        _startGameEvent.value = false
        computerBattleField.randomizeShips()
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun generateShips() {
        personBattleField.initBattleShip()
        personBattleField.randomizeShips()
        _personShips.value = personBattleField.getShipsCoordinates()
        _status.value = R.string.status_generate_or_start_text
    }

    fun handlePCAreaClick(point: Point) {
        if (activePlayer == Player.PERSON) {
            if (computerBattleField.isCellFreeToBeSelected(point)) {
                _selectedPoint.value = point
            }
        }
    }

    fun makeFire() {
        if (activePlayer == Player.PERSON) {
            val isShipHit = computerBattleField.handleShot(_selectedPoint.value)
            _selectedPoint.value = null
            if (isShipHit) {
                _computerSuccessfulShots.value = computerBattleField.getCrossesCoordinates()
                startGame()
            } else {
                _computerFailShots.value = computerBattleField.getDotsCoordinates()
                activePlayer = Player.COMPUTER
            }
        } else {

        }
    }

    fun startGame() {
        activePlayer = Player.PERSON
        _startGameEvent.value = true
        _status.value = R.string.status_select_to_fire_text
    }
}