package com.alviy.moner.core.common.ext

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.dpToPx(dp: Float): Float = dp * resources.displayMetrics.density

fun Context.showToast(@StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messageRes, duration).show()
}

/** Installed app version name, or [fallback] when the package can't be read. */
fun Context.appVersionName(fallback: String): String =
    runCatching { packageManager.getPackageInfo(packageName, 0).versionName }
        .getOrNull() ?: fallback
