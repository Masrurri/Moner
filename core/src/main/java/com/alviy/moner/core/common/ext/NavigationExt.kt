package com.alviy.moner.core.common.ext

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.navOptions

fun NavController.navigateTo(
    @IdRes resId: Int,
    @IdRes popUpToId: Int? = null,
    isLaunchSingleTop: Boolean = false,
    isInclusive: Boolean = false,
    isSaveState: Boolean = false,
    isRestoreState: Boolean = false,
    bundle: Bundle? = null,
) {
    val options = navOptions {
        popUpToId?.let {
            popUpTo(it) {
                inclusive = isInclusive
                saveState = isSaveState
            }
        }
        launchSingleTop = isLaunchSingleTop
        restoreState = isRestoreState
    }
    navigate(resId, bundle, options)
}
