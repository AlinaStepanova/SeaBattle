package com.avs.battleship

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    var status = MutableLiveData<Int>()

    init {
        status.value = R.string.welcome_text
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

    fun handlePlayingAreaClick(id: Int, x: Float, y: Float) {
        when (id) {
            R.id.viewPC -> {

            }
            R.id.viewPerson -> {

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