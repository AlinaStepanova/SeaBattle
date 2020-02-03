package com.avs.battleship.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.avs.battleship.R
import com.avs.battleship.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private val customOnTouchListener = View.OnTouchListener(implementCustomTouchListener())

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        binding.mainViewModel = viewModel

        binding.viewPerson.provideViewModel(viewModel)
        binding.viewPC.provideViewModel(viewModel)

        viewModel.status.observe(this, Observer { newStatusId ->
            binding.tvStatus.text = resources.getText(newStatusId)
        })

        viewModel.selectedPoint.observe(this, Observer { point ->
            binding.viewPC.getSelectedPoint(point)
            binding.viewFire.visibility = View.VISIBLE
        })

        viewModel.personShips.observe(this, Observer { coordinates ->
            binding.viewPerson.getShipsCoordinates(coordinates)
            binding.viewStart.visibility = View.VISIBLE
        })

        viewModel.computerSuccessfulShots.observe(this, Observer { coordinates ->
            binding.viewPC.getCrossesCoordinates(coordinates)
        })

        viewModel.computerFailedShots.observe(this, Observer { coordinates ->
            binding.viewPC.getDotsCoordinates(coordinates)
            binding.viewFire.visibility = View.INVISIBLE
        })

        viewModel.startGameEvent.observe(this, Observer { isStarted ->
            if (isStarted) {
                binding.viewStart.visibility = View.INVISIBLE
                binding.viewGenerate.visibility = View.INVISIBLE
            }
        })

        binding.viewGenerate.setOnTouchListener(customOnTouchListener)
        binding.viewFire.setOnTouchListener(customOnTouchListener)
        binding.viewStart.setOnTouchListener(customOnTouchListener)
    }

    private fun implementCustomTouchListener(): (View, MotionEvent) -> Boolean {
        return { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black
                        )
                    )
                    if (v is TextView) v.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                }
                MotionEvent.ACTION_UP -> {
                    v.background =
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.square_background
                        )
                    if (v is TextView)
                        v.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.greyDefault
                            )
                        )
                    v.performClick()
                }
            }
            true
        }
    }
}
