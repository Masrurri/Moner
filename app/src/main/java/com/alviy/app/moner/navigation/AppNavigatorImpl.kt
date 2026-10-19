package com.alviy.app.moner.navigation

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.alviy.app.moner.R
import com.alviy.moner.core.navigation.AppNavigator
import com.alviy.moner.core.navigation.NavArgs

/** The only place that knows every feature graph id; features navigate through [AppNavigator]. */
class AppNavigatorImpl(private val navController: NavController) : AppNavigator {

    override fun navigateToReceiptGather(scenario: String?) {
        navController.navigate(
            R.id.receipt_gather_nav_graph,
            bundleOf(NavArgs.SCENARIO to scenario)
        )
    }

    override fun back() {
        navController.popBackStack()
    }
}
