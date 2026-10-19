package com.alviy.moner.feature.budget.ui

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.alviy.moner.core.common.base.BaseBottomSheetDialogFragment
import com.alviy.moner.core.common.ext.collectWhile
import com.alviy.moner.core.common.ext.onDialogResult
import com.alviy.moner.core.common.ext.showDialog
import com.alviy.moner.core.designsystem.adapter.PillAdapter
import com.alviy.moner.core.designsystem.dialog.TextFieldDialog
import com.alviy.moner.feature.budget.databinding.DialogAddCategoryBudgetBinding
import com.alviy.moner.feature.budget.domain.model.BudgetPeriod
import com.alviy.moner.feature.budget.viewmodel.BudgetViewModel

/** Add Category Budget sheet. Shares [BudgetViewModel] with the BudgetFragment that shows it. */
class AddCategoryBudgetDialog :
    BaseBottomSheetDialogFragment<DialogAddCategoryBudgetBinding>(DialogAddCategoryBudgetBinding::inflate) {

    // Scoped to the parent BudgetFragment so the budget is saved for the month selected there.
    private val viewModel: BudgetViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private lateinit var categoryAdapter: PillAdapter

    override fun initView(savedInstanceState: Bundle?) {
        categoryAdapter = PillAdapter()
        savedInstanceState?.getString(STATE_SELECTED_CATEGORY)?.let { categoryAdapter.selectedItem = it }
        binding.rvCategory.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategory.adapter = categoryAdapter

        viewModel.availableCategories.collectWhile(viewLifecycleOwner) { categoryAdapter.submitList(it) }

        binding.btnNewCategory.setOnClickListener {
            TextFieldDialog.newInstance(REQUEST_NEW_CATEGORY).showDialog(childFragmentManager, TextFieldDialog.TAG)
        }
        onDialogResult(REQUEST_NEW_CATEGORY) { result ->
            TextFieldDialog.enteredText(result)?.let { name ->
                viewModel.addCategory(name)
                categoryAdapter.selectedItem = name
            }
        }

        binding.btnSaveBudget.setOnClickListener { save() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::categoryAdapter.isInitialized) outState.putString(STATE_SELECTED_CATEGORY, categoryAdapter.selectedItem)
    }

    private fun save() {
        val category = categoryAdapter.selectedItem
        if (category.isNullOrBlank()) return

        val period = if (binding.togglePeriod.checkedButtonId == binding.btnPeriodWeekly.id) {
            BudgetPeriod.WEEKLY
        } else {
            BudgetPeriod.MONTHLY
        }
        viewModel.saveCategoryBudget(
            categoryName = category,
            monthlyLimit = binding.amountInput.amount,
            period = period,
            notifyAt80Percent = binding.switchNotify.isChecked
        )
        dismiss()
    }

    companion object {
        const val TAG = "AddCategoryBudgetDialog"
        private const val REQUEST_NEW_CATEGORY = "add_budget_new_category"
        private const val STATE_SELECTED_CATEGORY = "state_selected_category"

        fun newInstance() = AddCategoryBudgetDialog()
    }
}
