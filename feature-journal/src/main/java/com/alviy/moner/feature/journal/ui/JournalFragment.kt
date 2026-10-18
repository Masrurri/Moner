package com.alviy.moner.feature.journal.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alviy.moner.core.common.base.BaseFragment
import com.alviy.moner.core.common.ext.getColor
import com.alviy.moner.core.common.ext.setStatusBarColor
import com.alviy.moner.core.common.ext.toIDR
import com.alviy.moner.core.designsystem.AddExpenseDialog
import com.alviy.moner.core.designsystem.TextFieldDialog
import com.alviy.moner.core.designsystem.WheelDialog
import com.alviy.moner.feature.journal.R
import com.alviy.moner.feature.journal.databinding.FragmentJournalBinding
import com.alviy.moner.feature.journal.viewmodel.JournalViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class JournalFragment : BaseFragment<FragmentJournalBinding>() {
    private val viewModel: JournalViewModel by viewModels()
    private var categoryAdapter: CategoryAdapter? = null
    private var transactionAdapter: RecentTransactionAdapter? = null

    override val injectedFragment = this

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentJournalBinding.inflate(inflater, container, false)

    override fun initView() {
        setupRvCategory()
        setupRvTransactions()

        binding.btnSelectedMonth.setOnClickListener { showDialogSelectTime() }
        binding.btnSpend.setOnClickListener { showAddExpenseDialog() }
        setStatusBarColor(binding.getColor(com.alviy.moner.core.R.color.white_grey))
    }

    private fun setupRvCategory() {
        categoryAdapter = CategoryAdapter { category ->
            viewModel.selectCategoryFilter(category)
        }
    }

    private fun setupRvTransactions() {
        transactionAdapter = RecentTransactionAdapter()
        binding.rvTransactions.adapter = transactionAdapter
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showDialogSelectTime() {
        val months = viewModel.monthYears.value
        if (months.isEmpty()) return
        val currentIndex = months.indexOf(viewModel.selectedMonthYear.value).coerceAtLeast(0)
        WheelDialog.newInstance(months, currentIndex) { selected ->
            viewModel.selectMonthYear(selected)
        }.show(parentFragmentManager, "wheelDialog")
    }

    private fun showAddExpenseDialog() {
        AddExpenseDialog.newInstance(viewModel.availableCategories.value).apply {
            onNewCategoryRequested = { onCreated ->
                TextFieldDialog.newInstance { name ->
                    viewModel.addCategory(name)
                    onCreated(name)
                }.show(childFragmentManager, "newCategoryDialog")
            }
            onSave = { input ->
                viewModel.addTransaction(
                    categoryName = input.categoryName,
                    date = input.date,
                    title = input.title,
                    source = input.source,
                    amount = input.amount,
                    note = input.note
                )
            }
        }.show(childFragmentManager, "addExpenseDialog")
    }

    override fun observeData() {
        observeMonthYears()
        observeGreeting()
        observeTotalExpense()
        observeCategoryFilters()
        observeTransactions()
    }

    private fun observeMonthYears() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.selectedMonthYear.collect { selected ->
                binding.btnSelectedMonth.text = selected
                    ?: getString(R.string.journal_select_time_default)
            }
        }
    }

    private fun observeGreeting() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.profile.collect { profile ->
                val name = profile.name.ifBlank {
                    getString(R.string.journal_greeting_default_name)
                }
                binding.tvGreeting.text =
                    getString(R.string.journal_greeting_format, name)
            }
        }
    }

    private fun observeTotalExpense() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.expenseSummary.collect { summary ->
                    binding.tvTotalAmount.text = summary.totalExpense.toIDR()
                }
            }
            launch {
                viewModel.monthOverMonthDeltaPercent.collect { delta ->
                    binding.tvDelta.apply {
                        if (delta == null) {
                            isVisible = false
                        } else {
                            isVisible = true
                            val sign = if (delta >= 0) "+" else "-"
                            text = getString(
                                R.string.journal_delta_format,
                                sign,
                                abs(delta)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeCategoryFilters() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.categoryFilterOptions.collect { options ->
                    categoryAdapter?.submitList(options)
                }
            }
            launch {
                viewModel.selectedCategoryFilter.collect { selected ->
                    categoryAdapter?.setSelected(selected)
                }
            }
        }
    }

    private fun observeTransactions() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.journalRows.collect { rows ->
                transactionAdapter?.submitList(rows)
                binding.tvEmptyState.isVisible = rows.isEmpty()
                binding.rvTransactions.isVisible = rows.isNotEmpty()
            }
        }
    }
}
