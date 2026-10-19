package com.alviy.moner.feature.journal.viewmodel

import com.alviy.moner.api.gmail.data.repository.GmailRepository
import com.alviy.moner.api.gmail.model.ReceiptTransaction
import com.alviy.moner.core.common.base.BaseViewModel
import com.alviy.moner.core.common.entity.ErrorCodes
import com.alviy.moner.core.common.entity.Result
import com.alviy.moner.core.common.mergeWithDefaultCategories
import com.alviy.moner.core.domain.model.ExpenseItem
import com.alviy.moner.core.domain.model.ExpenseSummary
import com.alviy.moner.core.domain.model.Profile
import com.alviy.moner.core.domain.repository.ExpenseRepository
import com.alviy.moner.core.network.GoogleAuthTokenHolder
import com.alviy.moner.feature.journal.ui.model.JournalRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val gmailRepository: GmailRepository,
    private val googleAuthTokens: GoogleAuthTokenHolder
) : BaseViewModel() {

    private val _selectedMonthYear = MutableStateFlow<String?>(null)
    val selectedMonthYear: StateFlow<String?> = _selectedMonthYear.asStateFlow()

    private val _monthYears = MutableStateFlow<List<String>>(emptyList())
    val monthYears: StateFlow<List<String>> = _monthYears.asStateFlow()

    private val _profile = MutableStateFlow(Profile())
    val profile: StateFlow<Profile> = _profile.asStateFlow()

    private val _availableCategories = MutableStateFlow(mergeWithDefaultCategories(emptyList()))
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow(ALL_CATEGORIES)
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val currentMonthExpenseJournal = MutableStateFlow<List<ExpenseItem>>(emptyList())

    private val _categoryFilterOptions = MutableStateFlow(listOf(ALL_CATEGORIES))
    val categoryFilterOptions: StateFlow<List<String>> = _categoryFilterOptions.asStateFlow()

    private val _journalRows = MutableStateFlow<List<JournalRow>>(emptyList())
    val journalRows: StateFlow<List<JournalRow>> = _journalRows.asStateFlow()

    private val _expenseSummary = MutableStateFlow(ExpenseSummary())
    val expenseSummary: StateFlow<ExpenseSummary> = _expenseSummary.asStateFlow()

    private val _monthOverMonthDeltaPercent = MutableStateFlow<Double?>(null)
    val monthOverMonthDeltaPercent: StateFlow<Double?> = _monthOverMonthDeltaPercent.asStateFlow()

    // null = nothing requested yet; the fragment ignores it, so no callback fires until a fetch starts.
    private val _receipts = MutableStateFlow<Result<List<ReceiptTransaction>>?>(null)
    val receipts = _receipts.asStateFlow()

    init {
        expenseRepository.getProfile().collectFlow(_profile)
        expenseRepository.getMonthYears().collectFlow(_monthYears) { months ->
            if (_selectedMonthYear.value == null && months.isNotEmpty()) {
                selectMonthYear(months.first())
            }
        }
    }

    fun onAuthorized(accessToken: String) {
        googleAuthTokens.accessToken = accessToken
        load()
    }

    fun onAuthFailed(message: String?) {
        _receipts.value = Result.error(ErrorCodes.GENERAL, "Authorization failed", message ?: "Authorization failed")
    }

    fun load() {
        gmailRepository.getGmailReceipts().collectFlow(_receipts) { result ->
            if (result.code == ErrorCodes.UNAUTHORIZED) googleAuthTokens.accessToken = null // expired
        }
    }

    fun selectMonthYear(monthYear: String) {
        _selectedMonthYear.value = monthYear
        _selectedCategoryFilter.value = ALL_CATEGORIES

        // Same targets on every call: switching month replaces the previous month's DAO observers.
        expenseRepository.getExpenseJournal(monthYear).collectFlow(currentMonthExpenseJournal) { items ->
            _categoryFilterOptions.value = listOf(ALL_CATEGORIES) + items.map { it.categoryName }.distinct().sorted()
            refreshJournalRows()
        }
        expenseRepository.getCategoryNamesForMonth(monthYear)
            .map { mergeWithDefaultCategories(it) }
            .collectFlow(_availableCategories)
        expenseRepository.getExpenseSummary(monthYear).collectFlow(_expenseSummary)

        recomputeMonthOverMonthDelta(monthYear)
    }

    fun selectCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
        refreshJournalRows()
    }

    private fun refreshJournalRows() {
        _journalRows.value = buildRows(currentMonthExpenseJournal.value, _selectedCategoryFilter.value)
    }

    fun addCategory(name: String) = launchSafely {
        expenseRepository.insertCategory(name)
    }

    fun addTransaction(
        categoryName: String,
        date: LocalDateTime,
        title: String,
        source: String,
        amount: BigDecimal,
        note: String?
    ) = launchSafely {
        expenseRepository.addExpense(
            categoryName = categoryName,
            date = date.toString(),
            desc = title,
            source = source,
            amount = amount,
            note = note
        )
    }

    private fun recomputeMonthOverMonthDelta(currentMonthYear: String) = launchSafely {
        val months = monthYears.value.ifEmpty { expenseRepository.getMonthYears().first() }
        val previousMonthYear = months.getOrNull(months.indexOf(currentMonthYear) + 1)
        if (previousMonthYear == null) {
            _monthOverMonthDeltaPercent.value = null
            return@launchSafely
        }

        val currentTotal = expenseRepository.getExpenseSummary(currentMonthYear).first().totalExpense
            ?: BigDecimal.ZERO
        val previousTotal = expenseRepository.getExpenseSummary(previousMonthYear).first().totalExpense
            ?: BigDecimal.ZERO

        _monthOverMonthDeltaPercent.value = if (previousTotal > BigDecimal.ZERO) {
            (currentTotal - previousTotal)
                .divide(previousTotal, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .toDouble()
        } else {
            null
        }
    }

    private fun buildRows(items: List<ExpenseItem>, filter: String): List<JournalRow> {
        val transactions = items
            .filter { filter == ALL_CATEGORIES || it.categoryName == filter }
            .flatMap { item ->
                (item.transactions ?: emptyList()).map { txn -> txn.copy(categoryName = item.categoryName) }
            }
            .sortedByDescending { it.date }

        val rows = mutableListOf<JournalRow>()
        var lastLabel: String? = null
        transactions.forEach { txn ->
            val label = dayLabelFor(txn.date)
            if (label != lastLabel) {
                rows.add(JournalRow.DateHeader(label))
                lastLabel = label
            }
            rows.add(JournalRow.Transaction(txn))
        }
        return rows
    }

    private fun dayLabelFor(dateIso: String): String {
        val date = runCatching { LocalDateTime.parse(dateIso).toLocalDate() }.getOrNull() ?: return ""
        val today = LocalDate.now()
        return when (date) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()))
        }
    }

    companion object {
        const val ALL_CATEGORIES = "All"
    }
}
