package com.alviy.moner.feature.home.viewmodel

import com.alviy.moner.core.common.base.BaseViewModel
import com.alviy.moner.core.common.entity.ErrorCodes
import com.alviy.moner.core.common.entity.Result
import com.alviy.moner.core.common.ext.fullMonthYearFormatter
import com.alviy.moner.core.domain.model.SpendSummary
import com.alviy.moner.core.domain.repository.SpendRepository
import com.alviy.moner.core.network.GoogleAuthTokenHolder
import com.alviy.moner.feature.home.ui.model.RecentTransactionRow
import com.alviy.moner.feature.home.ui.model.toRecentTransactionRows
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val spendRepository: SpendRepository
) : BaseViewModel() {

    /** "MMMM yyyy" of the current month, the format categories are stored with. */
    val currentMonthYear: String = YearMonth.now().format(fullMonthYearFormatter())

    // January of this year up to the current month; future months are never offered.
    val monthYears: List<String> = YearMonth.now().let { now ->
        (1..now.monthValue).map { month -> now.withMonth(month).format(fullMonthYearFormatter()) }
    }

    private val _selectedMonthYear = MutableStateFlow(currentMonthYear)
    val selectedMonthYear: StateFlow<String> = _selectedMonthYear.asStateFlow()

    private val _spendSummary = MutableStateFlow(SpendSummary())
    val spendSummary: StateFlow<SpendSummary> = _spendSummary.asStateFlow()

    private val _recentTransactions = MutableStateFlow<List<RecentTransactionRow>>(emptyList())
    val recentTransactions: StateFlow<List<RecentTransactionRow>> = _recentTransactions.asStateFlow()

    init {
        selectMonthYear(_selectedMonthYear.value)
    }

    fun selectMonthYear(monthYear: String) {
        _selectedMonthYear.value = monthYear
        // Same targets on every call, so the previous month's observers are replaced.
        spendRepository.getSpendSummary(monthYear).collectFlow(_spendSummary)
        spendRepository.getSpendHistory(monthYear)
            .map { it.toRecentTransactionRows() }
            .collectFlow(_recentTransactions)
    }

    fun setSpendLimit(amount: BigDecimal) = launchSafely {
        spendRepository.setSpendLimit(_selectedMonthYear.value, amount)
    }

    fun addSpend(
        categoryName: String,
        date: LocalDateTime,
        title: String,
        source: String,
        amount: BigDecimal,
        note: String?
    ) = launchSafely {
        spendRepository.addSpend(
            categoryName = categoryName,
            date = date.toString(),
            desc = title,
            source = source,
            amount = amount,
            note = note
        )
    }
}
