package com.alviy.moner.core.common.ui

import android.content.Context
import androidx.annotation.StringRes

/**
 * Text produced outside the UI layer (ViewModels, mappers) without holding a Context.
 * The view resolves it with [asString], so every user-facing string can live in strings.xml.
 */
sealed interface UiText {
    data class Dynamic(val value: String) : UiText

    class Resource(@StringRes val resId: Int, vararg val args: Any) : UiText {
        override fun equals(other: Any?): Boolean =
            other is Resource && resId == other.resId && args.contentEquals(other.args)

        override fun hashCode(): Int = 31 * resId + args.contentHashCode()
    }

    fun asString(context: Context): String = when (this) {
        is Dynamic -> value
        is Resource -> context.getString(resId, *args)
    }
}
