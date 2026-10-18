package com.alviy.moner.core.common.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Normalised API error. Nullable + defaults because Gson bypasses Kotlin null-safety. */
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
}