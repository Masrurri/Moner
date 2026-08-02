package com.alviy.moner.feature.budget.ui

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alviy.moner.core.common.base.BaseFragment
import com.alviy.moner.core.common.ext.toIDR
import com.alviy.moner.core.designsystem.WheelDialog
import com.alviy.moner.feature.budget.databinding.FragmentBudgetBinding
import com.alviy.moner.feature.budget.viewmodel.BudgetViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BudgetFragment : BaseFragment<FragmentBudgetBinding>(
    FragmentBudgetBinding::inflate
) {

    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var categoryBudgetAdapter: CategoryBudgetAdapter

    override val injectedFragment = this

    override fun initView() {
        val binding = binding ?: return

        categoryBudgetAdapter = CategoryBudgetAdapter { }
        binding.rvCategoryBudgets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategoryBudgets.adapter = categoryBudgetAdapter

        binding.btnSelectMonth.setOnClickListener {
            val months = viewModel.monthYears.value
            if (months.isEmpty()) return@setOnClickListener
            val currentIndex = months.indexOf(viewModel.selectedMonthYear.value).coerceAtLeast(0)
            WheelDialog.newInstance(months, currentIndex) { selected ->
                viewModel.selectMonthYear(selected)
            }.show(childFragmentManager, "select_month")
        }

        binding.btnAddCategoryBudget.setOnClickListener {
            AddCategoryBudgetDialog.newInstance().show(childFragmentManager, "add_category_budget")
        }
    }

    override fun observeData() {
        val binding = binding ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedMonthYear.collect { monthYear ->
                        binding.btnSelectMonth.text = monthYear.substringBefore(" ")
                    }
                }
                launch {
                    viewModel.budgetSummary.collect { summary ->
                        binding.tvSpentAmount.text = summary.totalSpent.toIDR()
                        binding.tvLimitAmount.text = "/ ${summary.totalLimit.toIDR()}"
                        binding.progressRing.progress = summary.progressFraction
                    }
                }
                launch {
                    viewModel.categoryBudgets.collect { list ->
                        categoryBudgetAdapter.submitList(list)
                        binding.tvEmptyState.visibility =
                            if (list.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }
}
