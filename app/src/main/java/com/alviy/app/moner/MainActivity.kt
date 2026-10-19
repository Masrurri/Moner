package com.alviy.app.moner

import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.alviy.app.moner.databinding.ActivityMainBinding
import com.alviy.moner.core.common.base.BaseActivity
import com.alviy.moner.core.common.ext.navigateTo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private lateinit var navController: NavController
    private var activeGraphId: Int? = null

    override fun initView() {
        setupNavigation()
    }

    private fun setupNavigation() {
        navController = NavHostFragment.findNavController(binding.navHost.getFragment())
        binding.bottomNav.setupWithNavController(navController)
        binding.bottomNav.setOnItemSelectedListener { item ->
            val graphId = TABS.entries.firstOrNull { it.value == item.itemId }?.key
                ?: return@setOnItemSelectedListener false
            navController.navigateTo(resId = graphId, popUpToId = graphId)
            true
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            syncBottomNav(destination)
        }
    }

    /** Highlights the tab owning [destination]; hides the bar on screens outside the tabs. */
    private fun syncBottomNav(destination: NavDestination) {
        val graphId = destination.parent?.id ?: return
        if (graphId == activeGraphId) return
        activeGraphId = graphId

        val itemId = TABS[graphId]
        binding.bottomNav.isVisible = itemId != null
        if (itemId != null) binding.bottomNav.menu.findItem(itemId)?.isChecked = true
    }

    private companion object {
        /** Feature graph id -> bottom nav item id. Add a row here to add a tab. */
        val TABS = mapOf(
            R.id.home_nav to R.id.home_bottom_nav,
            R.id.budget_nav to R.id.budget_bottom_nav,
            R.id.settings_nav to R.id.settings_bottom_nav,
        )
    }
}
