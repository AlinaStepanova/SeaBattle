package com.avs.sea.battle.main

import com.avs.sea.battle.R
import com.avs.sea.battle.REJECTED_FLASH_MILLIS
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.battle_field.FleetLayout
import com.avs.sea.battle.battle_field.ShipPlacement
import com.avs.sea.battle.ships.Orientation
import com.avs.sea.battle.ui.GamePhase
import com.avs.sea.battle.ui.UiEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelPlacementTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MainViewModel
    private val events = mutableListOf<UiEvent>()
    private lateinit var eventsJob: Job

    private val state get() = viewModel.uiState.value
    private val fourDeckCells = FleetLayout.initial().ships[0].cells()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MainViewModel()
        events.clear()
        eventsJob = CoroutineScope(testDispatcher).launch { viewModel.events.collect { events.add(it) } }
        testDispatcher.scheduler.runCurrent()
    }

    @After
    fun tearDown() {
        eventsJob.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun newGameStartsInPlacementWithTheInitialLayoutAndTheFourDeckSelected() {
        assertEquals(GamePhase.PLACING, state.phase)
        assertEquals(FleetLayout.initial().cells(), state.personBoardShips)
        assertEquals(fourDeckCells, state.selectedShipCells)
        assertEquals(R.string.status_place_ships_text, state.statusResId)
    }

    @Test
    fun tappingAnEmptyCellMovesTheSelectedShipThere() {
        viewModel.onPersonBoardTap(Coordinate(4, 3))
        val moved = ShipPlacement(4, Coordinate(4, 3), Orientation.HORIZONTAL).cells()
        assertEquals(moved, state.selectedShipCells)
        assertTrue(state.personBoardShips.containsAll(moved))
        assertTrue(fourDeckCells.none { it in state.personBoardShips })
    }

    @Test
    fun tappingTheSelectedShipRotatesIt() {
        viewModel.onPersonBoardTap(Coordinate(4, 3))
        viewModel.onPersonBoardTap(Coordinate(4, 5))
        assertEquals(ShipPlacement(4, Coordinate(4, 3), Orientation.VERTICAL).cells(), state.selectedShipCells)
    }

    @Test
    fun tappingAnotherShipSelectsItWithoutMovingAnything() {
        viewModel.onPersonBoardTap(Coordinate(3, 0))
        assertEquals(FleetLayout.initial().ships[2].cells(), state.selectedShipCells)
        assertEquals(FleetLayout.initial().cells(), state.personBoardShips)
    }

    @Test
    fun movesApplyToTheNewlySelectedShip() {
        viewModel.onPersonBoardTap(Coordinate(5, 9))
        viewModel.onPersonBoardTap(Coordinate(5, 5))
        assertEquals(listOf(Coordinate(5, 5)), state.selectedShipCells)
        assertFalse(Coordinate(5, 9) in state.personBoardShips)
    }

    @Test
    fun invalidMoveKeepsTheLayoutAndFlashesTheWouldBeCells() {
        viewModel.onPersonBoardTap(Coordinate(5, 8))
        assertEquals(FleetLayout.initial().cells(), state.personBoardShips)
        assertEquals(fourDeckCells, state.selectedShipCells)
        assertEquals(listOf(Coordinate(5, 8), Coordinate(5, 9)), state.rejectedCells)
    }

    @Test
    fun invalidRotationFlashesTheWouldBeCells() {
        viewModel.onPersonBoardTap(Coordinate(0, 2))
        assertEquals(ShipPlacement(4, Coordinate(0, 1), Orientation.VERTICAL).cells(), state.rejectedCells)
        assertEquals(fourDeckCells, state.selectedShipCells)
    }

    @Test
    fun rejectedFlashClearsAfterTheDelay() {
        viewModel.onPersonBoardTap(Coordinate(5, 8))
        testDispatcher.scheduler.advanceTimeBy(REJECTED_FLASH_MILLIS - 1)
        testDispatcher.scheduler.runCurrent()
        assertTrue(state.rejectedCells.isNotEmpty())
        testDispatcher.scheduler.advanceTimeBy(1)
        testDispatcher.scheduler.runCurrent()
        assertTrue(state.rejectedCells.isEmpty())
    }

    @Test
    fun aNewRejectionRestartsTheFlash() {
        viewModel.onPersonBoardTap(Coordinate(5, 8))
        testDispatcher.scheduler.advanceTimeBy(REJECTED_FLASH_MILLIS - 100)
        viewModel.onPersonBoardTap(Coordinate(8, 8))
        testDispatcher.scheduler.advanceTimeBy(200)
        testDispatcher.scheduler.runCurrent()
        assertEquals(listOf(Coordinate(8, 8), Coordinate(8, 9)), state.rejectedCells)
    }

    @Test
    fun aValidMoveClearsTheFlashOfARefusedOne() {
        viewModel.onPersonBoardTap(Coordinate(5, 8))
        viewModel.onPersonBoardTap(Coordinate(4, 3))
        assertTrue(state.rejectedCells.isEmpty())
    }

    @Test
    fun shufflingClearsTheFlashOfARefusedMove() {
        viewModel.onPersonBoardTap(Coordinate(5, 8))
        viewModel.generateShips()
        assertTrue(state.rejectedCells.isEmpty())
    }

    @Test
    fun generateShipsShufflesAndSelectsTheFourDeckAgain() {
        viewModel.onPersonBoardTap(Coordinate(3, 0))
        viewModel.generateShips()
        assertEquals(20, state.personBoardShips.size)
        assertEquals(4, state.selectedShipCells.size)
        assertTrue(state.personBoardShips.containsAll(state.selectedShipCells))
        assertEquals(GamePhase.PLACING, state.phase)
    }

    @Test
    fun startGameUsesTheArrangedLayout() {
        viewModel.onPersonBoardTap(Coordinate(4, 3))
        val arranged = state.personBoardShips.toSet()
        viewModel.startGame()
        assertEquals(GamePhase.BATTLE, state.phase)
        assertEquals(arranged, state.personBoardShips.toSet())
        assertTrue(state.selectedShipCells.isEmpty())
        assertTrue(state.rejectedCells.isEmpty())
    }

    @Test
    fun boardTapsAreIgnoredOnceTheBattleStarts() {
        viewModel.startGame()
        val ships = state.personBoardShips
        viewModel.onPersonBoardTap(Coordinate(4, 3))
        assertEquals(ships, state.personBoardShips)
        assertTrue(state.selectedShipCells.isEmpty())
    }

    @Test
    fun shufflingIsIgnoredOnceTheBattleStarts() {
        viewModel.startGame()
        val ships = state.personBoardShips
        viewModel.generateShips()
        assertEquals(ships, state.personBoardShips)
        assertTrue(state.selectedShipCells.isEmpty())
    }

    @Test
    fun aLateStartDuringTheComputersTurnIsIgnored() {
        viewModel.startGame()
        missOnce()
        viewModel.startGame()
        assertEquals(GamePhase.COMPUTER_TURN, state.phase)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(state.personBoardDots.isNotEmpty() || state.personBoardCrosses.isNotEmpty())
    }

    @Test
    fun newGameDuringPlacementResetsWithoutAsking() {
        viewModel.onPersonBoardTap(Coordinate(4, 3))
        viewModel.onNewGameRequested()
        assertFalse(state.showNewGameConfirmation)
        assertEquals(FleetLayout.initial().cells(), state.personBoardShips)
    }

    @Test
    fun newGameMidBattleAsksForConfirmation() {
        viewModel.startGame()
        viewModel.onNewGameRequested()
        assertTrue(state.showNewGameConfirmation)
        assertEquals(GamePhase.BATTLE, state.phase)
    }

    @Test
    fun dismissingTheConfirmationKeepsTheGame() {
        viewModel.startGame()
        viewModel.onNewGameRequested()
        viewModel.dismissNewGame()
        assertFalse(state.showNewGameConfirmation)
        assertEquals(GamePhase.BATTLE, state.phase)
    }

    @Test
    fun confirmingReturnsToPlacementWithTheInitialLayout() {
        viewModel.startGame()
        viewModel.onNewGameRequested()
        viewModel.startNewGame()
        assertFalse(state.showNewGameConfirmation)
        assertEquals(GamePhase.PLACING, state.phase)
        assertEquals(fourDeckCells, state.selectedShipCells)
        testDispatcher.scheduler.runCurrent()
        assertEquals(emptyList<UiEvent>(), events)
    }

    @Test
    fun newGameDuringTheComputersTurnAsksAndCancelsItsPendingMove() {
        viewModel.startGame()
        missOnce()
        assertEquals(GamePhase.COMPUTER_TURN, state.phase)
        viewModel.onNewGameRequested()
        assertTrue(state.showNewGameConfirmation)
        viewModel.startNewGame()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(GamePhase.PLACING, state.phase)
        assertTrue(state.personBoardDots.isEmpty())
        assertTrue(state.personBoardCrosses.isEmpty())
    }

    @Test
    fun newGameAfterGameOverResetsWithoutAsking() {
        viewModel.startGame()
        val computerField = viewModel.getComputerBattleField()
        while (!computerField.isGameOver()) {
            viewModel.handlePCAreaClick(computerField.getShipsCoordinates().first())
            viewModel.makeFireAsPerson()
        }
        viewModel.onNewGameRequested()
        assertFalse(state.showNewGameConfirmation)
        assertEquals(GamePhase.PLACING, state.phase)
        testDispatcher.scheduler.runCurrent()
        assertEquals(listOf<UiEvent>(UiEvent.RequestReview), events)
    }

    private fun missOnce() {
        val computerField = viewModel.getComputerBattleField()
        val ships = computerField.getShipsCoordinates()
        val miss = (0 until 10).flatMap { x -> (0 until 10).map { y -> Coordinate(x, y) } }.first { it !in ships }
        viewModel.handlePCAreaClick(miss)
        viewModel.makeFireAsPerson()
        assertNotEquals(GamePhase.BATTLE, state.phase)
    }
}
