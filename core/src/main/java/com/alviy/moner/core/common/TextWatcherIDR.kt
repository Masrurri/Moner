package com.alviy.moner.core.common

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class TextWatcherIDR(private val editText: EditText) : TextWatcher {

    private var isEditing = false
    private val prefix = "Rp "

    init {
        editText.post {
            if (editText.text.isNullOrEmpty()) {
                setDefault()
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (isEditing) return
        isEditing = true

        try {
            val clean = s.toString()
                .replace(prefix, "")
                .replace("[^\\d]".toRegex(), "")

            val parsed = clean.toBigDecimalOrNull() ?: BigDecimal.ZERO

            val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
            formatter.maximumFractionDigits = 0

            val formatted = "$prefix${formatter.format(parsed)}"

            editText.setText(formatted)
            editText.setSelection(formatted.length)
        } catch (e: Exception) {
            setDefault()
        }

        isEditing = false
    }

    private fun setDefault() {
        val text = "Rp 0"
        editText.setText(text)
        editText.setSelection(text.length)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}