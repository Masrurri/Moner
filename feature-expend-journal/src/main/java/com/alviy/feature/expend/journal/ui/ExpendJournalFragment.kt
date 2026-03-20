package com.alviy.feature.expend.journal.ui

import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.alviy.app.feature.expend.journal.databinding.FragmentExpendJournalBinding
import com.alviy.core.base.BaseFragment
import com.alviy.core.designsystem.TextFieldDialog
import com.alviy.core.designsystem.AddJournalDialog
import com.alviy.core.designsystem.WheelDialog
import com.alviy.core.model.TransactionItem
import com.alviy.core.utils.Utils.toIDR
import com.alviy.feature.expend.journal.viewmodel.ExpendJournalViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.getValue

@AndroidEntryPoint
class ExpendJournalFragment : BaseFragment<FragmentExpendJournalBinding>(
    FragmentExpendJournalBinding::inflate
) {
    private val viewModel: ExpendJournalViewModel by viewModels()
    private var categoryAdapter: CategoryAdapter? = null
    private var journalSectionAdapter: JournalSectionAdapter? = null

    override val injectedFragment = this

    override fun initView() {
        setupRvCategory()
        setupRvJournalSection()
        viewModel.getDistinctMonthYears()
        binding?.btnSelectTime?.setOnClickListener {
            if (viewModel.listCategory.isNotEmpty()) showDialogSelectTime()
        }
        binding?.btnAddCategory?.setOnClickListener {
            showDialogAddCategory()
        }
    }

    private fun showDialogSelectTime() {
        WheelDialog.newInstance(
            viewModel.listCategory,
            viewModel.listCategory.indexOfFirst { it == binding?.btnSelectTime?.text }
        ) { selected ->
            viewModel.selectedMonthYear = selected
            binding?.btnSelectTime?.text = selected
            viewModel.getCategories(selected)
        }.show(parentFragmentManager, "wheelDialog")
    }

    private fun showDialogAddJournal(categoryName: String) {
        AddJournalDialog.newInstance(
            titleLabel = "$categoryName Journal",
            descLabel = "Description",
            amountLabel = "Amount",
            primaryLabel = "Input Journal",
            secondaryLabel = "Cancel"
        ).apply {
            onClickPrimaryBtn = { data ->
                viewModel.addTransaction(
                    TransactionItem( categoryName = categoryName, date = LocalDateTime.now().toString(), amount = data.third.toBigDecimal(), desc = data.first, source = data.second)
                )
                this.dismissAllowingStateLoss()
            }
        }.show(childFragmentManager, "dialogAddJournal")
    }

    private fun showDialogAddCategory() {
        TextFieldDialog.newInstance{ viewModel.addCategory(it) }.show(parentFragmentManager, "categoryDialog")
    }

    override fun observeData() {
        observeDistinctMonthYears()
        observeExpenseSummaries()
        observeExpenseJournal()
        observeCategories()
    }

    private fun setupRvCategory() {
        categoryAdapter = CategoryAdapter { category ->
            val index = journalSectionAdapter?.currentList?.indexOfFirst { section ->
                section.categoryName == category
            }
            index?.let { if (index != -1) binding?.rvJournalSection?.smoothScrollToPosition(index) }
            Toast.makeText(requireContext(), category, Toast.LENGTH_SHORT).show()
        }
        binding?.rvCategory?.adapter = categoryAdapter
        binding?.rvCategory?.layoutManager = LinearLayoutManager(
            requireContext(),
            HORIZONTAL,
            false
        )


    }

    private fun setupRvJournalSection() {
        val pagerSnapHelper = PagerSnapHelper()
        journalSectionAdapter = JournalSectionAdapter { id, name ->
            showDialogAddJournal(name)
        }
        binding?.rvJournalSection?.adapter = journalSectionAdapter
        binding?.rvJournalSection?.layoutManager = LinearLayoutManager(
            requireContext(),
            HORIZONTAL,
            false
        )
        binding?.rvJournalSection?.onFlingListener = null
        pagerSnapHelper.attachToRecyclerView(binding?.rvJournalSection)

        binding?.rvJournalSection?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val snapView = pagerSnapHelper.findSnapView(layoutManager) ?: return
                val position = layoutManager.getPosition(snapView)
                val categoryNameSection = journalSectionAdapter?.currentList?.getOrNull(position)?.categoryName
                if (categoryNameSection.isNullOrEmpty()) return
                val index = categoryAdapter?.currentList?.indexOfFirst { it == categoryNameSection } ?: return
                if (index != -1) {
                    categoryAdapter?.setSelected(categoryNameSection)
                    binding?.rvCategory?.smoothScrollToPosition(index)
                }
            }
        })
    }

        private fun observeExpenseSummaries() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.expenseSummaries.collect {
                binding?.tvTotalAmount?.text = it.totalExpense.toIDR()
                binding?.tvLatestExpense?.text = "+${it.latestExpense.toIDR()}"
            }
        }
    }

    private fun observeExpenseJournal() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.expenseJournalData.collect {
                if (it.isNotEmpty()) {
                    journalSectionAdapter?.submitList(it.sortedBy { it.categoryName })
                    binding?.rvJournalSection?.isVisible = true
                }
            }
        }
    }

    private fun observeCategories() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.categoriesData.collect {
                if (it.isNotEmpty()) {
                    val categoryList = it.map { expenseItem -> expenseItem.categoryName }.sorted()
                    categoryAdapter?.submitList(categoryList)
                    categoryAdapter?.setSelected(categoryList.first())
                    binding?.rvCategory?.isVisible = categoryAdapter?.currentList.isNullOrEmpty().not()
                    viewModel.getExpenseSummary(viewModel.selectedMonthYear)
                    viewModel.getExpenseJournal(viewModel.selectedMonthYear)
                }
            }
        }
    }

    private fun observeDistinctMonthYears() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.monthYearListData.collect {
                if (it.isNotEmpty()) {
                    viewModel.listCategory = it
                    binding?.btnSelectTime?.text = it.firstOrNull()
                    viewModel.selectedMonthYear = it.firstOrNull().toString()
                    viewModel.getCategories(it.firstOrNull().orEmpty())
                }
            }
        }
    }
}