package com.alviy.moner.core.common.format

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/** Keeps an [EditText] formatted as Rupiah ("Rp 1.250.000") while the user types. */
class IdrTextWatcher(private val editText: EditText) : TextWatcher {

    private var isEditing = false

    init {
        editText.post {
            if (editText.text.isNullOrEmpty()) setFormatted(IdrFormatter.format(null))
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (isEditing) return
        isEditing = true
        try {
            setFormatted(IdrFormatter.format(IdrFormatter.parse(s?.toString())))
        } catch (e: Exception) {
            setFormatted(IdrFormatter.format(null))
        } finally {
            isEditing = false
        }
    }

    private fun setFormatted(text: String) {
        editText.setText(text)
        editText.setSelection(text.length)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
}
