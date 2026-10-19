package com.alviy.moner.core.common.result

import androidx.annotation.Keep

/**
 * Normalised API error. Nullable + defaults because Gson bypasses Kotlin null-safety.
 * @Keep: Gson parses it from error bodies by field name, which R8 would otherwise rename.
 */
@Keep
data class ErrorResponse(
    val code: String? = null,
    val title: String? = null,
    val message: String? = null,
)

object ErrorCodes {
    const val GENERAL = "80000"
    const val UNAUTHORIZED = "401"
    const val UNAVAILABLE = "503"
    const val TIMEOUT = "504"
    const val UNKNOWN = "0"
}

/**
 * Default copy for errors raised below the UI layer, where no Context is available.
 * Screens that need localised text should map [Result.code] to a string resource instead.
 */
object ErrorMessages {
    const val GENERAL_TITLE = "General Error"
    const val GENERAL_BODY = "Please try again, service unreachable"
    const val AUTHORIZATION_FAILED = "Authorization failed"
}
