package com.alviy.moner.feature.home.ui

import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.alviy.moner.api.gmail.auth.GmailAuthorization
import com.alviy.moner.api.gmail.domain.model.ReceiptTransaction
import com.alviy.moner.core.common.base.BaseFragment
import com.alviy.moner.core.common.ext.getColor
import com.alviy.moner.core.common.ext.onDialogResult
import com.alviy.moner.core.common.ext.setStatusBarColor
import com.alviy.moner.core.common.ext.showDialog
import com.alviy.moner.core.common.ext.toIDR
import com.alviy.moner.core.common.result.Result
import com.alviy.moner.core.designsystem.adapter.PillAdapter
import com.alviy.moner.core.designsystem.dialog.AddExpenseDialog
import com.alviy.moner.core.designsystem.dialog.TransactionListDialog
import com.alviy.moner.core.designsystem.dialog.TransactionReceipt
import com.alviy.moner.core.designsystem.dialog.WheelDialog
import com.alviy.moner.feature.home.R
import com.alviy.moner.feature.home.databinding.FragmentHomeBinding
import com.alviy.moner.feature.home.viewmodel.HomeViewModel
import com.google.android.gms.auth.api.identity.Identity
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel: HomeViewModel by viewModels()

    /** Category filter pills. Kept in sync with the ViewModel; its RecyclerView is not in the layout yet. */
    private val categoryAdapter = PillAdapter(selectFirstByDefault = true) { viewModel.selectCategoryFilter(it) }
    private var transactionAdapter: HomeTransactionAdapter? = null

    private val authLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            try {
                val auth = Identity.getAuthorizationClient(requireActivity())
                    .getAuthorizationResultFromIntent(result.data)
                auth.accessToken?.let(viewModel::onAuthorized) ?: viewModel.onAuthFailed(NO_ACCESS_TOKEN)
            } catch (e: Exception) {
                viewModel.onAuthFailed(e.message)
            }
        }

    override fun initView() = with(binding) {
        observeNavigation(viewModel)
        setStatusBarColor(getColor(com.alviy.moner.core.R.color.color_fixed_surface_light))

        transactionAdapter = HomeTransactionAdapter()
        rvTransactions.adapter = transactionAdapter
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())

        btnSelectedMonth.setOnClickListener { showMonthPicker() }
        btnSpend.setOnClickListener { showAddExpenseSheet() }
        btnFetch.setOnClickListener { appNav.navigateToReceiptGather() }

        onDialogResult(REQUEST_MONTH) { result ->
            WheelDialog.selectedItem(result)?.let(viewModel::selectMonthYear)
        }
        onDialogResult(REQUEST_ADD_EXPENSE) { result ->
            when (val parsed = AddExpenseDialog.parseResult(result)) {
                is AddExpenseDialog.Result.CategoryCreated -> viewModel.addCategory(parsed.name)
                is AddExpenseDialog.Result.Saved -> viewModel.addTransaction(parsed.input)
                null -> Unit
            }
        }
    }

    override fun observeData() {
        collect(viewModel.selectedMonthYear) { selected ->
            binding.btnSelectedMonth.text = selected ?: getString(R.string.home_select_time_default)
        }
        collect(viewModel.profile) { profile ->
            val name = profile.name.ifBlank { getString(R.string.home_greeting_default_name) }
            binding.tvGreeting.text = getString(R.string.home_greeting_format, name)
        }
        collect(viewModel.expenseSummary) { summary ->
            binding.tvTotalAmount.text = summary.totalExpense.toIDR()
        }
        collect(viewModel.monthOverMonthDeltaPercent, ::renderDelta)
        collect(viewModel.categoryFilterOptions) { categoryAdapter.submitList(it) }
        collect(viewModel.selectedCategoryFilter) { categoryAdapter.selectedItem = it }
        collect(viewModel.homeRows) { rows ->
            transactionAdapter?.submitList(rows)
            binding.tvEmptyState.isVisible = rows.isEmpty()
            binding.rvTransactions.isVisible = rows.isNotEmpty()
        }
        observeStateFlow(
            flow = viewModel.receipts,
            onLoading = { showReceipts(listOf(LOADING_ROW)) },
            onError = { error -> showReceipts(listOf(error.toReceiptRow())) },
        ) { receipts ->
            showReceipts(receipts.map { it.toReceiptRow() })
        }
    }

    override fun onDestroyView() {
        transactionAdapter = null
        super.onDestroyView()
    }

    private fun renderDelta(delta: Double?) = with(binding.tvDelta) {
        isVisible = delta != null
        if (delta == null) return@with
        val sign = if (delta >= 0) SIGN_POSITIVE else SIGN_NEGATIVE
        text = getString(R.string.home_delta_format, sign, abs(delta))
    }

    private fun showMonthPicker() {
        val months = viewModel.monthYears.value
        if (months.isEmpty()) return
        val currentIndex = months.indexOf(viewModel.selectedMonthYear.value).coerceAtLeast(0)
        WheelDialog.newInstance(months, currentIndex, REQUEST_MONTH)
            .showDialog(childFragmentManager, WheelDialog.TAG)
    }

    private fun showAddExpenseSheet() {
        AddExpenseDialog.newInstance(viewModel.availableCategories.value, REQUEST_ADD_EXPENSE)
            .showDialog(childFragmentManager, AddExpenseDialog.TAG)
    }

    private fun showReceipts(rows: List<TransactionReceipt>) {
        TransactionListDialog.newInstance(rows).showDialog(childFragmentManager, TransactionListDialog.TAG)
    }

    /** Starts Gmail OAuth; on success the ViewModel loads receipts and they are shown in a sheet. */
    @Suppress("unused") // Entry point for the receipt fetch flow; not wired to a button yet.
    private fun authorizeGmail() {
        Identity.getAuthorizationClient(requireActivity())
            .authorize(GmailAuthorization.readOnlyRequest())
            .addOnSuccessListener { result ->
                val pendingIntent = result.pendingIntent
                if (result.hasResolution() && pendingIntent != null) {
                    authLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
                } else {
                    result.accessToken?.let(viewModel::onAuthorized)
                }
            }
            .addOnFailureListener { viewModel.onAuthFailed(it.message) }
    }

    private fun ReceiptTransaction.toReceiptRow() = TransactionReceipt(
        id = id,
        channel = channel,
        title = title,
        destination = destination,
        amount = amount.toPlainString(),
        dateTime = listOfNotNull(date, time).joinToString(" "),
        method = method?.label.orEmpty(),
        note = note.orEmpty(),
    )

    private fun Result<*>.toReceiptRow() = TransactionReceipt(
        id = ERROR_ROW_TITLE,
        channel = code.toString(),
        title = title.toString(),
        destination = message.toString(),
        amount = "",
        dateTime = "",
        method = "",
        note = "",
    )

    private companion object {
        const val REQUEST_MONTH = "home_request_month"
        const val REQUEST_ADD_EXPENSE = "home_request_add_expense"

        const val SIGN_POSITIVE = "+"
        const val SIGN_NEGATIVE = "-"
        const val NO_ACCESS_TOKEN = "No access token"

        // Debug-only receipt sheet rows (not user-facing copy).
        const val ERROR_ROW_TITLE = "Error Fetch"
        val LOADING_ROW = TransactionReceipt("Loading", "", "", "", "", "", "", "")
    }
}
