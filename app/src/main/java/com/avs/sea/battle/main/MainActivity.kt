package com.avs.sea.battle.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.avs.sea.battle.R
import com.avs.sea.battle.RECIPIENTS
import com.avs.sea.battle.getShareIntent
import com.avs.sea.battle.openGmail
import com.avs.sea.battle.openMarket
import com.avs.sea.battle.privacy_policy.PrivacyPolicyActivity
import com.avs.sea.battle.ui.GameScreen
import com.avs.sea.battle.ui.MenuAction
import com.avs.sea.battle.ui.UiEvent
import com.avs.sea.battle.ui.theme.SeaBattleTheme
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        UiEvent.RequestReview -> launchReviewFlow()
                    }
                }
            }
        }
        setContent {
            SeaBattleTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                GameScreen(
                    uiState = uiState,
                    onGenerateShips = viewModel::generateShips,
                    onStartGame = viewModel::startGame,
                    onNewGame = viewModel::startNewGame,
                    onCellClick = viewModel::handlePCAreaClick,
                    onFire = viewModel::makeFireAsPerson,
                    onMenuAction = ::handleMenuAction,
                )
            }
        }
    }

    private fun handleMenuAction(action: MenuAction) {
        when (action) {
            MenuAction.SHARE -> startActivity(
                Intent.createChooser(getShareIntent(this), getString(R.string.share_text))
            )
            MenuAction.RATE -> openActivity(
                { startActivity(openMarket(false)) }, R.string.cannot_open_market_error_text
            )
            MenuAction.MORE_APPS -> openActivity(
                { startActivity(openMarket(true)) }, R.string.cannot_open_market_error_text
            )
            MenuAction.WRITE_TO_AUTHOR -> openActivity(
                { startActivity(openGmail(RECIPIENTS, getString(R.string.app_name))) },
                R.string.cannot_send_email_error_text
            )
            MenuAction.PRIVACY_POLICY -> startActivity(
                Intent(this, PrivacyPolicyActivity::class.java)
            )
        }
    }

    private fun openActivity(call: () -> Unit, messageId: Int) {
        try {
            call()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(messageId), Toast.LENGTH_LONG).show()
        }
    }

    private fun launchReviewFlow() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener {
            try {
                val flow = manager.launchReviewFlow(this, it.result)
                flow.addOnCompleteListener {
                    Log.d("Review", "Review flow completed")
                }
            } catch (e: Exception) {
                Log.e("Review", "Error: ${e.message}")
            }
        }
    }
}
