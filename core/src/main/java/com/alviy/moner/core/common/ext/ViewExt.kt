package com.alviy.moner.core.common.ext

import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding

fun View.getDrawable(@DrawableRes resId: Int) = context.getDrawable(resId)

fun View.getColor(@ColorRes resId: Int) = context.getColor(resId)

fun ViewBinding.getColor(@ColorRes resId: Int) = root.getColor(resId)

fun Fragment.setStatusBarColor(@ColorInt color: Int, isDarkIcon: Boolean = true) {
    val window = requireActivity().window
    window.statusBarColor = color
    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = isDarkIcon
}

/** Shows this dialog under [tag], replacing any dialog already shown with that tag. */
fun DialogFragment.showDialog(manager: FragmentManager, tag: String) {
    if (manager.isStateSaved) return            // app is going to the background; skip to avoid a crash
    manager.beginTransaction().apply {
        manager.findFragmentByTag(tag)?.let { remove(it) }   // removing a DialogFragment also dismisses its dialog
        add(this@showDialog, tag)
    }.commit()
}
