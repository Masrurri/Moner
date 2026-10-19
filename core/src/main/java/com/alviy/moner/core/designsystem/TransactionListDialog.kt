package com.alviy.moner.core.designsystem

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alviy.moner.core.databinding.DialogTransactionListBinding
import com.alviy.moner.core.databinding.ItemGmailFetchReceiptBinding
import java.math.BigDecimal

class TransactionListDialog : FullScreenSheetDialogFragment() {
    private lateinit var binding: DialogTransactionListBinding
    private var receiptList: List<TransactionReceipt>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogTransactionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        receiptList?.forEach { generateList(it) }
    }

    private fun generateList(data: TransactionReceipt) {
        ItemGmailFetchReceiptBinding.inflate(LayoutInflater.from(context)).apply {
            tvContent.text = SpannableStringBuilder().apply {
                append(data.id)
                append("\n")
                append(data.channel)
                append("\n")
                append(data.title)
                append("\n")
                append(data.destination)
                append("\n")
                append(data.amount)
                append("\n")
                append(data.dateTime)
                append("\n")
                append(data.method.orEmpty())
                append("\n")
                append(data.note.orEmpty())
            }
            binding.llcDialogTransactionList.addView(root)
        }
    }

    companion object {
        fun newInstance(receipts: List<TransactionReceipt>) = TransactionListDialog().apply {
            receiptList = receipts
        }

        data class TransactionReceipt(
            val id: String,
            val channel: String,
            val title: String,
            val destination: String,
            val amount: String,
            val dateTime: String,
            val method: String?,
            val note: String?,
        )
    }
}