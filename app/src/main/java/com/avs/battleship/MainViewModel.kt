package com.avs.battleship

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

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

    private fun makeFire() {

    }

    private fun startGame() {

    }

    private fun generateShips() {

    }
}