package com.avs.battleship.main

import android.graphics.Point
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avs.battleship.R
import com.avs.battleship.SQUARES_COUNT
import com.avs.battleship.battle_field.BattleField
import kotlinx.coroutines.*
import java.util.logging.Handler

class MainViewModel : ViewModel() {

    private var activePlayer = Player.NONE
    private var _selectedByPersonPoint = MutableLiveData<Point>()
    val selectedByPersonPoint: LiveData<Point>
        get() = _selectedByPersonPoint
    private var _selectedByComputerPoint = MutableLiveData<Point>()
    val selectedByComputerPoint: LiveData<Point>
        get() = _selectedByComputerPoint
    private var _status = MutableLiveData<Int>()
    val status: LiveData<Int>
        get() = _status
    private var _personShips = MutableLiveData<ArrayList<Point>>()
    val personShips: LiveData<ArrayList<Point>>
        get() = _personShips
    private var _personFailShots = MutableLiveData<ArrayList<Point>>()
    val personFailedShots: LiveData<ArrayList<Point>>
        get() = _personFailShots
    private var _personSuccessfulShots = MutableLiveData<ArrayList<Point>>()
    val personSuccessfulShots: LiveData<ArrayList<Point>>
        get() = _personSuccessfulShots
    private var _computerFailShots = MutableLiveData<ArrayList<Point>>()
    val computerFailedShots: LiveData<ArrayList<Point>>
        get() = _computerFailShots
    private var _computerSuccessfulShots = MutableLiveData<ArrayList<Point>>()
    val computerSuccessfulShots: LiveData<ArrayList<Point>>
        get() = _computerSuccessfulShots
    private var _startGameEvent = MutableLiveData<Boolean>()
    val startGameEvent: LiveData<Boolean>
        get() = _startGameEvent
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var personBattleField = BattleField()
    private var computerBattleField = BattleField()

    init {
        _status.value = R.string.status_welcome_text
        _startGameEvent.value = false
        computerBattleField.randomizeShips()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
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
                _selectedByPersonPoint.value = point
            }
        }
    }

    fun makeFireAsPerson() {
        if (activePlayer == Player.PERSON) {
            val isShipHit = computerBattleField.handleShot(_selectedByPersonPoint.value)
            _selectedByPersonPoint.value = null
            if (isShipHit) {
                _personSuccessfulShots.value = computerBattleField.getCrossesCoordinates()
                startGame()
            } else {
                _personFailShots.value = computerBattleField.getDotsCoordinates()
                activePlayer = Player.COMPUTER
                makeFireAsComputer()
            }
        }
    }

    private fun makeFireAsComputer() {
        while (activePlayer == Player.COMPUTER) {
            val point: Point = generatePointAsComputer()
            uiScope.launch {
                delay(2000)
                _selectedByComputerPoint.value = point
            }
            val isShipHit = personBattleField.handleShot(point)
            if (isShipHit) {
                _computerSuccessfulShots.value = personBattleField.getCrossesCoordinates()
            } else {
                _computerFailShots.value = personBattleField.getDotsCoordinates()
                activePlayer = Player.PERSON
                startGame()
            }
        }
    }

    private fun generatePointAsComputer(): Point {
        var point: Point
        do {
            point = Point((0 until SQUARES_COUNT).random(), (0 until SQUARES_COUNT).random())
        } while (!personBattleField.isCellFreeToBeSelected(point))
        return point
    }

    fun startGame() {
        activePlayer = Player.PERSON
        _startGameEvent.value = true
        _status.value = R.string.status_select_to_fire_text
    }
}