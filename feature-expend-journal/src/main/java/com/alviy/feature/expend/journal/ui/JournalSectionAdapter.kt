package com.alviy.feature.expend.journal.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EdgeEffect
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alviy.app.feature.expend.journal.databinding.ItemJournalSectionBinding
import com.alviy.core.model.ExpenseItem
import com.alviy.core.utils.Utils.toIDR

class JournalSectionAdapter(
    private val onClickAddJournal: (Int, String) -> Unit
) : ListAdapter<ExpenseItem, JournalSectionAdapter.JournalSectionAdapterViewHolder>(DIFF_CALLBACK) {

    inner class JournalSectionAdapterViewHolder(
        private val binding: ItemJournalSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val transactionAdapter = TransactionAdapter()

        init {
            binding.rvJournal.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rvJournal.adapter = transactionAdapter
        }

        fun bind(item: ExpenseItem) {
            binding.tvTransactionSectionLabel.text = "${item.categoryName} Journal"
            binding.tvCategoryLabel.text = item.categoryName
            binding.tvTotalExpense.text = item.totalExpense.toIDR()
            binding.tvMonth.text = item.monthYear
            binding.btnAdd.setOnClickListener {
                onClickAddJournal.invoke(item.categoryId, item.categoryName)
            }
            transactionAdapter.submitList(item.transactions?.sortedByDescending { it.date })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalSectionAdapterViewHolder {
        val binding = ItemJournalSectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JournalSectionAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JournalSectionAdapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ExpenseItem>() {
            override fun areItemsTheSame(oldItem: ExpenseItem, newItem: ExpenseItem): Boolean {
                return oldItem.categoryName == newItem.categoryName && oldItem.monthYear == newItem.monthYear
            }

            override fun areContentsTheSame(oldItem: ExpenseItem, newItem: ExpenseItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}