package com.alviy.moner.feature.journal.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.alviy.moner.core.designsystem.TransactionListDialog
import com.alviy.moner.core.designsystem.WheelDialog
import com.alviy.moner.feature.journal.R
import com.alviy.moner.feature.journal.databinding.FragmentJournalBinding
import com.alviy.moner.feature.journal.viewmodel.JournalViewModel
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class JournalFragment : BaseFragment<FragmentJournalBinding>() {
    private val viewModel: JournalViewModel by viewModels()
    private var categoryAdapter: CategoryAdapter? = null
    private var transactionAdapter: RecentTransactionAdapter? = null

    private val authLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            try {
                val auth = Identity.getAuthorizationClient(requireActivity())
                    .getAuthorizationResultFromIntent(result.data)
                auth.accessToken?.let(viewModel::onAuthorized) ?: viewModel.onAuthFailed("No access token")
            } catch (e: Exception) {
                viewModel.onAuthFailed(e.message)
            }
        }

    override val injectedFragment = this

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentJournalBinding.inflate(inflater, container, false)

    override fun initView() = with(binding) {
        setStatusBarColor(getColor(R.color.white_grey))
        setupRvCategory()
        setupRvTransactions()
        btnSelectedMonth.setOnClickListener { showDialogSelectTime() }
        btnSpend.setOnClickListener { showAddExpenseDialog() }
        btnFetch.setOnClickListener { authorizeGmail() }
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
        observeStateFlow(
            flow = viewModel.receipts,
            onLoading = null,
            onError = {
                TransactionListDialog.newInstance(
                    listOf(TransactionListDialog.Companion.GmailReceipt("Error Fetch", it.code.toString(), it.title.toString(), it.message.toString(), "", "", ""))
                ).show(parentFragmentManager, "transactionDialog")
            }
        ) { it ->
            TransactionListDialog.newInstance(
                it.map { TransactionListDialog.Companion.GmailReceipt(it.id, it.merchant, it.subject, it.amount.toString(), it.currency.toString(), it.dateMillis.toString(), it.snippet) }
            ).show(parentFragmentManager, "transactionDialog")
            it.forEach { a -> Log.d("Receipt", a.toString()) }
        }
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

    private fun authorizeGmail() {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(GMAIL_READONLY)))
            .build()
        Identity.getAuthorizationClient(requireActivity()).authorize(request)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    authLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent!!.intentSender).build())
                } else {
                    result.accessToken?.let(viewModel::onAuthorized)
                }
            }
            .addOnFailureListener { viewModel.onAuthFailed(it.message) }
    }

    private companion object {
        const val GMAIL_READONLY = "https://www.googleapis.com/auth/gmail.readonly"
    }
}
