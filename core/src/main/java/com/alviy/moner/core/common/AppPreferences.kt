package com.alviy.moner.core.common

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isDarkModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit { putBoolean(KEY_DARK_MODE, value) }

    var isNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, false)
        set(value) = prefs.edit { putBoolean(KEY_NOTIFICATIONS, value) }

    companion object {
        private const val PREFS_NAME = "moner_prefs"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
    }
}
