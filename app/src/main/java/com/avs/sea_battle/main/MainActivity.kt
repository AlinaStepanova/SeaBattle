package com.avs.sea_battle.main

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.avs.sea_battle.*
import com.avs.sea_battle.databinding.ActivityMainBinding
import com.avs.sea_battle.privacy_policy.PrivacyPolicyActivity

class MainActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private val customOnTouchListener = View.OnTouchListener(implementCustomTouchListener())

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.mainViewModel = viewModel

        binding.viewPerson.provideViewModel(viewModel)
        binding.viewComputer.provideViewModel(viewModel)

        viewModel.status.observe(this, Observer { newStatusId ->
            binding.tvStatus.text = resources.getText(newStatusId)
        })

        viewModel.selectedByPersonCoordinate.observe(this, Observer { point ->
            binding.viewComputer.getSelectedCoordinate(point)
            binding.viewFire.visibility = if (point == null) View.INVISIBLE else View.VISIBLE
        })

        viewModel.selectedByComputerCoordinate.observe(this, Observer {
            binding.progressBar.visibility = View.VISIBLE
        })

        viewModel.personShips.observe(this, Observer { coordinates ->
            binding.viewPerson.getShipsCoordinates(coordinates)
            if (coordinates.isNotEmpty()) {
                binding.viewStart.visibility = View.VISIBLE
            }
        })

        viewModel.personSuccessfulShots.observe(this, Observer { coordinates ->
            binding.viewComputer.getCrossesCoordinates(coordinates)
            binding.viewFire.visibility = View.INVISIBLE
        })

        viewModel.personFailedShots.observe(this, Observer { coordinates ->
            binding.viewComputer.getDotsCoordinates(coordinates)
            binding.viewFire.visibility = View.INVISIBLE
        })

        viewModel.computerSuccessfulShots.observe(this, Observer { coordinates ->
            binding.viewPerson.getCrossesCoordinates(coordinates)
            binding.progressBar.visibility = View.INVISIBLE
        })

        viewModel.computerFailedShots.observe(this, Observer { coordinates ->
            binding.viewPerson.getDotsCoordinates(coordinates)
            binding.progressBar.visibility = View.INVISIBLE
        })

        viewModel.startGameEvent.observe(this, Observer { isStarted ->
            if (isStarted) binding.viewStart.visibility = View.INVISIBLE
            if (!isStarted) binding.viewNewGame.visibility = View.INVISIBLE
            binding.viewGenerate.visibility = if (isStarted) View.INVISIBLE else View.VISIBLE
        })

        viewModel.endGameEvent.observe(this, Observer { isEnded ->
            binding.viewNewGame.visibility = if (isEnded) View.VISIBLE else View.INVISIBLE
        })
        binding.ivMore.setOnClickListener { view -> showPopup(view) }
        binding.viewGenerate.setOnTouchListener(customOnTouchListener)
        binding.viewFire.setOnTouchListener(customOnTouchListener)
        binding.viewStart.setOnTouchListener(customOnTouchListener)
        binding.viewNewGame.setOnTouchListener(customOnTouchListener)
    }

    private fun implementCustomTouchListener(): (View, MotionEvent) -> Boolean {
        return { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
                    setTextColor(v, R.color.colorPrimary)
                }
                MotionEvent.ACTION_UP -> {
                    v.background = ContextCompat.getDrawable(this, R.drawable.square_background)
                    setTextColor(v, R.color.colorPrimaryDark)
                    v.performClick()
                }
            }
            true
        }
    }

    private fun setTextColor(v: View, color: Int) {
        if (v is TextView) v.setTextColor(
            ContextCompat.getColor(this, color)
        )
    }

    private fun showPopup(v: View?) {
        val wrapper: Context = ContextThemeWrapper(this, R.style.PopupStyle)
        if (v != null) {
            PopupMenu(wrapper, v).apply {
                setOnMenuItemClickListener(this@MainActivity)
                inflate(R.menu.menu)
                try {
                    val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                    fieldMPopup.isAccessible = true
                    val popup = fieldMPopup.get(this)
                    popup.javaClass
                        .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                        .invoke(popup, true)
                } catch (e: Exception){
                    if (BuildConfig.DEBUG) Log.d(this::class.java.simpleName, e.toString())
                } finally {
                    show()
                }
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share -> {
                startActivity(Intent.createChooser(getShareIntent(this),
                    resources.getString(R.string.share_text)))
                true
            }
            R.id.rate -> {
                val call = { startActivity(openMarket(false)) }
                openActivity(call, R.string.cannot_open_market_error_text)
                true
            }
            R.id.write_to_author -> {
                val call = { startActivity(openGmail(this, RECIPIENTS, resources.getString(R.string.app_name))) }
                openActivity(call, R.string.cannot_send_email_error_text)
                true
            }
            R.id.more_apps -> {
                val call = { startActivity(openMarket(true)) }
                openActivity(call, R.string.cannot_open_market_error_text)
                true
            }
            R.id.privacy_policy -> {
                startActivity(Intent(this, PrivacyPolicyActivity::class.java))
                true
            }
            else -> false
        }
    }

    private fun openActivity(call: () -> Unit, messageId: Int) {
        try {
            call()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, resources.getString(messageId), Toast.LENGTH_LONG).show()
        }
    }
}
