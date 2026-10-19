package com.alviy.moner.feature.budget.viewmodel

import com.alviy.moner.core.common.base.BaseViewModel
import com.alviy.moner.core.common.defaults.ExpenseDefaults
import com.alviy.moner.core.common.ext.toFullMonthYear
import com.alviy.moner.core.domain.repository.ExpenseRepository
import com.alviy.moner.feature.budget.domain.model.BudgetPeriod
import com.alviy.moner.feature.budget.domain.model.BudgetSummary
import com.alviy.moner.feature.budget.domain.model.CategoryBudgetProgress
import com.alviy.moner.feature.budget.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

/** Shared by [com.alviy.moner.feature.budget.ui.BudgetFragment] and its Add Category Budget sheet. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository
) : BaseViewModel() {

    private val _selectedMonthYear = MutableStateFlow(LocalDateTime.now().toFullMonthYear())
    val selectedMonthYear: StateFlow<String> = _selectedMonthYear.asStateFlow()

    /** Read synchronously when the month picker opens, so it is kept hot. */
    val monthYears: StateFlow<List<String>> = expenseRepository.getMonthYears()
        .stateEagerly(emptyList())

    val categoryBudgets: StateFlow<List<CategoryBudgetProgress>> = _selectedMonthYear
        .flatMapLatest(budgetRepository::getCategoryBudgetsWithSpend)
        .stateWhileSubscribed(emptyList())

    val budgetSummary: StateFlow<BudgetSummary> = _selectedMonthYear
        .flatMapLatest(budgetRepository::getBudgetSummary)
        .stateWhileSubscribed(BudgetSummary())

    val availableCategories: StateFlow<List<String>> = _selectedMonthYear
        .flatMapLatest(expenseRepository::getCategoryNamesForMonth)
        .map(ExpenseDefaults::mergeWithCategories)
        .stateEagerly(ExpenseDefaults.mergeWithCategories(emptyList()))

    fun selectMonthYear(monthYear: String) {
        _selectedMonthYear.value = monthYear
    }

    fun addCategory(name: String) = launchSafely {
        expenseRepository.insertCategory(name)
    }

    fun saveCategoryBudget(
        categoryName: String,
        monthlyLimit: BigDecimal,
        period: BudgetPeriod,
        notifyAt80Percent: Boolean
    ) = launchSafely {
        budgetRepository.upsertCategoryBudget(
            _selectedMonthYear.value, categoryName, monthlyLimit, period, notifyAt80Percent
        )
    }

    fun deleteCategoryBudget(categoryName: String) = launchSafely {
        budgetRepository.deleteCategoryBudget(_selectedMonthYear.value, categoryName)
    }
}
