package com.alviy.moner.api.gmail.parser.text

import java.math.BigDecimal

data class Money(val value: BigDecimal, val currency: String?)

/**
 * Normalises the amount formats seen in Indonesian emails to one exact BigDecimal:
 *   "Rp 26.378,00" -> 26378   "IDR 4,000.00" -> 4000   "16.300" -> 16300   "Rp 100.000,00" -> 100000
 * When both separators appear, the LAST one is the decimal mark. A lone separator followed by exactly
 * three digits is a thousands mark; otherwise it is a decimal mark.
 */
object AmountParser {
    private val PREFIXED = Regex("""(?<![A-Za-z])(Rp\.?|IDR|USD|SGD|EUR|[$€])\s*(\d[\d.,]*)""", RegexOption.IGNORE_CASE)
    private val NUMBER = Regex("""\d[\d.,]*""")

    /** First amount in [raw]. Currency is null when the text has no symbol (e.g. Grab's "16.300"). */
    fun parse(raw: String?): Money? {
        if (raw.isNullOrBlank()) return null
        PREFIXED.find(raw)?.let { m ->
            normalize(m.groupValues[2])?.let { return Money(it, currencyCode(m.groupValues[1])) }
        }
        val number = NUMBER.find(raw)?.value ?: return null
        return normalize(number)?.let { Money(it, null) }
    }

    fun normalize(token: String): BigDecimal? {
        val t = token.trimEnd('.', ',')
        if (t.isEmpty()) return null
        val lastDot = t.lastIndexOf('.')
        val lastComma = t.lastIndexOf(',')
        val decimalSep: Char? = when {
            lastDot >= 0 && lastComma >= 0 -> if (lastDot > lastComma) '.' else ','
            lastDot >= 0 || lastComma >= 0 -> {
                val sep = if (lastDot >= 0) '.' else ','
                val digitsAfter = t.length - t.lastIndexOf(sep) - 1
                if (t.count { it == sep } == 1 && digitsAfter != 3) sep else null
            }
            else -> null
        }
        val plain = if (decimalSep == null) {
            t.filter(Char::isDigit)
        } else {
            val i = t.lastIndexOf(decimalSep)
            t.substring(0, i).filter(Char::isDigit) + "." + t.substring(i + 1).filter(Char::isDigit)
        }
        val value = runCatching { BigDecimal(plain) }.getOrNull() ?: return null
        return if (value.stripTrailingZeros().scale() <= 0) value.setScale(0) else value
    }

    private fun currencyCode(symbol: String): String = when (symbol.uppercase().trimEnd('.')) {
        "RP", "IDR" -> "IDR"
        "$", "USD" -> "USD"
        "€", "EUR" -> "EUR"
        else -> symbol.uppercase()
    }
}