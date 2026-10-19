package com.alviy.moner.core.designsystem.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alviy.moner.core.databinding.ItemPillsBinding

/**
 * Single-choice row of pills (categories, payment sources, filters).
 *
 * @param selectFirstByDefault when nothing is selected yet, the first item shows as selected
 *        (without calling [onSelected]) — used by filters whose first item is "All".
 */
class PillAdapter(
    private val selectFirstByDefault: Boolean = false,
    private val onSelected: (String) -> Unit = {},
) : ListAdapter<String, PillAdapter.ViewHolder>(Diff) {

    private var selected: String? = null

    var selectedItem: String?
        get() = selected
        set(value) {
            val previous = selected
            selected = value
            if (previous != value) {
                notifyItemOf(previous)
                notifyItemOf(value)
            }
        }

    inner class ViewHolder(
        private val binding: ItemPillsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String, position: Int) {
            // Plain assignment: notifying from inside onBind would crash during layout.
            if (selectFirstByDefault && selected == null && position == 0) selected = item
            binding.tvCategory.text = item
            binding.containerItemPills.isSelected = item == selected
            binding.containerItemPills.setOnClickListener {
                @Suppress("DEPRECATION")
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                selectedItem = item
                onSelected(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemPillsBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    private fun notifyItemOf(item: String?) {
        val index = item?.let(currentList::indexOf) ?: return
        if (index >= 0) notifyItemChanged(index)
    }

    private object Diff : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}
