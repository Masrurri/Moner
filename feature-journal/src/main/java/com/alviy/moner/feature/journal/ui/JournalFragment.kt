package com.alviy.moner.feature.journal.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alviy.moner.core.common.base.BaseFragment
import com.alviy.moner.core.common.ext.getColor
import com.alviy.moner.core.common.ext.navigateTo
import com.alviy.moner.core.common.ext.setStatusBarColor
import com.alviy.moner.core.common.ext.showDialog
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

@AndroidEntryPoint
class JournalFragment : BaseFragment<FragmentJournalBinding>() {
    private val viewModel: JournalViewModel by viewModels()
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
        observeNavigation(viewModel)
        setStatusBarColor(getColor(R.color.white_grey))
        setupRvTransactions()
        btnSelectedMonth.setOnClickListener { showDialogSelectTime() }
        btnSpend.setOnClickListener { showAddExpenseDialog() }
        btnFetch.setOnClickListener { appNav.navigateToReceiptGather() }
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
        }.showDialog(childFragmentManager, "addExpenseDialog")
    }

    override fun observeData() {
        observeState(viewModel.selectedMonthYear) { selected ->
            binding.btnSelectedMonth.text = selected ?: getString(R.string.journal_select_time_default)
        }
        observeState(viewModel.expenseSummary) { summary ->
            binding.tvTotalAmount.text = summary.totalExpense.toIDR()
        }
        observeState(viewModel.journalRows) { rows ->
            transactionAdapter?.submitList(rows)
            binding.tvEmptyState.isVisible = rows.isEmpty()
            binding.rvTransactions.isVisible = rows.isNotEmpty()
        }
        observeReceipts()
    }

    private fun observeReceipts() {
        observeStateFlow(
            flow = viewModel.receipts,
            onLoading = {
                TransactionListDialog.newInstance(
                    listOf(
                        TransactionListDialog.Companion.TransactionReceipt(
                            "Loading", "", "", "", "", "", "", ""
                        )
                    )
                ).showDialog(parentFragmentManager, "transactionDialog")
            },
            onError = { error ->
                TransactionListDialog.newInstance(
                    listOf(
                        TransactionListDialog.Companion.TransactionReceipt(
                            "Error Fetch", error.code.toString(), error.title.toString(), error.message.toString(), "", "", "", ""
                        )
                    )
                ).showDialog(parentFragmentManager, "transactionDialog")
            }
        ) { receipts ->
            TransactionListDialog.newInstance(
                receipts.map { r ->
                    TransactionListDialog.Companion.TransactionReceipt(
                        r.id, r.channel, r.title, r.destination,
                        r.amount.toPlainString(),
                        listOfNotNull(r.date, r.time).joinToString(" "),
                        r.method?.label.orEmpty(),
                        r.note.orEmpty(),
                    )
                }
            ).showDialog(parentFragmentManager, "transactionDialog")
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
