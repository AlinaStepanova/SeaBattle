package com.avs.sea.battle.main

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avs.sea.battle.R
import com.avs.sea.battle.SECOND_IN_MILLIS
import com.avs.sea.battle.battle_field.BattleField
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.ui.GamePhase
import com.avs.sea.battle.ui.GameUiState
import com.avs.sea.battle.ui.UiEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private var activePlayer: Player = Player.NONE
    private lateinit var personBattleField: BattleField
    private lateinit var computerBattleField: BattleField
    private lateinit var shotManager: ShotManager

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    init {
        initValues()
    }

    private fun initValues() {
        activePlayer = Player.NONE
        shotManager = ShotManager()
        personBattleField = BattleField()
        computerBattleField = BattleField()
        computerBattleField.randomizeShips()
        _uiState.value = GameUiState()
    }

    fun generateShips() {
        personBattleField.initBattleShip()
        personBattleField.randomizeShips()
        _uiState.update {
            it.copy(
                personBoardShips = personBattleField.getShipsCoordinates(),
                statusResId = R.string.status_generate_or_start_text,
            )
        }
    }

    fun startGame() {
        activePlayer = Player.PERSON
        _uiState.update {
            it.copy(phase = GamePhase.BATTLE, statusResId = R.string.status_select_to_fire_text)
        }
    }

    fun startNewGame() {
        initValues()
    }

    fun handlePCAreaClick(coordinate: Coordinate) {
        if (activePlayer == Player.PERSON && computerBattleField.isCellFreeToBeSelected(coordinate)) {
            _uiState.update { it.copy(selectedCoordinate = coordinate) }
        }
    }

    fun makeFireAsPerson() {
        val target = _uiState.value.selectedCoordinate
        if (activePlayer != Player.PERSON || target == null) return
        val shipState = computerBattleField.handleShot(target)
        if (shipState.first) {
            _uiState.update {
                it.copy(
                    selectedCoordinate = null,
                    computerBoardCrosses = computerBattleField.getCrossesCoordinates(),
                    computerBoardDots = if (shipState.second.isNotEmpty()) {
                        computerBattleField.getDotsCoordinates()
                    } else {
                        it.computerBoardDots
                    },
                    statusResId = R.string.status_shot_ship_again_text,
                )
            }
            if (computerBattleField.isGameOver()) {
                endGame(isPersonWon = true)
            }
        } else {
            activePlayer = Player.COMPUTER
            _uiState.update {
                it.copy(
                    selectedCoordinate = null,
                    computerBoardDots = computerBattleField.getDotsCoordinates(),
                    statusResId = R.string.status_opponent_shot_text,
                    phase = GamePhase.COMPUTER_TURN,
                )
            }
            playAsComputer()
        }
    }

    private fun playAsComputer() {
        val coordinate = shotManager.getCoordinateToShot()
        val shipState = personBattleField.handleShot(coordinate)
        shotManager.handleShot(shipState)
        viewModelScope.launch {
            delay(SECOND_IN_MILLIS)
            if (shipState.first) {
                _uiState.update {
                    it.copy(
                        personBoardCrosses = personBattleField.getCrossesCoordinates(),
                        personBoardDots = if (shipState.second.isNotEmpty()) {
                            personBattleField.getDotsCoordinates()
                        } else {
                            it.personBoardDots
                        },
                    )
                }
                if (personBattleField.isGameOver()) {
                    endGame(isPersonWon = false)
                } else {
                    _uiState.update { it.copy(statusResId = R.string.status_opponent_shot_again_text) }
                    playAsComputer()
                }
            } else {
                activePlayer = Player.PERSON
                _uiState.update {
                    it.copy(
                        personBoardDots = personBattleField.getDotsCoordinates(),
                        phase = GamePhase.BATTLE,
                        statusResId = R.string.status_select_to_fire_text,
                    )
                }
            }
        }
    }

    private fun endGame(isPersonWon: Boolean) {
        activePlayer = Player.NONE
        _uiState.update {
            it.copy(
                phase = GamePhase.OVER,
                winner = if (isPersonWon) Player.PERSON else Player.COMPUTER,
                computerBoardShips = computerBattleField.getShipsCoordinates(),
                statusResId = if (isPersonWon) {
                    R.string.status_game_over_you_win_text
                } else {
                    R.string.status_game_over_you_lose_text
                },
            )
        }
        if (isPersonWon) {
            _events.tryEmit(UiEvent.RequestReview)
        }
    }

    @VisibleForTesting
    fun getComputerBattleField(): BattleField = computerBattleField
}
