package com.avs.sea.battle

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri

fun openGmail(email: String, subject: String?): Intent {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.setData(Uri.parse("mailto:"))
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    return intent
}

fun getShareIntent(context: Context): Intent {
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = SHARE_INTENT_TYPE
    val shareBody: String = (context.resources.getString(R.string.share_body_text)
            + "\n\n" + PLAY_MARKET_URL)
    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, context.resources.getString(R.string.app_name))
    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
    return sharingIntent
}

fun openMarket(showAppsList: Boolean): Intent {
    val uri = if (showAppsList) {
        Uri.parse("market://search?q=pub:$DEV_NAME")
    } else {
        Uri.parse("market://details?id=$NAMESPACE")
    }
    return Intent(Intent.ACTION_VIEW, uri)
}

fun isDarkThemeOn(context: Context): Boolean {
    return context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}