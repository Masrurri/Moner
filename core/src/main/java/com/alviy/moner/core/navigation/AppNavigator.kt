package com.alviy.moner.core.navigation

import androidx.navigation.NavController

interface AppNavigator {
    fun navigateToReceiptGather(scenario: String? = null)
    fun back()

    fun interface Factory {
        fun create(navController: NavController): AppNavigator
    }
}