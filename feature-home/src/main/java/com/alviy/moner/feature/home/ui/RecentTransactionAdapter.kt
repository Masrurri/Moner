package com.alviy.moner.feature.home.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alviy.moner.core.common.ext.toIDR
import com.alviy.moner.core.designsystem.iconForCategory
import com.alviy.moner.feature.home.databinding.ItemRecentTransactionBinding
import com.alviy.moner.feature.home.databinding.ItemRecentTransactionHeaderBinding
import com.alviy.moner.feature.home.ui.model.RecentTransactionRow
import java.time.LocalDateTime

class RecentTransactionAdapter : ListAdapter<RecentTransactionRow, RecyclerView.ViewHolder>(Diff) {

    class HeaderViewHolder(
        private val binding: ItemRecentTransactionHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: RecentTransactionRow.DateHeader) {
            binding.root.text = row.label
        }
    }

    class TransactionViewHolder(
        private val binding: ItemRecentTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: RecentTransactionRow.Transaction) {
            val item = row.item
            binding.ivIcon.setImageResource(iconForCategory(item.categoryName))
            binding.tvTitle.text = item.desc
            val time = runCatching {
                LocalDateTime.parse(item.date).toLocalTime().toString().take(5)
            }.getOrDefault("")
            binding.tvSubtitle.text = "${item.source} · $time"
            binding.tvSpendAmount.text = "- ${item.amount.toIDR()}"
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is RecentTransactionRow.DateHeader -> VIEW_TYPE_HEADER
        is RecentTransactionRow.Transaction -> VIEW_TYPE_TRANSACTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(ItemRecentTransactionHeaderBinding.inflate(inflater, parent, false))
        } else {
            TransactionViewHolder(ItemRecentTransactionBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is RecentTransactionRow.DateHeader -> (holder as HeaderViewHolder).bind(row)
            is RecentTransactionRow.Transaction -> (holder as TransactionViewHolder).bind(row)
        }
    }

    object Diff : DiffUtil.ItemCallback<RecentTransactionRow>() {
        override fun areItemsTheSame(oldItem: RecentTransactionRow, newItem: RecentTransactionRow): Boolean = when {
            oldItem is RecentTransactionRow.DateHeader && newItem is RecentTransactionRow.DateHeader ->
                oldItem.label == newItem.label
            oldItem is RecentTransactionRow.Transaction && newItem is RecentTransactionRow.Transaction ->
                oldItem.item.id == newItem.item.id
            else -> false
        }

        override fun areContentsTheSame(oldItem: RecentTransactionRow, newItem: RecentTransactionRow) = oldItem == newItem
    }

    private companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_TRANSACTION = 1
    }
}
