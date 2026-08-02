package com.alviy.moner.core.common.ext

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun fullMonthYearFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy", locale)

fun LocalDateTime.toDateMonthYear(locale: Locale = Locale.getDefault()): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yy", locale)
    return this.format(formatter)
}

fun LocalDateTime.toFullMonthYear(locale: Locale = Locale.getDefault()): String {
    return this.format(fullMonthYearFormatter(locale))
}

fun String.toLocalDateTime(pattern: String = "yyyy-MM-dd HH:mm:ss"): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return LocalDateTime.parse(this, formatter)
}
