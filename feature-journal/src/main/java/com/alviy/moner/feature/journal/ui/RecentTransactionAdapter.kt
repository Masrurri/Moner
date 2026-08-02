package com.alviy.moner.feature.journal.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alviy.moner.core.common.ext.toDateMonthYear
import com.alviy.moner.core.common.ext.toIDR
import com.alviy.moner.core.designsystem.iconForCategory
import com.alviy.moner.feature.journal.databinding.ItemJournalBinding
import com.alviy.moner.feature.journal.databinding.ItemJournalDateHeaderBinding
import com.alviy.moner.feature.journal.ui.model.JournalRow
import java.time.LocalDateTime

class RecentTransactionAdapter : ListAdapter<JournalRow, RecyclerView.ViewHolder>(Diff) {

    inner class HeaderViewHolder(
        private val binding: ItemJournalDateHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: JournalRow.DateHeader) {
            binding.root.text = row.label
        }
    }

    inner class TransactionViewHolder(
        private val binding: ItemJournalBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: JournalRow.Transaction) {
            val item = row.item
            binding.ivIcon.setImageResource(iconForCategory(item.categoryName))
            binding.tvTitle.text = item.desc
            val time = runCatching {
                LocalDateTime.parse(item.date).toLocalTime().toString().take(5)
            }.getOrDefault("")
            binding.tvSubtitle.text = "${item.source} · $time"
            binding.tvExpenseAmount.text = "- ${item.amount.toIDR()}"
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is JournalRow.DateHeader -> VIEW_TYPE_HEADER
        is JournalRow.Transaction -> VIEW_TYPE_TRANSACTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(ItemJournalDateHeaderBinding.inflate(inflater, parent, false))
        } else {
            TransactionViewHolder(ItemJournalBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is JournalRow.DateHeader -> (holder as HeaderViewHolder).bind(row)
            is JournalRow.Transaction -> (holder as TransactionViewHolder).bind(row)
        }
    }

    object Diff : DiffUtil.ItemCallback<JournalRow>() {
        override fun areItemsTheSame(oldItem: JournalRow, newItem: JournalRow): Boolean {
            return when {
                oldItem is JournalRow.DateHeader && newItem is JournalRow.DateHeader ->
                    oldItem.label == newItem.label
                oldItem is JournalRow.Transaction && newItem is JournalRow.Transaction ->
                    oldItem.item.categoryId == newItem.item.categoryId && oldItem.item.date == newItem.item.date
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: JournalRow, newItem: JournalRow) = oldItem == newItem
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TRANSACTION = 1
    }
}
