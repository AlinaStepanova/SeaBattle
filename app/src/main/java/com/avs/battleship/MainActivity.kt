package com.avs.battleship

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.avs.battleship.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private val customOnTouchListener = View.OnTouchListener(implementCustomTouchListener())


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        binding.buttonGenerate.setOnTouchListener(customOnTouchListener)
        binding.buttonFire.setOnTouchListener(customOnTouchListener)
        binding.buttonStart.setOnTouchListener(customOnTouchListener)
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
                    if (v is TextView) v.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
                MotionEvent.ACTION_UP -> {
                    v.background =
                        ContextCompat.getDrawable(this, R.drawable.square_background)
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
