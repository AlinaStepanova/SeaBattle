package com.avs.sea.battle.main

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avs.sea.battle.R
import com.avs.sea.battle.REJECTED_FLASH_MILLIS
import com.avs.sea.battle.SECOND_IN_MILLIS
import com.avs.sea.battle.battle_field.BattleField
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.battle_field.FleetLayout
import com.avs.sea.battle.battle_field.PlacementResult
import com.avs.sea.battle.ui.GamePhase
import com.avs.sea.battle.ui.GameUiState
import com.avs.sea.battle.ui.UiEvent
import kotlinx.coroutines.Job
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

    private var fleetLayout = FleetLayout.initial()
    private var selectedShipIndex = 0
    private var computerTurnJob: Job? = null
    private var rejectedFlashJob: Job? = null

    init {
        initValues()
    }

    private fun initValues() {
        computerTurnJob?.cancel()
        rejectedFlashJob?.cancel()
        activePlayer = Player.NONE
        shotManager = ShotManager()
        personBattleField = BattleField()
        computerBattleField = BattleField()
        computerBattleField.randomizeShips()
        _uiState.value = GameUiState()
        showLayout(FleetLayout.initial())
    }

    fun generateShips() {
        showLayout(FleetLayout.random())
    }

    // Tapping the selected ship rotates it, another ship selects it, and an empty cell moves the
    // selected ship there.
    fun onPersonBoardTap(coordinate: Coordinate) {
        if (_uiState.value.phase != GamePhase.PLACING) return
        val tappedShip = fleetLayout.shipIndexAt(coordinate)
        when (tappedShip) {
            selectedShipIndex -> applyPlacement(fleetLayout.rotate(selectedShipIndex))
            null -> applyPlacement(fleetLayout.move(selectedShipIndex, coordinate))
            else -> {
                selectedShipIndex = tappedShip
                publishLayout()
            }
        }
    }

    private fun showLayout(layout: FleetLayout) {
        fleetLayout = layout
        selectedShipIndex = 0
        publishLayout()
    }

    private fun applyPlacement(result: PlacementResult) {
        when (result) {
            is PlacementResult.Placed -> {
                fleetLayout = result.layout
                publishLayout()
            }
            is PlacementResult.Rejected -> flashRejected(result.cells)
        }
    }

    private fun publishLayout() {
        _uiState.update {
            it.copy(
                personBoardShips = fleetLayout.cells(),
                selectedShipCells = fleetLayout.ships[selectedShipIndex].cells(),
            )
        }
    }

    private fun flashRejected(cells: List<Coordinate>) {
        rejectedFlashJob?.cancel()
        _uiState.update { it.copy(rejectedCells = cells) }
        rejectedFlashJob = viewModelScope.launch {
            delay(REJECTED_FLASH_MILLIS)
            _uiState.update { it.copy(rejectedCells = emptyList()) }
        }
    }

    fun startGame() {
        rejectedFlashJob?.cancel()
        personBattleField.placeShips(fleetLayout)
        activePlayer = Player.PERSON
        _uiState.update {
            it.copy(
                phase = GamePhase.BATTLE,
                statusResId = R.string.status_select_to_fire_text,
                personBoardShips = personBattleField.getShipsCoordinates(),
                selectedShipCells = emptyList(),
                rejectedCells = emptyList(),
            )
        }
    }

    // Restarting mid-battle throws away progress, so it is confirmed first.
    fun onNewGameRequested() {
        val phase = _uiState.value.phase
        if (phase == GamePhase.BATTLE || phase == GamePhase.COMPUTER_TURN) {
            _uiState.update { it.copy(showNewGameConfirmation = true) }
        } else {
            startNewGame()
        }
    }

    fun confirmNewGame() {
        startNewGame()
    }

    fun dismissNewGame() {
        _uiState.update { it.copy(showNewGameConfirmation = false) }
    }

    fun startNewGame() {
        // Ask for a review only once the player has seen their win and chosen to play again,
        // so the dialog doesn't cover the result screen.
        if (_uiState.value.winner == Player.PERSON) {
            _events.tryEmit(UiEvent.RequestReview)
        }
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
        computerTurnJob = viewModelScope.launch {
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
    }

    @VisibleForTesting
    fun getComputerBattleField(): BattleField = computerBattleField
}
