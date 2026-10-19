package com.alviy.moner.core.navigation

import androidx.annotation.IdRes
import androidx.navigation.NavDirections

sealed interface NavCommand {
    data class GoTo(val directions: NavDirections) : NavCommand
    data object Back : NavCommand
    data class BackTo(@param:IdRes val destinationId: Int, val inclusive: Boolean = false) : NavCommand
}