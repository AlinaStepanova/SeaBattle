package com.avs.sea_battle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import java.util.*

fun openGmail(activity: Activity, email: Array<String>, subject: String?): Intent? {
    val emailIntent = Intent(Intent.ACTION_SEND)
    emailIntent.putExtra(Intent.EXTRA_EMAIL, email)
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    emailIntent.type = "text/plain"
    val pm = activity.packageManager
    val matches = pm.queryIntentActivities(emailIntent, 0)
    var best: ResolveInfo? = null
    for (info in matches) {
        if (info.activityInfo.packageName.endsWith(".gm")
            || info.activityInfo.name.toLowerCase(Locale.getDefault()).contains("gmail")
        ) best = info
    }
    if (best != null) emailIntent.setClassName(
        best.activityInfo.packageName,
        best.activityInfo.name
    )
    return emailIntent
}

fun getShareIntent(context: Context): Intent {
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = SHARE_INTENT_TYPE
    val shareBody: String = (context.resources.getString(R.string.share_body_text))
    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, context.resources.getString(R.string.app_name))
    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
    return sharingIntent
}

fun openMarket(activity: Activity, showAppsList: Boolean): Intent? {
    val uri = if (showAppsList) {
        Uri.parse("market://search?q=pub:$DEV_NAME")
    } else {
        Uri.parse("market://details?id=" + activity.packageName)
    }
    return Intent(Intent.ACTION_VIEW, uri)
}