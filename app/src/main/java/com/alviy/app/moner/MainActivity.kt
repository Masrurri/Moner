package com.alviy.app.moner

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
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
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)   // BaseActivity calls initView() and observeData()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    override fun initView() {
        setupNavigation()
        applyWindowInsets()
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

    private fun applyWindowInsets() {
        val baseMargin = resources.getDimensionPixelSize(R.dimen.spacing_lg)

        // Content: below the status bar, drawn all the way to the screen bottom.
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHost) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(left = bars.left, top = bars.top, right = bars.right)
            insets
        }

        // Floating pill: above the 3-button nav, normal gap with gesture nav.
        ViewCompat.setOnApplyWindowInsetsListener(binding.cardBottomNav) { v, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = maxOf(baseMargin, navBar.bottom)
            }
            insets   // don't consume, so the other views still get the insets
        }

        // BottomNavigationView: no inset padding inside the pill.
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav) { v, insets ->
            v.updatePadding(bottom = 0)
            insets
        }
    }

}