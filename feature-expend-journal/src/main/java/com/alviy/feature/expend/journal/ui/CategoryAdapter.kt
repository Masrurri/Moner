package com.alviy.feature.expend.journal.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alviy.app.feature.expend.journal.R
import com.alviy.app.feature.expend.journal.databinding.ItemPillsCategoryBinding

class CategoryAdapter(
    private val onClick: (String) -> Unit
) : ListAdapter<String, CategoryAdapter.ViewHolder>(CategoryDiff) {
    private var selectedPosition = 0
    inner class ViewHolder(
        private val binding: ItemPillsCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String, position: Int) {
            binding.tvCategory.text = category
            val isSelected = position == selectedPosition
            binding.containerItemPillsCategory.background = ContextCompat.getDrawable(
                binding.containerItemPillsCategory.context,
                if (isSelected) R.drawable.bg_pills_active else R.drawable.bg_pills
            )

            binding.containerItemPillsCategory.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                val previous = selectedPosition
                selectedPosition = pos

                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)

                onClick(category)
            }
        }
    }

    fun setSelected(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    fun resetSelectedPos() {
        selectedPosition = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPillsCategoryBinding.inflate(
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