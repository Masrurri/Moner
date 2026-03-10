package com.alviy.app.moner

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.alviy.app.moner.databinding.ActivityMainBinding
import com.alviy.core.base.BaseActivity
import com.alviy.core.utils.Utils.navigateTo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(
    ActivityMainBinding::inflate
) {
    private lateinit var navController: NavController
    private var activeBottomNav: CharSequence? = null

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
                R.id.budget_bottom_nav -> navController.navigateTo(
                    resId = com.alviy.app.feature.budget.R.id.budget_nav,
                    popUpToId = com.alviy.app.feature.budget.R.id.budget_nav
                )
                R.id.expend_journal_bottom_nav -> navController.navigateTo(
                    resId = com.alviy.app.feature.expend.journal.R.id.exepnd_journal_nav,
                    popUpToId = com.alviy.app.feature.expend.journal.R.id.exepnd_journal_nav
                )
                R.id.setting_bottom_nav -> navController.navigateTo(
                    resId = com.alviy.app.feature.setting.R.id.setting_nav,
                    popUpToId = com.alviy.app.feature.setting.R.id.setting_nav
                )
                else -> return@setOnItemSelectedListener false
            }
            true
        }
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            observeActiveBottomNav(destination)
        }
    }

    private fun observeActiveBottomNav(destination: NavDestination) {
        if (destination.label == activeBottomNav) return
        activeBottomNav = destination.label
        binding.bottomNav.selectedItemId = when(activeBottomNav) {
            "Budget" -> R.id.budget_bottom_nav
            "Journal" -> R.id.expend_journal_bottom_nav
            else -> R.id.setting_bottom_nav
        }
    }

}