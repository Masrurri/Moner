package com.alviy.moner.core.designsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.alviy.moner.core.R
import com.alviy.moner.core.databinding.DialogAmountFieldBinding
import java.math.BigDecimal

/** Small dialog with a single IDR amount input, e.g. for setting a spending limit. */
class AmountFieldDialog : DialogFragment() {
    private lateinit var binding: DialogAmountFieldBinding
    private var title: String = ""
    private var buttonText: String = ""
    private var initialAmount: BigDecimal? = null
    private var onSubmit: ((BigDecimal) -> Unit)? = null

    override fun onStart() {
        super.onStart()
        val margin = resources.getDimensionPixelSize(R.dimen.category_dialog_margin_horizontal)
        dialog?.window?.setLayout(
            resources.displayMetrics.widthPixels - (margin * 2),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.RoundedDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogAmountFieldBinding.inflate(inflater, container, false)
        return binding.clContainer
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitle.text = title
        binding.btnPrimary.text = buttonText
        binding.amountInput.onAmountChanged = { binding.btnPrimary.isEnabled = it > BigDecimal.ZERO }
        initialAmount?.let(binding.amountInput::setAmount)
        binding.amountInput.requestAmountFocus()
        binding.btnPrimary.setOnClickListener {
            onSubmit?.invoke(binding.amountInput.amount)
            dismiss()
        }
    }

    companion object {
        fun newInstance(
            title: String,
            buttonText: String,
            initialAmount: BigDecimal? = null,
            onSubmit: (BigDecimal) -> Unit
        ) = AmountFieldDialog().apply {
            this.title = title
            this.buttonText = buttonText
            this.initialAmount = initialAmount
            this.onSubmit = onSubmit
        }
    }
}
