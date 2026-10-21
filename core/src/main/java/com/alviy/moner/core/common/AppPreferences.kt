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

    var isNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, false)
        set(value) = prefs.edit { putBoolean(KEY_NOTIFICATIONS, value) }

    /** Gmail receive time (epoch millis) of the newest email already fetched; 0 = never fetched. */
    var receiptsFetchedUntil: Long
        get() = prefs.getLong(KEY_RECEIPTS_FETCHED_UNTIL, 0L)
        set(value) = prefs.edit { putLong(KEY_RECEIPTS_FETCHED_UNTIL, value) }

    companion object {
        private const val PREFS_NAME = "moner_prefs"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_RECEIPTS_FETCHED_UNTIL = "receipts_fetched_until"
    }
}
