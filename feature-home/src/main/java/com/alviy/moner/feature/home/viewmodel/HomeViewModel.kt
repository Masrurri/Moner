package com.alviy.moner.feature.home.viewmodel

import androidx.lifecycle.viewModelScope
import com.alviy.moner.api.gmail.domain.model.ReceiptTransaction
import com.alviy.moner.api.gmail.domain.repository.GmailRepository
import com.alviy.moner.core.common.base.BaseViewModel
import com.alviy.moner.core.common.defaults.ExpenseDefaults
import com.alviy.moner.core.common.ext.DatePatterns
import com.alviy.moner.core.common.ext.format
import com.alviy.moner.core.common.ext.isoToLocalDateOrNull
import com.alviy.moner.core.common.result.ErrorCodes
import com.alviy.moner.core.common.result.ErrorMessages
import com.alviy.moner.core.common.result.Result
import com.alviy.moner.core.common.ui.UiText
import com.alviy.moner.core.designsystem.dialog.NewExpenseInput
import com.alviy.moner.core.domain.model.ExpenseItem
import com.alviy.moner.core.domain.model.ExpenseSummary
import com.alviy.moner.core.domain.model.Profile
import com.alviy.moner.core.domain.repository.ExpenseRepository
import com.alviy.moner.core.network.GoogleAuthTokenHolder
import com.alviy.moner.feature.home.ui.model.HomeRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import javax.inject.Inject
import com.alviy.moner.core.R as CoreR

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val gmailRepository: GmailRepository,
    private val googleAuthTokens: GoogleAuthTokenHolder,
) : BaseViewModel() {

    // ------------------------------------------------------------------ month selection

    private val _selectedMonthYear = MutableStateFlow<String?>(null)
    val selectedMonthYear: StateFlow<String?> = _selectedMonthYear.asStateFlow()

    val monthYears: StateFlow<List<String>> = expenseRepository.getMonthYears()
        .stateWhileSubscribed(emptyList())

    // ------------------------------------------------------------------ header

    val profile: StateFlow<Profile> = expenseRepository.getProfile()
        .stateWhileSubscribed(Profile())

    val expenseSummary: StateFlow<ExpenseSummary> = _selectedMonthYear
        .flatMapLatestOrDefault(ExpenseSummary(), expenseRepository::getExpenseSummary)
        .stateWhileSubscribed(ExpenseSummary())

    private val _monthOverMonthDeltaPercent = MutableStateFlow<Double?>(null)

    /** Change vs the previous month in percent; null when there is nothing to compare with. */
    val monthOverMonthDeltaPercent: StateFlow<Double?> = _monthOverMonthDeltaPercent.asStateFlow()

    // ------------------------------------------------------------------ categories & transactions

    /** Read synchronously when the Add Expense sheet opens, so it is kept hot. */
    val availableCategories: StateFlow<List<String>> = _selectedMonthYear
        .flatMapLatestOrDefault(emptyList(), expenseRepository::getCategoryNamesForMonth)
        .map(ExpenseDefaults::mergeWithCategories)
        .stateEagerly(ExpenseDefaults.mergeWithCategories(emptyList()))

    private val _selectedCategoryFilter = MutableStateFlow(ALL_CATEGORIES)
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val currentMonthExpenseJournal: StateFlow<List<ExpenseItem>> = _selectedMonthYear
        .flatMapLatestOrDefault(emptyList(), expenseRepository::getExpenseJournal)
        .stateWhileSubscribed(emptyList())

    val categoryFilterOptions: StateFlow<List<String>> = currentMonthExpenseJournal
        .map { items -> listOf(ALL_CATEGORIES) + items.map { it.categoryName }.distinct().sorted() }
        .stateWhileSubscribed(listOf(ALL_CATEGORIES))

    val homeRows: StateFlow<List<HomeRow>> =
        combine(currentMonthExpenseJournal, _selectedCategoryFilter, ::buildRows)
            .stateWhileSubscribed(emptyList())

    // ------------------------------------------------------------------ Gmail receipts

    // null = nothing requested yet; the fragment ignores it, so no callback fires until a fetch starts.
    private val _receipts = MutableStateFlow<Result<List<ReceiptTransaction>>?>(null)
    val receipts: StateFlow<Result<List<ReceiptTransaction>>?> = _receipts.asStateFlow()

    init {
        // Open on the most recent month that has data.
        viewModelScope.launch {
            monthYears.collect { months ->
                if (_selectedMonthYear.value == null && months.isNotEmpty()) selectMonthYear(months.first())
            }
        }
    }

    fun selectMonthYear(monthYear: String) {
        _selectedMonthYear.value = monthYear
        _selectedCategoryFilter.value = ALL_CATEGORIES
        recomputeMonthOverMonthDelta(monthYear)
    }

    fun selectCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun addCategory(name: String) = launchSafely {
        expenseRepository.insertCategory(name)
    }

    fun addTransaction(input: NewExpenseInput) = launchSafely {
        expenseRepository.addExpense(
            categoryName = input.categoryName,
            date = input.date.toString(),
            desc = input.title,
            source = input.source,
            amount = input.amount,
            note = input.note,
        )
    }

    fun onAuthorized(accessToken: String) {
        googleAuthTokens.accessToken = accessToken
        loadReceipts()
    }

    fun onAuthFailed(message: String?) {
        _receipts.value = Result.error(
            code = ErrorCodes.GENERAL,
            title = ErrorMessages.AUTHORIZATION_FAILED,
            message = message ?: ErrorMessages.AUTHORIZATION_FAILED,
        )
    }

    fun loadReceipts() {
        _receipts.loadFrom(gmailRepository.getGmailReceipts()) { result ->
            if (result.code == ErrorCodes.UNAUTHORIZED) googleAuthTokens.accessToken = null // expired
        }
    }

    private fun recomputeMonthOverMonthDelta(currentMonthYear: String) = launchSafely {
        val months = monthYears.value.ifEmpty { expenseRepository.getMonthYears().first() }
        val previousMonthYear = months.getOrNull(months.indexOf(currentMonthYear) + 1)
        if (previousMonthYear == null) {
            _monthOverMonthDeltaPercent.value = null
            return@launchSafely
        }

        val currentTotal = totalFor(currentMonthYear)
        val previousTotal = totalFor(previousMonthYear)

        _monthOverMonthDeltaPercent.value = if (previousTotal > BigDecimal.ZERO) {
            (currentTotal - previousTotal)
                .divide(previousTotal, PERCENT_SCALE, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED)
                .toDouble()
        } else {
            null
        }
    }

    private suspend fun totalFor(monthYear: String): BigDecimal =
        expenseRepository.getExpenseSummary(monthYear).first().totalExpense ?: BigDecimal.ZERO

    private fun buildRows(items: List<ExpenseItem>, filter: String): List<HomeRow> {
        val transactions = items
            .filter { filter == ALL_CATEGORIES || it.categoryName == filter }
            .flatMap { item -> item.transactions.orEmpty().map { it.copy(categoryName = item.categoryName) } }
            .sortedByDescending { it.date }

        return buildList {
            var lastDate: LocalDate? = null
            var isFirst = true
            transactions.forEach { txn ->
                val date = txn.date.isoToLocalDateOrNull()
                if (isFirst || date != lastDate) {
                    add(HomeRow.DateHeader(dayLabelFor(date)))
                    lastDate = date
                    isFirst = false
                }
                add(HomeRow.Transaction(txn))
            }
        }
    }

    private fun dayLabelFor(date: LocalDate?): UiText {
        val today = LocalDate.now()
        return when (date) {
            null -> UiText.Dynamic("")
            today -> UiText.Resource(CoreR.string.common_today)
            today.minusDays(1) -> UiText.Resource(CoreR.string.common_yesterday)
            else -> UiText.Dynamic(date.format(DatePatterns.DAY_FULL_MONTH_YEAR))
        }
    }

    /** Switches to [load] for each selected month; emits [default] while no month is selected. */
    private fun <T> Flow<String?>.flatMapLatestOrDefault(default: T, load: (String) -> Flow<T>): Flow<T> =
        flatMapLatest { monthYear -> if (monthYear == null) flowOf(default) else load(monthYear) }

    companion object {
        /** Filter value meaning "no category filter". Shown as the first pill. */
        const val ALL_CATEGORIES = "All"

        private const val PERCENT_SCALE = 4
        private val ONE_HUNDRED = BigDecimal(100)
    }
}
