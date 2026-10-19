package com.alviy.moner.core.common.format

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/**
 * Single source of truth for Rupiah display and parsing ("Rp 1.250.000").
 * Used by list rows, summaries and the live amount input so they never drift apart.
 */
object IdrFormatter {
    const val PREFIX = "Rp "
    val LOCALE: Locale = Locale("in", "ID")

    private val NON_DIGIT = Regex("[^\\d]")

    /** Plain grouped number without the prefix: 1250000 -> "1.250.000". */
    fun formatNumber(value: BigDecimal): String =
        NumberFormat.getNumberInstance(LOCALE)
            .apply { maximumFractionDigits = 0 }
            .format(value)

    /** null -> "Rp 0". */
    fun format(value: BigDecimal?): String = PREFIX + formatNumber(value ?: BigDecimal.ZERO)

    /** Reads back any "Rp 1.250.000"-style text; anything unparseable is zero. */
    fun parse(text: String?): BigDecimal =
        text.orEmpty()
            .replace(PREFIX.trim(), "")
            .replace(NON_DIGIT, "")
            .toBigDecimalOrNull()
            ?: BigDecimal.ZERO
}
