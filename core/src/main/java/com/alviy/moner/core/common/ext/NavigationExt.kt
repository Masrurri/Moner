package com.alviy.moner.core.common.ext

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.navOptions

fun NavController.navigateTo(
    resId: Int,
    popUpToId: Int? = null,
    isLaunchSingleTop: Boolean? = null,
    isInclusive: Boolean? = null,
    isSaveState: Boolean? = null,
    bundle: Bundle? = null
) {
    val navOptions = navOptions {
        popUpToId?.let {
            popUpTo(it) {
                this.inclusive = isInclusive.orFalse()
                this.saveState = isSaveState.orFalse()
            }
        }
        this.launchSingleTop = isLaunchSingleTop.orFalse()
    }
    this.navigate(resId, bundle, navOptions)
}
