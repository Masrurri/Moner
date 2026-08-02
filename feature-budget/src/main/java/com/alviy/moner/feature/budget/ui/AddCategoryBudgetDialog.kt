package com.alviy.moner.feature.budget.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alviy.moner.core.designsystem.FullScreenSheetDialogFragment
import com.alviy.moner.core.designsystem.SourceExpenseAdapter
import com.alviy.moner.core.designsystem.TextFieldDialog
import com.alviy.moner.feature.budget.databinding.DialogAddCategoryBudgetBinding
import com.alviy.moner.feature.budget.domain.model.BudgetPeriod
import com.alviy.moner.feature.budget.viewmodel.BudgetViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AddCategoryBudgetDialog : FullScreenSheetDialogFragment() {

    private var _binding: DialogAddCategoryBudgetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetViewModel by activityViewModels()
    private lateinit var categoryAdapter: SourceExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddCategoryBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryAdapter = SourceExpenseAdapter { }
        binding.rvCategory.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategory.adapter = categoryAdapter

        viewModel.availableCategories
            .onEach { categoryAdapter.submitList(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.btnNewCategory.setOnClickListener {
            TextFieldDialog.newInstance { newCategory ->
                viewModel.addCategory(newCategory)
                categoryAdapter.selectedCategory = newCategory
            }.show(childFragmentManager, "new_category")
        }

        binding.btnSaveBudget.setOnClickListener {
            val category = categoryAdapter.selectedCategory
            if (category.isNullOrBlank()) return@setOnClickListener

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AddCategoryBudgetDialog()
    }
}
