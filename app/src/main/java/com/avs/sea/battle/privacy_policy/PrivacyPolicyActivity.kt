package com.avs.sea.battle.privacy_policy

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.avs.sea.battle.PRIVACY_POLICY_URL
import com.avs.sea.battle.ui.theme.SeaBattleTheme

class PrivacyPolicyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeaBattleTheme {
                var webView by remember { mutableStateOf<WebView?>(null) }
                var canGoBack by remember { mutableStateOf(false) }
                BackHandler(enabled = canGoBack) { webView?.goBack() }
                AndroidView(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = object : WebViewClient() {
                                override fun doUpdateVisitedHistory(
                                    view: WebView?, url: String?, isReload: Boolean
                                ) {
                                    canGoBack = view?.canGoBack() == true
                                }
                            }
                            loadUrl(PRIVACY_POLICY_URL)
                            webView = this
                        }
                    },
                )
            }
        }
    }
}
