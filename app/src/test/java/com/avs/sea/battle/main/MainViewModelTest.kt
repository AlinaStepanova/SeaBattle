package com.avs.sea.battle.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.avs.sea.battle.R
import com.avs.sea.battle.SQUARES_COUNT
import com.avs.sea.battle.battle_field.Coordinate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = MainViewModel()
        viewModel.generateShips()
        viewModel.startGame()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun makeFireAsPersonWithoutSelectionKeepsPersonTurn() {
        viewModel.makeFireAsPerson()
        assertEquals(R.string.status_select_to_fire_text, viewModel.status.value)
        assertNull(viewModel.selectedByComputerCoordinate.value)
    }

    @Test
    fun makeFireAsPersonAtEmptyCellPassesTurnToComputer() {
        viewModel.handlePCAreaClick(findEmptyCell())
        viewModel.makeFireAsPerson()
        assertEquals(R.string.status_opponent_shot_text, viewModel.status.value)
        assertNotNull(viewModel.selectedByComputerCoordinate.value)
    }

    @Test
    fun winningGameRequestsReview() {
        winGame()
        assertEquals(true to Player.PERSON, viewModel.endGameEvent.value)
        assertEquals(true, viewModel.showReviewRequest.value)
    }

    @Test
    fun reviewRequestIsClearedOnceLaunched() {
        winGame()
        viewModel.onReviewFlowLaunched()
        assertEquals(false, viewModel.showReviewRequest.value)
    }

    @Test
    fun startingNewGameClearsReviewRequest() {
        winGame()
        viewModel.startNewGame()
        assertEquals(false, viewModel.showReviewRequest.value)
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
