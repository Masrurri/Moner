package com.alviy.moner.feature.journal.viewmodel

import com.alviy.moner.api.gmail.data.repository.GmailRepository
import com.alviy.moner.api.gmail.model.ReceiptTransaction
import com.alviy.moner.core.common.base.BaseViewModel
import com.alviy.moner.core.common.entity.ErrorCodes
import com.alviy.moner.core.common.entity.Result
import com.alviy.moner.core.common.mergeWithDefaultCategories
import com.alviy.moner.core.domain.model.ExpenseItem
import com.alviy.moner.core.domain.model.ExpenseSummary
import com.alviy.moner.core.domain.repository.ExpenseRepository
import com.alviy.moner.core.network.GoogleAuthTokenHolder
import com.alviy.moner.feature.journal.ui.model.JournalRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _monthYears = MutableStateFlow<List<String>>(emptyList())
    val monthYears: StateFlow<List<String>> = _monthYears.asStateFlow()

    private val _selectedMonthYear = MutableStateFlow<String?>(null)
    val selectedMonthYear: StateFlow<String?> = _selectedMonthYear.asStateFlow()

    private val _expenseSummary = MutableStateFlow(ExpenseSummary())
    val expenseSummary: StateFlow<ExpenseSummary> = _expenseSummary.asStateFlow()

    private val _journalRows = MutableStateFlow<List<JournalRow>>(emptyList())
    val journalRows: StateFlow<List<JournalRow>> = _journalRows.asStateFlow()

    // Categories offered by the add-spending dialog.
    private val _availableCategories = MutableStateFlow(mergeWithDefaultCategories(emptyList()))
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()

    // null = nothing requested yet; the fragment ignores it, so no callback fires until a fetch starts.
    private val _receipts = MutableStateFlow<Result<List<ReceiptTransaction>>?>(null)
    val receipts = _receipts.asStateFlow()

    init {
        expenseRepository.getMonthYears().collectFlow(_monthYears) { months ->
            if (_selectedMonthYear.value == null && months.isNotEmpty()) selectMonthYear(months.first())
        }
    }

    fun selectMonthYear(monthYear: String) {
        _selectedMonthYear.value = monthYear
        // Same targets on every call, so the previous month's DAO observers are replaced.
        expenseRepository.getExpenseSummary(monthYear).collectFlow(_expenseSummary)
        expenseRepository.getExpenseJournal(monthYear).map(::buildRows).collectFlow(_journalRows)
        expenseRepository.getCategoryNamesForMonth(monthYear)
            .map { mergeWithDefaultCategories(it) }
            .collectFlow(_availableCategories)
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

    private fun buildRows(items: List<ExpenseItem>): List<JournalRow> {
        val transactions = items
            .flatMap { item ->
                item.transactions.orEmpty().map { txn -> txn.copy(categoryName = item.categoryName) }
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
}
