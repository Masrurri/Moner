package com.alviy.moner.feature.receipt.gather.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alviy.moner.core.common.ext.getColor
import com.alviy.moner.core.common.ext.toIDR
import com.alviy.moner.core.databinding.ItemListHeaderBinding
import com.alviy.moner.core.designsystem.iconForCahannel
import com.alviy.moner.feature.receipt.gather.R
import com.alviy.moner.feature.receipt.gather.databinding.ItemReceiptBinding
import com.alviy.moner.feature.receipt.gather.ui.model.ReceiptListModel

class ReceiptListAdapter : ListAdapter<ReceiptListModel, RecyclerView.ViewHolder>(Diff) {

    class HeaderViewHolder(
        private val binding: ItemListHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: ReceiptListModel.DateHeader) {
            binding.tvListHeader.text = row.label
        }
    }

    class TransactionViewHolder(
        private val binding: ItemReceiptBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: ReceiptListModel.ReceiptItem) {
            val item = row.item
            binding.ivIcon.setImageResource(iconForCahannel(item.channel))
            binding.tvTitle.text = item.destination
            binding.tvSubtitle.text = "${item.method} · ${item.date} · ${item.time}"
            binding.tvSpendAmount.text = "- ${item.amount.toIDR()}"
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ReceiptListModel.DateHeader -> VIEW_TYPE_HEADER
        is ReceiptListModel.ReceiptItem -> VIEW_TYPE_TRANSACTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(ItemListHeaderBinding.inflate(inflater, parent, false))
        } else {
            TransactionViewHolder(ItemReceiptBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is ReceiptListModel.DateHeader -> (holder as HeaderViewHolder).bind(row)
            is ReceiptListModel.ReceiptItem -> (holder as TransactionViewHolder).bind(row)
        }
    }

    object Diff : DiffUtil.ItemCallback<ReceiptListModel>() {
        override fun areItemsTheSame(oldItem: ReceiptListModel, newItem: ReceiptListModel): Boolean = when {
            oldItem is ReceiptListModel.DateHeader && newItem is ReceiptListModel.DateHeader ->
                oldItem.label == newItem.label
            oldItem is ReceiptListModel.ReceiptItem && newItem is ReceiptListModel.ReceiptItem ->
                oldItem.item.id == newItem.item.id
            else -> false
        }

        override fun areContentsTheSame(oldItem: ReceiptListModel, newItem: ReceiptListModel) = oldItem == newItem
    }

    private companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_TRANSACTION = 1
    }
}