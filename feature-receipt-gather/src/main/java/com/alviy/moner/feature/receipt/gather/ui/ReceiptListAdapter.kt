package com.alviy.moner.feature.receipt.gather.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alviy.moner.api.gmail.mapper.parse.ReceiptDateFormat
import com.alviy.moner.core.common.ext.getColor
import com.alviy.moner.core.common.ext.toIDR
import com.alviy.moner.core.databinding.ItemListHeaderBinding
import com.alviy.moner.core.designsystem.iconForCahannel
import com.alviy.moner.core.domain.model.Receipt
import com.alviy.moner.feature.receipt.gather.R
import com.alviy.moner.feature.receipt.gather.databinding.ItemReceiptBinding
import com.alviy.moner.feature.receipt.gather.ui.model.ReceiptListModel

class ReceiptListAdapter(
    private val onItemClick: (Receipt) -> Unit
) : ListAdapter<ReceiptListModel, RecyclerView.ViewHolder>(Diff) {

    class HeaderViewHolder(
        private val binding: ItemListHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: ReceiptListModel.DateHeader) {
            binding.tvListHeader.text = row.label
        }
    }

    class TransactionViewHolder(
        private val binding: ItemReceiptBinding,
        private val onItemClick: (Receipt) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var current: Receipt? = null

        init {
            binding.clContainer.setOnClickListener { current?.let(onItemClick) }
        }

        fun bind(row: ReceiptListModel.ReceiptItem) {
            val item = row.item
            current = item
            binding.ivIcon.setImageResource(iconForCahannel(item.channel))
            binding.tvTitle.text = item.destination
            binding.tvSubtitle.text = listOfNotNull(
                item.activity.label,
                item.method?.label,
                ReceiptDateFormat.time(item.transactionAt),
            ).joinToString(" · ")
            binding.tvSpendAmount.text = "- ${item.amount.toIDR()}"
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ReceiptListModel.DateHeader -> VIEW_TYPE_HEADER
        is ReceiptListModel.ReceiptItem -> VIEW_TYPE_TRANSACTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(ItemListHeaderBinding.inflate(inflater, parent, false))
            VIEW_TYPE_TRANSACTION -> TransactionViewHolder(
                ItemReceiptBinding.inflate(inflater, parent, false), onItemClick
            )
            else -> error("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind(row as ReceiptListModel.DateHeader)
            is TransactionViewHolder -> holder.bind(row as ReceiptListModel.ReceiptItem)
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