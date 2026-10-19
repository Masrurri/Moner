package com.alviy.app.moner.navigation

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.alviy.app.moner.R
import com.alviy.moner.core.navigation.AppNavigator

class AppNavigatorImpl(private val navController: NavController) : AppNavigator {

    override fun navigateToReceiptGather(scenario: String?) {
        navController.navigate(
            R.id.receipt_gather_nav_graph,
            bundleOf("scenario" to scenario)
        )
    }

    override fun back() {
        navController.popBackStack()
    }
}