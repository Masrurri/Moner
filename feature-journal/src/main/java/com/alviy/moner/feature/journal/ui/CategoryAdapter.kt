package com.alviy.moner.feature.journal.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.alviy.moner.core.databinding.ItemPillsBinding

class CategoryAdapter(
    private val onClick: (String) -> Unit
) : ListAdapter<String, CategoryAdapter.ViewHolder>(CategoryDiff) {
    internal var selectedCategory: String? = null
    inner class ViewHolder(
        private val binding: ItemPillsBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String, position: Int) {
            if (selectedCategory == null && position == 0) selectedCategory = category
            val isSelected = category == selectedCategory
            binding.tvCategory.text = category
            binding.containerItemPills.isSelected = isSelected

            binding.containerItemPills.setOnClickListener {
                val pos = adapterPosition
                if (pos == NO_POSITION) return@setOnClickListener

                val previous = selectedCategory
                selectedCategory = category

                previous?.let { notifyItemChanged(currentList.indexOf(it)) }
                notifyItemChanged(pos)

                onClick(category)
            }
        }
    }

    fun setSelected(category: String) {
        val previous = selectedCategory
        selectedCategory = category

        previous?.let { notifyItemChanged(currentList.indexOf(it)) }
        selectedCategory?.let { notifyItemChanged(currentList.indexOf(it)) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPillsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    object CategoryDiff : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}