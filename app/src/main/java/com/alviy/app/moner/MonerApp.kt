package com.alviy.app.moner

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MonerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Single static light palette: ignore the system dark setting.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
