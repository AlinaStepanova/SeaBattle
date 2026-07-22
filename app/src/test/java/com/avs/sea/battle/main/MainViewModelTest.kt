package com.avs.sea.battle.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.avs.sea.battle.R
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.ui.GamePhase
import com.avs.sea.battle.ui.UiEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MainViewModel
    private val events = mutableListOf<UiEvent>()
    private lateinit var eventsJob: Job

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = MainViewModel()
        events.clear()
        eventsJob = CoroutineScope(UnconfinedTestDispatcher()).launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.generateShips()
        viewModel.startGame()
    }

    @After
    fun tearDown() {
        eventsJob.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun startedGameIsInBattlePhaseWithShipsPlaced() {
        assertEquals(GamePhase.BATTLE, viewModel.uiState.value.phase)
        assertEquals(20, viewModel.uiState.value.personBoardShips.size)
        assertEquals(R.string.status_select_to_fire_text, viewModel.uiState.value.statusResId)
    }

    @Test
    fun makeFireAsPersonWithoutSelectionKeepsPersonTurn() {
        viewModel.makeFireAsPerson()
        assertEquals(GamePhase.BATTLE, viewModel.uiState.value.phase)
        assertEquals(R.string.status_select_to_fire_text, viewModel.uiState.value.statusResId)
    }

    @Test
    fun makeFireAsPersonAtEmptyCellPassesTurnToComputer() {
        viewModel.handlePCAreaClick(findEmptyCell())
        viewModel.makeFireAsPerson()
        assertEquals(GamePhase.COMPUTER_TURN, viewModel.uiState.value.phase)
        assertEquals(R.string.status_opponent_shot_text, viewModel.uiState.value.statusResId)
        assertNull(viewModel.uiState.value.selectedCoordinate)
    }

    @Test
    fun winningGameEndsGameWithAllComputerShipsSunk() {
        winGame()
        assertEquals(GamePhase.OVER, viewModel.uiState.value.phase)
        assertEquals(Player.PERSON, viewModel.uiState.value.winner)
        // every computer ship cell is hit, so the board shows 20 crosses and no remaining ships
        assertEquals(20, viewModel.uiState.value.computerBoardCrosses.size)
        assertEquals(emptyList<Coordinate>(), viewModel.uiState.value.computerBoardShips)
        assertEquals(R.string.status_game_over_you_win_text, viewModel.uiState.value.statusResId)
    }

    @Test
    fun winningGameEmitsSingleReviewRequest() {
        winGame()
        assertEquals(listOf<UiEvent>(UiEvent.RequestReview), events)
    }

    @Test
    fun startingNewGameResetsState() {
        winGame()
        viewModel.startNewGame()
        assertEquals(GamePhase.PLACING, viewModel.uiState.value.phase)
        assertEquals(emptyList<Coordinate>(), viewModel.uiState.value.personBoardShips)
        assertEquals(emptyList<Coordinate>(), viewModel.uiState.value.computerBoardCrosses)
        assertNull(viewModel.uiState.value.winner)
    }

    private fun findEmptyCell(): Coordinate {
        val ships = viewModel.getComputerBattleField().getShipsCoordinates()
        for (i in 0 until SQUARES_COUNT) {
            for (j in 0 until SQUARES_COUNT) {
                if (ships.none { it.x == i && it.y == j }) {
                    return Coordinate(i, j)
                }
            }
        }
        throw IllegalStateException("Battle field has no empty cells")
    }

    private fun winGame() {
        val computerField = viewModel.getComputerBattleField()
        while (!computerField.isGameOver()) {
            viewModel.handlePCAreaClick(computerField.getShipsCoordinates().first())
            viewModel.makeFireAsPerson()
        }
    }
}
