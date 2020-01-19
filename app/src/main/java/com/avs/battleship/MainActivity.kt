package com.avs.battleship

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.avs.battleship.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.buttonFire.setOnClickListener {  }
        binding.buttonGenerate.setOnClickListener {  }
        binding.buttonStart.setOnClickListener {  }
    }
}
