package com.alviy.moner.feature.journal.viewmodel

import androidx.lifecycle.viewModelScope
import com.alviy.moner.core.common.base.BaseViewModel
import com.alviy.moner.core.common.mergeWithDefaultCategories
import com.alviy.moner.core.domain.model.ExpenseItem
import com.alviy.moner.core.domain.model.ExpenseSummary
import com.alviy.moner.core.domain.model.Profile
import com.alviy.moner.core.domain.repository.ExpenseRepository
import com.alviy.moner.feature.journal.ui.model.JournalRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class JournalViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : BaseViewModel() {

    private val _selectedMonthYear = MutableStateFlow<String?>(null)
    val selectedMonthYear: StateFlow<String?> = _selectedMonthYear.asStateFlow()

    val monthYears: StateFlow<List<String>> = expenseRepository.getMonthYears()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<Profile> = expenseRepository.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Profile())

    val availableCategories: StateFlow<List<String>> = _selectedMonthYear
        .flatMapLatest { monthYear ->
            if (monthYear == null) flowOf(emptyList()) else expenseRepository.getCategoryNamesForMonth(monthYear)
        }
        .map { mergeWithDefaultCategories(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, mergeWithDefaultCategories(emptyList()))

    private val _selectedCategoryFilter = MutableStateFlow(ALL_CATEGORIES)
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val currentMonthExpenseJournal: StateFlow<List<ExpenseItem>> = _selectedMonthYear
        .flatMapLatest { monthYear ->
            if (monthYear == null) flowOf(emptyList()) else expenseRepository.getExpenseJournal(monthYear)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryFilterOptions: StateFlow<List<String>> = currentMonthExpenseJournal
        .map { items -> listOf(ALL_CATEGORIES) + items.map { it.categoryName }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(ALL_CATEGORIES))

    val expenseSummary: StateFlow<ExpenseSummary> = _selectedMonthYear
        .flatMapLatest { monthYear ->
            if (monthYear == null) flowOf(ExpenseSummary()) else expenseRepository.getExpenseSummary(monthYear)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExpenseSummary())

    private val _monthOverMonthDeltaPercent = MutableStateFlow<Double?>(null)
    val monthOverMonthDeltaPercent: StateFlow<Double?> = _monthOverMonthDeltaPercent.asStateFlow()

    val journalRows: StateFlow<List<JournalRow>> = combine(
        currentMonthExpenseJournal,
        _selectedCategoryFilter
    ) { items, filter ->
        buildRows(items, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            monthYears.collect { months ->
                if (_selectedMonthYear.value == null && months.isNotEmpty()) {
                    selectMonthYear(months.first())
                }
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
            today -> "TODAY"
            today.minusDays(1) -> "YESTERDAY"
            else -> date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())).uppercase()
        }
    }

    companion object {
        const val ALL_CATEGORIES = "All"
    }
}
