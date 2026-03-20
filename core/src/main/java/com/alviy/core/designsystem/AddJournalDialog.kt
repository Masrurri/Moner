package com.alviy.core.designsystem

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.alviy.app.core.R
import com.alviy.app.core.databinding.DialogAddJournalBinding
import com.alviy.core.utils.Utils.IdrToBigDecimal
import com.alviy.core.utils.Utils.enableIDRFormat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class AddJournalDialog : BottomSheetDialogFragment() {

    private var selectedSource: String? = null
    private var titleLabel: String? = null
    private var descLabel: String? = null
    private var amountLabel: String? = null
    private var primaryBtnLabel: String? = null
    private var secondaryBtnLabel: String? = null

    var onClickPrimaryBtn: ((Triple<String, String, String>) -> Unit)? = null
    private var onClickSecondaryBtn: (() -> Unit)? = null

    private lateinit var binding: DialogAddJournalBinding

    init {
        isCancelable = false
    }

    override fun getTheme(): Int = R.style.RoundedDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DialogAddJournalBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initArgs()
        initView()
    }

    private fun initView() = with(binding) {
        setupRvSource()
        tvTitle.text = titleLabel
        tfDesc.setHint(descLabel)
        tfAmount.setHint(amountLabel)
        tfAmount.getEditText().enableIDRFormat()
        btnPrimary.setOnClickListener {
            onClickPrimaryBtn?.invoke(Triple(tfDesc.getText(), selectedSource.orEmpty(), tfAmount.getText().IdrToBigDecimal().toString()))
        }
        btnSecondary.setOnClickListener {
            dismissAllowingStateLoss()
        }
        tfDesc.doOnTextChanged {
            btnPrimary.isEnabled = tfDesc.getText().isBlank().not() && selectedSource.isNullOrBlank().not() && tfAmount.getText().IdrToBigDecimal() != BigDecimal.ZERO
        }
        tfAmount.doOnTextChanged {
            btnPrimary.isEnabled = tfDesc.getText().isBlank().not() && selectedSource.isNullOrBlank().not() && tfAmount.getText().IdrToBigDecimal() != BigDecimal.ZERO
        }
    }

    private fun setupRvSource() = with(binding) {
        val sourceAdapter = SourceExpenseAdapter {
            selectedSource = it
            btnPrimary.isEnabled = tfDesc.getText().isBlank().not() && selectedSource.isNullOrBlank().not() && tfAmount.getText().IdrToBigDecimal() != BigDecimal.ZERO
        }
        sourceAdapter.submitList(listOf("LinkAja", "Mandiri", "BCA", "OVO", "GoPay", "ShopeePay"))
        rvSourceExpense.adapter = sourceAdapter
        rvSourceExpense.layoutManager = LinearLayoutManager(
            requireContext(),
            HORIZONTAL,
            false
        )
    }

    private fun initArgs() {
        arguments?.let {
            titleLabel = it.getString(ARG_TITLE_LABEL)
            descLabel = it.getString(ARG_DESC_LABEL)
            amountLabel = it.getString(ARG_AMOUNT_LABEL)
            primaryBtnLabel = it.getString(ARG_PRIMARY_BTN_LABEL)
            secondaryBtnLabel = it.getString(ARG_SECONDARY_LABEL)
        }
    }

    companion object {
        private const val ARG_TITLE_LABEL = "arg_title_label"
        private const val ARG_DESC_LABEL = "arg_desc_label"
        private const val ARG_AMOUNT_LABEL = "arg_amount_label"
        private const val ARG_PRIMARY_BTN_LABEL = "arg_primary_label"
        private const val ARG_SECONDARY_LABEL = "arg_secondary_label"

        fun newInstance(
            titleLabel: String,
            descLabel: String,
            amountLabel: String,
            primaryLabel: String,
            secondaryLabel: String
        ) = AddJournalDialog().apply {
            arguments = bundleOf(
                ARG_TITLE_LABEL to titleLabel,
                ARG_DESC_LABEL to descLabel,
                ARG_AMOUNT_LABEL to amountLabel,
                ARG_PRIMARY_BTN_LABEL to primaryLabel,
                ARG_SECONDARY_LABEL to secondaryLabel
            )
        }
    }
}