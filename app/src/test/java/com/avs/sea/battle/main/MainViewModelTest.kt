package com.avs.sea.battle.main

import com.avs.sea.battle.R
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.battle_field.FleetLayout
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: MainViewModel
    private val events = mutableListOf<UiEvent>()
    private lateinit var eventsJob: Job

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MainViewModel()
        events.clear()
        eventsJob = CoroutineScope(testDispatcher).launch {
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
        assertNull(viewModel.uiState.value.statusResId)
    }

    // The turn arrows replace the battle status text; only placement and game over show text.
    @Test
    fun battleShowsNoStatusText() {
        viewModel.onComputerBoardTap(viewModel.getComputerBattleField().getShipsCoordinates().first())
        assertNull(viewModel.uiState.value.statusResId)
        viewModel.onComputerBoardTap(findFreeEmptyCell())
        assertNull(viewModel.uiState.value.statusResId)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.statusResId)
    }

    @Test
    fun tappingAFreeEmptyCellFiresAndPassesTheTurn() {
        val miss = findFreeEmptyCell()
        viewModel.onComputerBoardTap(miss)
        assertEquals(GamePhase.COMPUTER_TURN, viewModel.uiState.value.phase)
        assertTrue(miss in viewModel.uiState.value.computerBoardDots)
    }

    @Test
    fun tappingAShipCellHitsAndKeepsTheTurn() {
        val shipCell = viewModel.getComputerBattleField().getShipsCoordinates().first()
        viewModel.onComputerBoardTap(shipCell)
        assertEquals(GamePhase.BATTLE, viewModel.uiState.value.phase)
        assertEquals(listOf(shipCell), viewModel.uiState.value.computerBoardCrosses)
    }

    @Test
    fun tappingAnAlreadyHitCellIsIgnored() {
        val shipCell = viewModel.getComputerBattleField().getShipsCoordinates().first()
        viewModel.onComputerBoardTap(shipCell)
        val afterFirstTap = viewModel.uiState.value
        viewModel.onComputerBoardTap(shipCell)
        assertEquals(afterFirstTap, viewModel.uiState.value)
    }

    // The dots around a sunk ship are cells already shot at, so tapping one must not count as a miss.
    @Test
    fun tappingAMarkedCellAroundASunkShipIsIgnored() {
        val computerField = viewModel.getComputerBattleField()
        while (viewModel.uiState.value.computerBoardDots.isEmpty()) {
            viewModel.onComputerBoardTap(computerField.getShipsCoordinates().first())
        }
        val afterSinking = viewModel.uiState.value
        viewModel.onComputerBoardTap(afterSinking.computerBoardDots.first())
        assertEquals(afterSinking, viewModel.uiState.value)
        assertEquals(GamePhase.BATTLE, viewModel.uiState.value.phase)
    }

    @Test
    fun aDoubleTapOnAMissFiresOnce() {
        val miss = findFreeEmptyCell()
        viewModel.onComputerBoardTap(miss)
        val dotsAfterFirstTap = viewModel.uiState.value.computerBoardDots
        viewModel.onComputerBoardTap(miss)
        viewModel.onComputerBoardTap(findFreeEmptyCell())
        assertEquals(dotsAfterFirstTap, viewModel.uiState.value.computerBoardDots)
        assertEquals(GamePhase.COMPUTER_TURN, viewModel.uiState.value.phase)
    }

    @Test
    fun tapsDuringTheComputersTurnAreIgnored() {
        viewModel.onComputerBoardTap(findFreeEmptyCell())
        viewModel.onComputerBoardTap(viewModel.getComputerBattleField().getShipsCoordinates().first())
        assertEquals(emptyList<Coordinate>(), viewModel.uiState.value.computerBoardCrosses)
    }

    @Test
    fun tapsAfterGameOverAreIgnored() {
        winGame()
        val atGameOver = viewModel.uiState.value
        for (x in 0 until SQUARES_COUNT) {
            for (y in 0 until SQUARES_COUNT) {
                viewModel.onComputerBoardTap(Coordinate(x, y))
            }
        }
        assertEquals(atGameOver, viewModel.uiState.value)
    }

    @Test
    fun computerTurnEndsWithMissAndReturnsTurnToPerson() {
        viewModel.onComputerBoardTap(findFreeEmptyCell())
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(GamePhase.BATTLE, viewModel.uiState.value.phase)
        assertNull(viewModel.uiState.value.statusResId)
        assertTrue(viewModel.uiState.value.personBoardDots.isNotEmpty())
    }

    @Test
    fun losingGameRevealsComputerShipsWithoutReviewRequest() {
        loseGame()
        assertEquals(GamePhase.OVER, viewModel.uiState.value.phase)
        assertEquals(Player.COMPUTER, viewModel.uiState.value.winner)
        assertEquals(20, viewModel.uiState.value.personBoardCrosses.size)
        assertEquals(20, viewModel.uiState.value.computerBoardShips.size)
        assertEquals(R.string.status_game_over_you_lose_text, viewModel.uiState.value.statusResId)
        assertEquals(emptyList<UiEvent>(), events)
    }

    @Test
    fun newGameDialogClosesWhenTheComputerWinsBehindIt() {
        while (viewModel.uiState.value.phase != GamePhase.OVER) {
            viewModel.onComputerBoardTap(findFreeEmptyCell())
            viewModel.onNewGameRequested()
            assertTrue(viewModel.uiState.value.showNewGameConfirmation)
            testDispatcher.scheduler.advanceUntilIdle()
        }
        assertEquals(Player.COMPUTER, viewModel.uiState.value.winner)
        assertFalse(viewModel.uiState.value.showNewGameConfirmation)
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
    fun winningGameDoesNotRequestReviewWhileWinIsOnScreen() {
        winGame()
        assertEquals(R.string.status_game_over_you_win_text, viewModel.uiState.value.statusResId)
        assertEquals(emptyList<UiEvent>(), events)
    }

    @Test
    fun startingNewGameAfterWinRequestsReviewOnce() {
        winGame()
        val eventsBeforeNewGame = events.toList()
        viewModel.startNewGame()
        viewModel.startNewGame()
        assertEquals(emptyList<UiEvent>(), eventsBeforeNewGame)
        assertEquals(listOf<UiEvent>(UiEvent.RequestReview), events)
    }

    @Test
    fun startingNewGameAfterLossDoesNotRequestReview() {
        loseGame()
        viewModel.startNewGame()
        assertEquals(emptyList<UiEvent>(), events)
    }

    @Test
    fun startingNewGameResetsState() {
        winGame()
        viewModel.startNewGame()
        assertEquals(GamePhase.PLACING, viewModel.uiState.value.phase)
        assertEquals(FleetLayout.initial().cells(), viewModel.uiState.value.personBoardShips)
        assertEquals(emptyList<Coordinate>(), viewModel.uiState.value.computerBoardCrosses)
        assertNull(viewModel.uiState.value.winner)
    }

    private fun findFreeEmptyCell(): Coordinate {
        val computerField = viewModel.getComputerBattleField()
        val ships = computerField.getShipsCoordinates()
        for (i in 0 until SQUARES_COUNT) {
            for (j in 0 until SQUARES_COUNT) {
                val cell = Coordinate(i, j)
                if (ships.none { it.x == i && it.y == j } && computerField.isCellFreeToBeSelected(cell)) {
                    return cell
                }
            }
        }
        throw IllegalStateException("Battle field has no free empty cells")
    }

    private fun winGame() {
        val computerField = viewModel.getComputerBattleField()
        while (!computerField.isGameOver()) {
            viewModel.onComputerBoardTap(computerField.getShipsCoordinates().first())
        }
    }

    private fun loseGame() {
        while (viewModel.uiState.value.phase != GamePhase.OVER) {
            viewModel.onComputerBoardTap(findFreeEmptyCell())
            testDispatcher.scheduler.advanceUntilIdle()
        }
    }
}
