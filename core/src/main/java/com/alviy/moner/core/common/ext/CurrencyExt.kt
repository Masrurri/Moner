package com.alviy.moner.core.common.ext

import android.widget.EditText
import com.alviy.moner.core.common.TextWatcherIDR
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

fun BigDecimal?.toIDR(): String {
    val localeID = Locale("in", "ID")
    val formatter = NumberFormat.getNumberInstance(localeID)
    formatter.maximumFractionDigits = 0

    return if (this == null) "Rp 0" else "Rp ${formatter.format(this)}"
}

fun EditText.enableIDRFormat() {
    this.addTextChangedListener(TextWatcherIDR(this))
}

fun String.IdrToBigDecimal(): BigDecimal {
    val clean = this
        .replace("Rp", "")
        .replace("[^\\d]".toRegex(), "")

    return clean.toBigDecimalOrNull() ?: BigDecimal.ZERO
}
