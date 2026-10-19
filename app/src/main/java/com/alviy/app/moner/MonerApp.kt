package com.alviy.app.moner

import android.app.Application
import com.alviy.moner.core.common.theme.ThemeManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MonerApp : Application() {

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate() {
        super.onCreate()
        themeManager.applySavedTheme()
    }
}
