package com.alviy.moner.feature.budget.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alviy.moner.core.common.ext.toIDR
import com.alviy.moner.core.designsystem.icon.CategoryIcon
import com.alviy.moner.feature.budget.R
import com.alviy.moner.feature.budget.databinding.ItemCategoryBudgetBinding
import com.alviy.moner.feature.budget.domain.model.CategoryBudgetProgress
import com.alviy.moner.core.R as CoreR

class CategoryBudgetAdapter(
    private val onClick: (CategoryBudgetProgress) -> Unit = {}
) : ListAdapter<CategoryBudgetProgress, CategoryBudgetAdapter.ViewHolder>(Diff) {

    inner class ViewHolder(
        private val binding: ItemCategoryBudgetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryBudgetProgress) {
            val context = binding.root.context
            binding.ivCategoryIcon.setImageResource(CategoryIcon.forName(item.categoryName))
            binding.tvCategoryName.text = item.categoryName
            binding.tvSpentLimit.text = context.getString(
                R.string.budget_spent_limit_format, item.spent.toIDR(), item.limit.toIDR()
            )
            binding.progressBar.progress = (item.progressFraction * PERCENT).toInt()
            binding.progressBar.setIndicatorColor(
                context.getColor(if (item.isOverBudget) CoreR.color.color_danger else CoreR.color.color_accent_neon)
            )

            binding.tvOverBudget.isVisible = item.isOverBudget
            if (item.isOverBudget) {
                binding.tvOverBudget.text = context.getString(R.string.budget_over_by_format, item.overageAmount.toIDR())
            }

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemCategoryBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object Diff : DiffUtil.ItemCallback<CategoryBudgetProgress>() {
        override fun areItemsTheSame(oldItem: CategoryBudgetProgress, newItem: CategoryBudgetProgress) =
            oldItem.categoryName == newItem.categoryName

        override fun areContentsTheSame(oldItem: CategoryBudgetProgress, newItem: CategoryBudgetProgress) =
            oldItem == newItem
    }

    private companion object {
        const val PERCENT = 100
    }
}
