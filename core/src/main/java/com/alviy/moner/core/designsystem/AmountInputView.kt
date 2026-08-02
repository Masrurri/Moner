package com.alviy.moner.core.designsystem

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.widget.addTextChangedListener
import com.alviy.moner.core.common.ext.IdrToBigDecimal
import com.alviy.moner.core.common.ext.enableIDRFormat
import com.alviy.moner.core.databinding.ViewAmountInputBinding
import java.math.BigDecimal

/**
 * Large, centered amount input ("Rp 0") matching the design's "amountPlainText"
 * pattern shared by Add Expense and Add Category Budget. Backed by a single
 * visible EditText so the caret renders and stays in sync with the live
 * thousand-separator formatting (no shadow/hidden view to fall out of sync).
 */
class AmountInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewAmountInputBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    var onAmountChanged: ((BigDecimal) -> Unit)? = null

    init {
        binding.editAmount.inputType = InputType.TYPE_CLASS_NUMBER
        binding.editAmount.enableIDRFormat()
        binding.editAmount.addTextChangedListener {
            onAmountChanged?.invoke(amount)
        }
    }

    val amount: BigDecimal
        get() = binding.editAmount.text?.toString().orEmpty().IdrToBigDecimal()

    fun setAmount(value: BigDecimal) {
        binding.editAmount.setText("Rp ${value.toBigInteger()}")
    }

    fun requestAmountFocus() {
        binding.editAmount.requestFocus()
    }
}
