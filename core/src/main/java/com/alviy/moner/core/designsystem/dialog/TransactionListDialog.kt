package com.alviy.moner.core.designsystem.dialog

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.os.bundleOf
import com.alviy.moner.core.common.base.BaseBottomSheetDialogFragment
import com.alviy.moner.core.databinding.DialogTransactionListBinding
import com.alviy.moner.core.databinding.ItemGmailFetchReceiptBinding

/** One fetched receipt row; every field is already formatted for display. */
data class TransactionReceipt(
    val id: String,
    val channel: String,
    val title: String,
    val destination: String,
    val amount: String,
    val dateTime: String,
    val method: String?,
    val note: String?,
) {
    fun toDisplayText(): String = listOf(
        id, channel, title, destination, amount, dateTime, method.orEmpty(), note.orEmpty(),
    ).joinToString(LINE_BREAK)

    private companion object {
        const val LINE_BREAK = "\n"
    }
}

/** Debug sheet listing receipts fetched from Gmail, one text block per receipt. */
class TransactionListDialog :
    BaseBottomSheetDialogFragment<DialogTransactionListBinding>(DialogTransactionListBinding::inflate) {

    override fun initView(savedInstanceState: Bundle?) {
        val inflater = LayoutInflater.from(requireContext())
        requireArguments().getStringArrayList(ARG_ROWS).orEmpty().forEach { text ->
            ItemGmailFetchReceiptBinding.inflate(inflater).apply {
                tvContent.text = text
                binding.llcDialogTransactionList.addView(root)
            }
        }
    }

    companion object {
        const val TAG = "TransactionListDialog"
        private const val ARG_ROWS = "arg_rows"

        fun newInstance(receipts: List<TransactionReceipt>) = TransactionListDialog().apply {
            arguments = bundleOf(ARG_ROWS to ArrayList(receipts.map(TransactionReceipt::toDisplayText)))
        }
    }
}
