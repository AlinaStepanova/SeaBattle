package com.avs.sea_battle.privacy_policy

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.avs.sea_battle.PRIVACY_POLICY_URL
import com.avs.sea_battle.R
import com.avs.sea_battle.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_privacy_policy)
        val webView = binding.wvPrivacyPolicy
        webView.webViewClient = WebViewClient()
        webView.loadUrl(PRIVACY_POLICY_URL)
    }
}
