package com.alviy.moner.core.common.ext

import android.widget.EditText
import com.alviy.moner.core.common.format.IdrFormatter
import com.alviy.moner.core.common.format.IdrTextWatcher
import java.math.BigDecimal

fun BigDecimal?.toIDR(): String = IdrFormatter.format(this)

fun String?.idrToBigDecimal(): BigDecimal = IdrFormatter.parse(this)

fun EditText.enableIDRFormat() {
    addTextChangedListener(IdrTextWatcher(this))
}
