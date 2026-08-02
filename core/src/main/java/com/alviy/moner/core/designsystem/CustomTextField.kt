package com.alviy.moner.core.designsystem

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import com.alviy.moner.core.R
import com.alviy.moner.core.databinding.CustomTextFieldBinding

class CustomTextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = CustomTextFieldBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        setupAttributes(attrs)
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        attrs ?: return

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomTextField)

        val hint = typedArray.getString(R.styleable.CustomTextField_hintText)
        val inputType = typedArray.getInt(
            R.styleable.CustomTextField_android_inputType,
            InputType.TYPE_CLASS_TEXT
        )

        binding.root.hint = hint
        binding.customEditText.inputType = inputType

        typedArray.recycle()
    }

    fun getText(): String {
        return binding.customEditText.text?.toString().orEmpty()
    }

    fun getEditText(): EditText {
        return binding.customEditText
    }

    fun setText(value: String) {
        binding.customEditText.setText(value)
    }

    fun setHint(value: String?) {
        binding.customTextInputLayout.hint = value
    }

    fun setError(message: String?) {
        binding.customTextInputLayout.error = message
    }

    fun clearError() {
        binding.customTextInputLayout.error = null
    }

    fun setStartIcon(resId: Int) {
        binding.customTextInputLayout.setStartIconDrawable(resId)
    }

    fun setEndIconMode(mode: Int) {
        binding.customTextInputLayout.endIconMode = mode
    }

    fun doOnTextChanged(action: (String) -> Unit) {
        binding.customEditText.doAfterTextChanged {
            action(it.toString())
        }
    }
}