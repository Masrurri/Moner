package com.alviy.moner.feature.budget.ui

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.alviy.moner.core.common.base.BaseFragment
import com.alviy.moner.core.common.ext.onDialogResult
import com.alviy.moner.core.common.ext.showDialog
import com.alviy.moner.core.common.ext.toIDR
import com.alviy.moner.core.designsystem.dialog.WheelDialog
import com.alviy.moner.feature.budget.R
import com.alviy.moner.feature.budget.databinding.FragmentBudgetBinding
import com.alviy.moner.feature.budget.viewmodel.BudgetViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetFragment : BaseFragment<FragmentBudgetBinding>(FragmentBudgetBinding::inflate) {

    private val viewModel: BudgetViewModel by viewModels()
    private var categoryBudgetAdapter: CategoryBudgetAdapter? = null

    override fun initView() {
        categoryBudgetAdapter = CategoryBudgetAdapter()
        binding.rvCategoryBudgets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategoryBudgets.adapter = categoryBudgetAdapter

        binding.btnSelectMonth.setOnClickListener { showMonthPicker() }
        binding.btnAddCategoryBudget.setOnClickListener {
            AddCategoryBudgetDialog.newInstance().showDialog(childFragmentManager, AddCategoryBudgetDialog.TAG)
        }

        onDialogResult(REQUEST_MONTH) { result ->
            WheelDialog.selectedItem(result)?.let(viewModel::selectMonthYear)
        }
    }

    override fun observeData() {
        collect(viewModel.selectedMonthYear) { monthYear ->
            binding.btnSelectMonth.text = monthYear.substringBefore(MONTH_YEAR_SEPARATOR)   // "March 2026" -> "March"
        }
        collect(viewModel.budgetSummary) { summary ->
            binding.tvSpentAmount.text = summary.totalSpent.toIDR()
            binding.tvLimitAmount.text = getString(R.string.budget_limit_format, summary.totalLimit.toIDR())
            binding.progressRing.progress = summary.progressFraction
        }
        collect(viewModel.categoryBudgets) { list ->
            categoryBudgetAdapter?.submitList(list)
            binding.cvCategoryBudgets.isVisible = list.isNotEmpty()
            binding.cvEmptyState.isVisible = list.isEmpty()
        }
    }

    override fun onDestroyView() {
        categoryBudgetAdapter = null
        super.onDestroyView()
    }

    private fun showMonthPicker() {
        val months = viewModel.monthYears.value
        if (months.isEmpty()) return
        val currentIndex = months.indexOf(viewModel.selectedMonthYear.value).coerceAtLeast(0)
        WheelDialog.newInstance(months, currentIndex, REQUEST_MONTH)
            .showDialog(childFragmentManager, WheelDialog.TAG)
    }

    private companion object {
        const val REQUEST_MONTH = "budget_request_month"
        const val MONTH_YEAR_SEPARATOR = " "
    }
}
