package com.alviy.moner.core.common.theme

import androidx.appcompat.app.AppCompatDelegate
import com.alviy.moner.core.data.local.preferences.AppPreferences
import javax.inject.Inject
import javax.inject.Singleton

/** Owns the light/dark choice: persists it and applies it app-wide. */
@Singleton
class ThemeManager @Inject constructor(
    private val preferences: AppPreferences,
) {
    val isDarkMode: Boolean get() = preferences.isDarkModeEnabled

    /** Call once from Application.onCreate. */
    fun applySavedTheme() = apply(preferences.isDarkModeEnabled)

    fun setDarkMode(enabled: Boolean) {
        preferences.isDarkModeEnabled = enabled
        apply(enabled)
    }

    private fun apply(darkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
