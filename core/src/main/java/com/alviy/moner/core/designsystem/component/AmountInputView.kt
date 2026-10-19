package com.alviy.moner.core.designsystem.component

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.widget.addTextChangedListener
import com.alviy.moner.core.common.ext.enableIDRFormat
import com.alviy.moner.core.common.format.IdrFormatter
import com.alviy.moner.core.databinding.ViewAmountInputBinding
import java.math.BigDecimal

/**
 * Large, centered Rupiah input ("Rp 0") shared by Add Expense and Add Category Budget.
 * Backed by a single visible EditText so the caret stays in sync with the live
 * thousand-separator formatting.
 */
class AmountInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewAmountInputBinding.inflate(LayoutInflater.from(context), this, true)

    var onAmountChanged: ((BigDecimal) -> Unit)? = null

    val amount: BigDecimal
        get() = IdrFormatter.parse(binding.editAmount.text?.toString())

    init {
        binding.editAmount.inputType = InputType.TYPE_CLASS_NUMBER
        binding.editAmount.enableIDRFormat()
        binding.editAmount.addTextChangedListener { onAmountChanged?.invoke(amount) }
    }

    fun setAmount(value: BigDecimal) {
        binding.editAmount.setText(IdrFormatter.format(value))
    }

    fun requestAmountFocus() {
        binding.editAmount.requestFocus()
    }
}
