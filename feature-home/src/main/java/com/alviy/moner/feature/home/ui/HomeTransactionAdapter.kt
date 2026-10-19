package com.alviy.moner.feature.home.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alviy.moner.core.common.ext.isoToHourMinute
import com.alviy.moner.core.common.ext.toIDR
import com.alviy.moner.core.designsystem.icon.CategoryIcon
import com.alviy.moner.feature.home.R
import com.alviy.moner.feature.home.databinding.ItemHomeDateHeaderBinding
import com.alviy.moner.feature.home.databinding.ItemHomeTransactionBinding
import com.alviy.moner.feature.home.ui.model.HomeRow

/** Transactions grouped under day headers ("Today", "Yesterday", "3 March 2026"). */
class HomeTransactionAdapter : ListAdapter<HomeRow, RecyclerView.ViewHolder>(Diff) {

    class HeaderViewHolder(
        private val binding: ItemHomeDateHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: HomeRow.DateHeader) {
            binding.root.text = row.label.asString(binding.root.context)
        }
    }

    class TransactionViewHolder(
        private val binding: ItemHomeTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: HomeRow.Transaction) {
            val item = row.item
            val context = binding.root.context
            binding.ivIcon.setImageResource(CategoryIcon.forName(item.categoryName))
            binding.tvTitle.text = item.desc
            binding.tvSubtitle.text = context.getString(
                R.string.home_transaction_subtitle_format, item.source, item.date.isoToHourMinute()
            )
            binding.tvExpenseAmount.text = context.getString(
                R.string.home_transaction_amount_format, item.amount.toIDR()
            )
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is HomeRow.DateHeader -> VIEW_TYPE_HEADER
        is HomeRow.Transaction -> VIEW_TYPE_TRANSACTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(ItemHomeDateHeaderBinding.inflate(inflater, parent, false))
            else -> TransactionViewHolder(ItemHomeTransactionBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is HomeRow.DateHeader -> (holder as HeaderViewHolder).bind(row)
            is HomeRow.Transaction -> (holder as TransactionViewHolder).bind(row)
        }
    }

    private object Diff : DiffUtil.ItemCallback<HomeRow>() {
        override fun areItemsTheSame(oldItem: HomeRow, newItem: HomeRow): Boolean = when {
            oldItem is HomeRow.DateHeader && newItem is HomeRow.DateHeader ->
                oldItem.label == newItem.label
            oldItem is HomeRow.Transaction && newItem is HomeRow.Transaction ->
                oldItem.item.categoryId == newItem.item.categoryId && oldItem.item.date == newItem.item.date
            else -> false
        }

        override fun areContentsTheSame(oldItem: HomeRow, newItem: HomeRow) = oldItem == newItem
    }

    private companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_TRANSACTION = 1
    }
}
