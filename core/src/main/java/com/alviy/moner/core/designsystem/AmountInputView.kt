package com.alviy.moner.core.designsystem

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.widget.addTextChangedListener
import com.alviy.moner.core.common.ext.IdrToBigDecimal
import com.alviy.moner.core.common.ext.enableIDRFormat
import com.alviy.moner.core.databinding.ViewAmountInputBinding
import java.math.BigDecimal

/**
 * Large, plain-text tappable amount display ("Rp 0") backed by a hidden EditText,
 * matching the design's "amountPlainText" pattern shared by Add Expense and Add Category Budget.
 */
class AmountInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewAmountInputBinding.inflate(
        android.view.LayoutInflater.from(context), this, true
    )

    var onAmountChanged: ((BigDecimal) -> Unit)? = null

    init {
        binding.editAmount.inputType = InputType.TYPE_CLASS_NUMBER
        binding.editAmount.enableIDRFormat()
        binding.editAmount.addTextChangedListener {
            binding.tvAmountDisplay.text = it?.toString().orEmpty()
            onAmountChanged?.invoke(amount)
        }
        binding.tvAmountDisplay.setOnClickListener {
            binding.editAmount.requestFocus()
            binding.editAmount.setSelection(binding.editAmount.text?.length ?: 0)
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(binding.editAmount, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
    }

    val amount: BigDecimal
        get() = binding.editAmount.text?.toString().orEmpty().IdrToBigDecimal()

    fun setAmount(value: BigDecimal) {
        binding.editAmount.setText("Rp ${value.toBigInteger()}")
    }
}
