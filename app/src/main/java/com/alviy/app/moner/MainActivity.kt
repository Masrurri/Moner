package com.alviy.app.moner

import android.os.Bundle
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
class MainActivity : BaseActivity<ActivityMainBinding>(
    ActivityMainBinding::inflate
) {
    private lateinit var navController: NavController
    private var activeGraphId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        observeData()
    }

    override fun initView() {
        setupNavigation()
    }

    override fun observeData() = Unit

    private fun setupNavigation() {
        navController = NavHostFragment.findNavController(binding.navHost.getFragment())
        binding.bottomNav.setupWithNavController(navController)
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_bottom_nav -> navController.navigateTo(
                    resId = R.id.home_nav,
                    popUpToId = R.id.home_nav
                )
                R.id.budget_bottom_nav -> navController.navigateTo(
                    resId = R.id.budget_nav,
                    popUpToId = R.id.budget_nav
                )
                R.id.settings_bottom_nav -> navController.navigateTo(
                    resId = R.id.settings_nav,
                    popUpToId = R.id.settings_nav
                )
                else -> return@setOnItemSelectedListener false
            }
            true
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            observeActiveBottomNav(destination)
        }
    }

    private fun observeActiveBottomNav(destination: NavDestination) {
        val graphId = destination.parent?.id ?: return
        if (graphId == activeGraphId) return
        activeGraphId = graphId

        val itemId = when (graphId) {
            R.id.home_nav -> R.id.home_bottom_nav
            R.id.budget_nav -> R.id.budget_bottom_nav
            R.id.settings_nav -> R.id.settings_bottom_nav
            else -> null
        }

        binding.bottomNav.isVisible = itemId != null
        if (itemId != null) binding.bottomNav.menu.findItem(itemId)?.isChecked = true
    }

}