package com.alviy.moner.core.common.ext

import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.alviy.moner.core.common.base.BaseFragment

fun View.getDrawable(@DrawableRes resId: Int) = context.getDrawable(resId)

fun View.getColor(@ColorRes resId: Int) = context.getColor(resId)

fun ViewBinding.getColor(@ColorRes resId: Int) = root.getColor(resId)

fun Fragment.setStatusBarColor(@ColorInt color: Int, isDarkIcon: Boolean = true) {
    requireActivity().window.statusBarColor = color
    WindowCompat.getInsetsController(
        requireActivity().window,
        requireActivity().window.decorView
    ).isAppearanceLightStatusBars = isDarkIcon
}