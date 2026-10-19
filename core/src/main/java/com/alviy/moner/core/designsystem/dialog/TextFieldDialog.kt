package com.alviy.moner.core.designsystem.dialog

import android.os.Bundle
import androidx.core.os.bundleOf
import com.alviy.moner.core.R
import com.alviy.moner.core.common.base.BaseDialogFragment
import com.alviy.moner.core.common.ext.sendDialogResult
import com.alviy.moner.core.databinding.DialogTextFieldBinding

/** Single text input dialog ("New Category"). Result: [enteredText]. */
class TextFieldDialog : BaseDialogFragment<DialogTextFieldBinding>(DialogTextFieldBinding::inflate) {

    override val horizontalMarginRes = R.dimen.dialog_margin_horizontal

    private val requestKey: String get() = requireArguments().getString(ARG_REQUEST_KEY).orEmpty()

    override fun initView() {
        binding.tfCategoryName.doOnTextChanged { binding.btnPrimary.isEnabled = it.isNotBlank() }
        binding.btnPrimary.setOnClickListener {
            sendDialogResult(requestKey, bundleOf(RESULT_TEXT to binding.tfCategoryName.text))
            dismiss()
        }
    }

    companion object {
        const val TAG = "TextFieldDialog"
        private const val ARG_REQUEST_KEY = "arg_request_key"
        private const val RESULT_TEXT = "result_text"

        fun newInstance(requestKey: String) = TextFieldDialog().apply {
            arguments = bundleOf(ARG_REQUEST_KEY to requestKey)
        }

        fun enteredText(result: Bundle): String? = result.getString(RESULT_TEXT)?.takeIf { it.isNotBlank() }
    }
}
