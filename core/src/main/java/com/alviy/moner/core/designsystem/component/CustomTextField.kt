package com.alviy.moner.core.designsystem.component

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.content.withStyledAttributes
import androidx.core.widget.doAfterTextChanged
import com.alviy.moner.core.R
import com.alviy.moner.core.databinding.CustomTextFieldBinding
import com.google.android.material.textfield.TextInputLayout

/** Filled, rounded text field of the design system. Set `app:hintText` and `android:inputType` in XML. */
class CustomTextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = CustomTextFieldBinding.inflate(LayoutInflater.from(context), this, true)

    val editText: EditText get() = binding.customEditText

    var text: String
        get() = binding.customEditText.text?.toString().orEmpty()
        set(value) = binding.customEditText.setText(value)

    init {
        context.withStyledAttributes(attrs, R.styleable.CustomTextField) {
            binding.root.hint = getString(R.styleable.CustomTextField_hintText)
            binding.customEditText.inputType =
                getInt(R.styleable.CustomTextField_android_inputType, InputType.TYPE_CLASS_TEXT)
        }
    }

    fun setHint(value: String?) {
        binding.customTextInputLayout.hint = value
    }

    fun setError(message: String?) {
        binding.customTextInputLayout.error = message
    }

    fun clearError() = setError(null)

    fun setStartIcon(@DrawableRes resId: Int) {
        binding.customTextInputLayout.setStartIconDrawable(resId)
    }

    fun setEndIconMode(@TextInputLayout.EndIconMode mode: Int) {
        binding.customTextInputLayout.endIconMode = mode
    }

    fun doOnTextChanged(action: (String) -> Unit) {
        binding.customEditText.doAfterTextChanged { action(it.toString()) }
    }
}
