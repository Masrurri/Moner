package com.alviy.moner.core.common.ext

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Every date pattern the app displays or persists, in one place. */
object DatePatterns {
    /** Persisted month key, e.g. "March 2026". Changing it breaks existing rows. */
    const val MONTH_YEAR = "MMMM yyyy"
    const val SHORT_DATE = "dd/MM/yy"
    const val DAY_MONTH_YEAR = "d MMM yyyy"
    const val DAY_FULL_MONTH_YEAR = "d MMMM yyyy"
    const val LEGACY_DATE_TIME = "yyyy-MM-dd HH:mm:ss"
    const val HOUR_MINUTE_LENGTH = 5
}

fun fullMonthYearFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
    DateTimeFormatter.ofPattern(DatePatterns.MONTH_YEAR, locale)

fun LocalDateTime.toDateMonthYear(locale: Locale = Locale.getDefault()): String =
    format(DateTimeFormatter.ofPattern(DatePatterns.SHORT_DATE, locale))

fun LocalDateTime.toFullMonthYear(locale: Locale = Locale.getDefault()): String =
    format(fullMonthYearFormatter(locale))

fun LocalDate.format(pattern: String, locale: Locale = Locale.getDefault()): String =
    format(DateTimeFormatter.ofPattern(pattern, locale))

fun String.toLocalDateTime(pattern: String = DatePatterns.LEGACY_DATE_TIME): LocalDateTime =
    LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern))

/** ISO-8601 timestamp (as stored by Room) -> "HH:mm", or "" when unparseable. */
fun String.isoToHourMinute(): String =
    runCatching { LocalDateTime.parse(this).toLocalTime().toString().take(DatePatterns.HOUR_MINUTE_LENGTH) }
        .getOrDefault("")

/** ISO-8601 timestamp (as stored by Room) -> LocalDate, or null when unparseable. */
fun String.isoToLocalDateOrNull(): LocalDate? =
    runCatching { LocalDateTime.parse(this).toLocalDate() }.getOrNull()
