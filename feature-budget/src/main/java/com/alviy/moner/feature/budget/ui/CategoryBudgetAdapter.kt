package com.alviy.moner.feature.budget.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alviy.moner.core.common.ext.toIDR
import com.alviy.moner.core.designsystem.iconForCategory
import com.alviy.moner.feature.budget.R
import com.alviy.moner.feature.budget.databinding.ItemCategoryBudgetBinding
import com.alviy.moner.feature.budget.domain.model.CategoryBudgetProgress

class CategoryBudgetAdapter(
    private val onClick: (CategoryBudgetProgress) -> Unit
) : ListAdapter<CategoryBudgetProgress, CategoryBudgetAdapter.ViewHolder>(Diff) {

    inner class ViewHolder(
        private val binding: ItemCategoryBudgetBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryBudgetProgress) {
            binding.ivCategoryIcon.setImageResource(iconForCategory(item.categoryName))
            binding.tvCategoryName.text = item.categoryName
            binding.tvSpentLimit.text = "${item.spent.toIDR()} / ${item.limit.toIDR()}"
            binding.progressBar.progress = (item.progressFraction * 100).toInt()

            val context = binding.root.context
            val indicatorColor = ContextCompat.getColor(
                context,
                if (item.isOverBudget) com.alviy.moner.core.R.color.color_danger
                else com.alviy.moner.core.R.color.color_accent_neon
            )
            binding.progressBar.setIndicatorColor(indicatorColor)

            if (item.isOverBudget) {
                binding.tvOverBudget.visibility = View.VISIBLE
                binding.tvOverBudget.text = context.getString(
                    R.string.budget_over_by_format,
                    item.overageAmount.toIDR()
                )
            } else {
                binding.tvOverBudget.visibility = View.GONE
            }

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBudgetBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object Diff : DiffUtil.ItemCallback<CategoryBudgetProgress>() {
        override fun areItemsTheSame(oldItem: CategoryBudgetProgress, newItem: CategoryBudgetProgress) =
            oldItem.categoryName == newItem.categoryName

        override fun areContentsTheSame(oldItem: CategoryBudgetProgress, newItem: CategoryBudgetProgress) =
            oldItem == newItem
    }
}
