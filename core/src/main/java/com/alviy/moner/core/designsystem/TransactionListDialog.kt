package com.alviy.moner.core.designsystem

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alviy.moner.core.databinding.DialogTransactionListBinding
import com.alviy.moner.core.databinding.ItemGmailFetchReceiptBinding

class TransactionListDialog : FullScreenSheetDialogFragment() {
    private lateinit var binding: DialogTransactionListBinding
    private var receiptList: List<GmailReceipt>? = null

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

    private fun generateList(data: GmailReceipt) {
        ItemGmailFetchReceiptBinding.inflate(LayoutInflater.from(context)).apply {
            tvContent.text = SpannableStringBuilder().apply {
                append(data.id)
                append("\n")
                append(data.merchant)
                append("\n")
                append(data.subject)
                append("\n")
                append(data.amount)
                append("\n")
                append(data.currency)
                append("\n")
                append(data.dateMillis)
                append("\n")
                append(data.snippet)
            }
            binding.llcDialogTransactionList.addView(root)
        }
    }

    companion object {
        fun newInstance(receipts: List<GmailReceipt>) = TransactionListDialog().apply {
            receiptList = receipts
        }

        data class GmailReceipt(
            val id: String,
            val merchant: String,
            val subject: String,
            val amount: String,
            val currency: String,
            val dateMillis: String,
            val snippet: String
        )
    }
}