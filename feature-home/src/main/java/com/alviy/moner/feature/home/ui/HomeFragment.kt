package com.alviy.moner.feature.home.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.alviy.moner.core.common.base.BaseFragment
import com.alviy.moner.core.common.ext.getColor
import com.alviy.moner.core.common.ext.setStatusBarColor
import com.alviy.moner.core.common.ext.showDialog
import com.alviy.moner.core.common.ext.toIDR
import com.alviy.moner.core.designsystem.AddSpendDialog
import com.alviy.moner.core.designsystem.AmountFieldDialog
import com.alviy.moner.core.designsystem.TransactionListDialog
import com.alviy.moner.core.designsystem.WheelDialog
import com.alviy.moner.core.domain.model.SpendSummary
import com.alviy.moner.feature.home.R
import com.alviy.moner.feature.home.databinding.FragmentHomeBinding
import com.alviy.moner.feature.home.viewmodel.HomeViewModel
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val viewModel: HomeViewModel by viewModels()
    private val transactionAdapter = RecentTransactionAdapter()

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

    override fun initBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun initView() = with(binding) {
        observeNavigation(viewModel)
        setStatusBarColor(getColor(R.color.white_grey))
        rvTransactions.adapter = transactionAdapter
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        btnSelectedMonth.setOnClickListener { showSelectMonthDialog() }
        btnSpend.setOnClickListener { showAddSpendDialog() }
        btnFetch.setOnClickListener { appNav.navigateToReceiptGather() }
        secondStatContainer.setOnClickListener { showSpendLimitDialog() }
    }

    override fun observeData() {
        observeState(viewModel.selectedMonthYear) { binding.btnSelectedMonth.text = monthLabel(it) }
        observeState(viewModel.spendSummary, onChange = ::renderSummary)
        observeState(viewModel.recentTransactions) { rows ->
            transactionAdapter.submitList(rows)
            binding.tvEmptyState.isVisible = rows.isEmpty()
            binding.rvTransactions.isVisible = rows.isNotEmpty()
        }
        observeReceipts()
    }

    private fun renderSummary(summary: SpendSummary) = with(binding) {
        tvTotalAmount.text = summary.totalSpend.toIDR()
        tvSpendTodayValue.text = summary.totalSpendToday.toIDR()
        tvSpendWeekValue.text = summary.totalSpendWeek.toIDR()

        val limit = summary.spendLimit
        val remaining = summary.remainingLimit
        lpv.progress = summary.limitProgress
        tvLimitTotal.isVisible = limit != null
        tvLimitTotal.text = limit?.let { getString(R.string.home_limit_of, it.toIDR()) }
        tvLimitValue.text = when {
            remaining == null -> getString(R.string.home_limit_not_set)
            remaining < BigDecimal.ZERO -> getString(R.string.home_limit_over, remaining.negate().toIDR())
            else -> remaining.toIDR()
        }
    }

    private fun showSelectMonthDialog() {
        val months = viewModel.monthYears
        val labels = months.map(::monthLabel)
        val currentIndex = months.indexOf(viewModel.selectedMonthYear.value).coerceAtLeast(0)
        // The wheel returns the picked label; map it back to its month by position.
        WheelDialog.newInstance(labels, currentIndex) { label ->
            months.getOrNull(labels.indexOf(label))?.let(viewModel::selectMonthYear)
        }.show(parentFragmentManager, "wheelDialog")
    }

    private fun monthLabel(monthYear: String): String =
        if (monthYear == viewModel.currentMonthYear) getString(R.string.home_select_time_default) else monthYear

    private fun showSpendLimitDialog() {
        AmountFieldDialog.newInstance(
            title = getString(R.string.home_set_limit_title),
            buttonText = getString(R.string.home_set_limit_button),
            initialAmount = viewModel.spendSummary.value.spendLimit,
            onSubmit = viewModel::setSpendLimit
        ).show(childFragmentManager, "spendLimitDialog")
    }

    private fun showAddSpendDialog() {
        AddSpendDialog.newInstance().apply {
            onSave = { input ->
                viewModel.addSpend(
                    categoryName = input.categoryName,
                    date = input.date,
                    title = input.title,
                    source = input.source,
                    amount = input.amount,
                    note = input.note
                )
            }
        }.showDialog(childFragmentManager, "addSpendDialog")
    }

    // ---------------------------------------------------------------- Gmail (kept for the receipt import)

    private fun observeReceipts() {
        observeStateFlow(
            flow = viewModel.receipts,
            onLoading = { showReceipts(listOf(receiptMessage("Loading"))) },
            onError = { error ->
                showReceipts(
                    listOf(receiptMessage("Error Fetch", error.code.toString(), error.title.toString(), error.message.toString()))
                )
            }
        ) { receipts ->
            showReceipts(
                receipts.map { r ->
                    TransactionListDialog.Companion.TransactionReceipt(
                        r.id, r.channel, r.title, r.destination,
                        r.amount.toPlainString(),
                        listOfNotNull(r.date, r.time).joinToString(" "),
                        r.method?.label.orEmpty(),
                        r.note.orEmpty(),
                    )
                }
            )
        }
    }

    private fun receiptMessage(id: String, channel: String = "", title: String = "", destination: String = "") =
        TransactionListDialog.Companion.TransactionReceipt(id, channel, title, destination, "", "", "", "")

    private fun showReceipts(receipts: List<TransactionListDialog.Companion.TransactionReceipt>) {
        TransactionListDialog.newInstance(receipts).showDialog(parentFragmentManager, "transactionDialog")
    }

    @Suppress("unused") // wired up again once the Gmail import entry point is back
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
