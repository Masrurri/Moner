package com.alviy.feature.expend.journal.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alviy.app.feature.expend.journal.databinding.ItemJournalBinding
import com.alviy.core.model.TransactionItem
import com.alviy.core.utils.Utils.toFullMonthYear
import java.time.LocalDateTime

class TransactionAdapter : ListAdapter<TransactionItem, TransactionAdapter.TransactionViewHolder>(DIFF_CALLBACK) {

    inner class TransactionViewHolder(
        private val binding: ItemJournalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionItem) {
            binding.tvTitle.text = item.desc
            binding.tvDate.text = LocalDateTime.parse(item.date).toFullMonthYear()
            binding.tvSource.text = item.source
            binding.tvExpenseAmount.text = item.amount.toPlainString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemJournalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionItem>() {
            override fun areItemsTheSame(
                oldItem: TransactionItem,
                newItem: TransactionItem
            ): Boolean {
                return oldItem.desc == newItem.desc
            }

            override fun areContentsTheSame(
                oldItem: TransactionItem,
                newItem: TransactionItem
            ): Boolean {
                return oldItem.desc == newItem.desc
            }
        }
    }
}