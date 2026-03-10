package com.alviy.feature.expend.journal.ui

import android.graphics.Color
import android.view.View.VISIBLE
import android.widget.EdgeEffect
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.alviy.app.feature.expend.journal.databinding.FragmentExpendJournalBinding
import com.alviy.core.base.BaseFragment
import com.alviy.core.designsystem.WheelDialog
import com.alviy.core.model.ExpenseItem
import com.alviy.core.model.TransactionItem
import com.alviy.core.utils.Utils.fullMonthYearFormatter
import com.alviy.feature.expend.journal.viewmodel.ExpendJournalViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
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

            //for Mock
            val monthYearString = binding?.btnSelectTime?.text
            val yearMonth = YearMonth.parse(monthYearString, fullMonthYearFormatter())
            val dateTime = yearMonth.atDay(1).atStartOfDay()
            viewModel.addTransaction(
                TransactionItem(id, name, dateTime.toString(), "Mock $name", "Mock", 20000.toBigDecimal())
            )
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
                    categoryAdapter?.setSelected(index)
                    binding?.rvCategory?.smoothScrollToPosition(index)
                }
            }
        })
    }

        private fun observeExpenseSummaries() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.expenseSummaries.collect {
                if (it.expenseSummary != null) {
                    binding?.tvTotalAmount?.text = it.expenseSummary?.totalExpense.toString()
                    binding?.tvLatestExpense?.text = "+${it.expenseSummary?.latestExpense}"
                }
            }
        }
    }

    private fun observeExpenseJournal() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.expenseJournalData.collect {
                if (it.isNotEmpty()) {
                    journalSectionAdapter?.submitList(it)
                    binding?.rvJournalSection?.isVisible = true
                }
            }
        }
    }

    private fun observeCategories() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.categoriesData.collect {
                if (it.isNotEmpty()) {
                    categoryAdapter?.resetSelectedPos()
                    categoryAdapter?.submitList(it.map { expenseItem -> expenseItem.categoryName })
                    binding?.rvCategory?.isVisible = categoryAdapter?.currentList.isNullOrEmpty().not()
                    viewModel.getExpenseSummaries(viewModel.selectedMonthYear)
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