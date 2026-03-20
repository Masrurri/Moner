package com.alviy.core.designsystem

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.alviy.app.core.R
import com.alviy.app.core.databinding.ItemPillsBinding
import com.alviy.core.utils.Utils.getColor

class SourceExpenseAdapter(
    private val onClick: (String) -> Unit
) : ListAdapter<String, SourceExpenseAdapter.ViewHolder>(CategoryDiff) {
    internal var selectedCategory: String? = null
    inner class ViewHolder(
        private val binding: ItemPillsBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String, position: Int) {
            val isSelected = category == selectedCategory
            binding.tvCategory.apply {
                text = category
                setTextColor(getColor(if (isSelected) R.color.strokeLightPills else R.color.thin_text))
            }
            binding.containerItemPills.background = ContextCompat.getDrawable(
                binding.containerItemPills.context,
                if (isSelected) R.drawable.bg_pills_light_active else R.drawable.bg_pills_light
            )

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