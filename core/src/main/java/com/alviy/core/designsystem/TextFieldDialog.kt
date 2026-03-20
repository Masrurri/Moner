package com.alviy.core.designsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.alviy.app.core.R
import com.alviy.app.core.databinding.DialogTextFieldBinding

class TextFieldDialog : DialogFragment() {
    private lateinit var binding: DialogTextFieldBinding
    private var onClickPrimaryBtn: ((String) -> Unit)? = null

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogTextFieldBinding.inflate(inflater, container, false)
        return binding.clContainer
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tfCategoryName.doOnTextChanged {
            binding.btnPrimary.isEnabled = it.isBlank().not()
        }
        setupButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun setupButtons() {
        binding.btnPrimary.setOnClickListener {
            onClickPrimaryBtn?.invoke(binding.tfCategoryName.getText())
            dismiss()
        }
    }

    companion object {
        fun newInstance(onClickPrimaryBtn: ((String) -> Unit)? = null): TextFieldDialog {
            val dialog = TextFieldDialog()
            dialog.onClickPrimaryBtn = onClickPrimaryBtn
            return dialog
        }
    }
}