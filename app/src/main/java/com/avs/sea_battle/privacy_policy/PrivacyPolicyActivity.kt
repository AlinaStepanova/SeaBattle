package com.avs.sea_battle.privacy_policy

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.avs.sea_battle.PRIVACY_POLICY_URL
import com.avs.sea_battle.R

class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        val webView = findViewById<WebView>(R.id.wvPrivacyPolicy)
        webView.webViewClient = WebViewClient()
        webView.loadUrl(PRIVACY_POLICY_URL)
    }
}
