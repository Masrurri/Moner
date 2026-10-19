package com.alviy.moner.api.gmail.mapper.parse

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * "14 Okt 2026" + "10:07:03 WIB"  /  "12 Okt 2026 09:47:58"  ->  epoch millis (UTC).
 * Understands Indonesian and English month names, 24h and AM/PM time, WIB/WITA/WIT.
 * No zone in the text -> WIB. Date without time -> 00:00. Unparseable -> null (caller falls back).
 */
data class ParsedDate(val millis: Long, val hasTime: Boolean)

/** One display format for every receipt, independent of the phone's locale and time zone. */
object ReceiptDateFormat {
    val ZONE: ZoneId = ZoneId.of("Asia/Jakarta")
    private val DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT)
    private val TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)

    fun date(millis: Long): String = Instant.ofEpochMilli(millis).atZone(ZONE).format(DATE)
    fun time(millis: Long): String = Instant.ofEpochMilli(millis).atZone(ZONE).format(TIME)
}

object ReceiptDateParser {
    private val WIB = ZoneId.of("Asia/Jakarta")

    private val MONTHS = mapOf(
        "jan" to 1, "januari" to 1, "january" to 1,
        "feb" to 2, "februari" to 2, "february" to 2,
        "mar" to 3, "maret" to 3, "march" to 3,
        "apr" to 4, "april" to 4,
        "mei" to 5, "may" to 5,
        "jun" to 6, "juni" to 6, "june" to 6,
        "jul" to 7, "juli" to 7, "july" to 7,
        "agu" to 8, "agt" to 8, "ags" to 8, "agustus" to 8, "aug" to 8, "august" to 8,
        "sep" to 9, "sept" to 9, "september" to 9,
        "okt" to 10, "oct" to 10, "oktober" to 10, "october" to 10,
        "nov" to 11, "november" to 11,
        "des" to 12, "dec" to 12, "desember" to 12, "december" to 12,
    )

    private val DMY = Regex("""(\d{1,2})[\s/\-.]+([A-Za-z]{3,9})\.?[\s,/\-.]+(\d{4}|\d{2})(?!\d)""")   // 2026 or 26
    private val ISO = Regex("""(\d{4})-(\d{2})-(\d{2})""")
    private val NUM_DMY = Regex("""(\d{1,2})[/\-](\d{1,2})[/\-](\d{4})""")      // day first
    private val TIME = Regex("""(\d{1,2})[:.](\d{2})(?:[:.](\d{2}))?\s*([AaPp][Mm])?""")
    private val ZONE = Regex("""\b(WIB|WITA|WIT)\b""")
    private val OFFSET = Regex("""(?:^|\s)([+-])(\d{2}):?(\d{2})\b""")             // "+0700"

    fun parse(date: String?, time: String? = null, defaultZone: ZoneId = WIB): Long? =
        parseDetailed(date, time, defaultZone)?.millis

    fun parseDetailed(date: String?, time: String? = null, defaultZone: ZoneId = WIB): ParsedDate? {
        val text = listOfNotNull(date, time).joinToString(" ").trim()
        if (text.isEmpty()) return null
        return runCatching {
            val (localDate, range) = findDate(text) ?: return@runCatching null
            val rest = text.removeRange(range)                       // so "14.10" inside a date isn't read as a time
            val zone = ZONE.find(rest)?.value?.let(::zoneOf)
                ?: OFFSET.find(rest)?.let { ZoneId.of("${it.groupValues[1]}${it.groupValues[2]}:${it.groupValues[3]}") }
                ?: defaultZone
            val timeMatch = TIME.find(rest)
            val localTime = timeMatch?.let { m ->
                var hour = m.groupValues[1].toInt()
                when (m.groupValues[4].lowercase()) {
                    "pm" -> if (hour < 12) hour += 12
                    "am" -> if (hour == 12) hour = 0
                }
                LocalTime.of(hour, m.groupValues[2].toInt(), m.groupValues[3].ifEmpty { "0" }.toInt())
            } ?: LocalTime.MIDNIGHT
            ParsedDate(ZonedDateTime.of(localDate, localTime, zone).toInstant().toEpochMilli(), hasTime = timeMatch != null)
        }.getOrNull()
    }

    private fun findDate(text: String): Pair<LocalDate, IntRange>? {
        DMY.find(text)?.let { m ->
            MONTHS[m.groupValues[2].lowercase()]?.let { month ->
                return LocalDate.of(year(m.groupValues[3]), month, m.groupValues[1].toInt()) to m.range
            }
        }
        ISO.find(text)?.let { m ->
            return LocalDate.of(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt()) to m.range
        }
        NUM_DMY.find(text)?.let { m ->
            return LocalDate.of(m.groupValues[3].toInt(), m.groupValues[2].toInt(), m.groupValues[1].toInt()) to m.range
        }
        return null
    }

    private fun year(s: String): Int = s.toInt().let { if (s.length == 2) 2000 + it else it }

    private fun zoneOf(code: String): ZoneId = when (code) {
        "WITA" -> ZoneId.of("Asia/Makassar")
        "WIT" -> ZoneId.of("Asia/Jayapura")
        else -> WIB
    }
}