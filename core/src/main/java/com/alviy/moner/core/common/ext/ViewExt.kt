package com.alviy.moner.core.common.ext

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

fun View.getDrawable(@DrawableRes resId: Int) = context.getDrawable(resId)

fun View.getColor(@ColorRes resId: Int) = context.getColor(resId)
