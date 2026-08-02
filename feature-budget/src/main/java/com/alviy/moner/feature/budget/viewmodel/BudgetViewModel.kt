package com.alviy.moner.feature.budget.viewmodel

import androidx.lifecycle.viewModelScope
import com.alviy.moner.core.common.base.BaseViewModel
import com.alviy.moner.core.common.ext.toFullMonthYear
import com.alviy.moner.core.common.mergeWithDefaultCategories
import com.alviy.moner.core.domain.repository.ExpenseRepository
import com.alviy.moner.feature.budget.domain.model.BudgetPeriod
import com.alviy.moner.feature.budget.domain.model.BudgetSummary
import com.alviy.moner.feature.budget.domain.model.CategoryBudgetProgress
import com.alviy.moner.feature.budget.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository
) : BaseViewModel() {

    private val _selectedMonthYear = MutableStateFlow(LocalDateTime.now().toFullMonthYear())
    val selectedMonthYear: StateFlow<String> = _selectedMonthYear.asStateFlow()

    val monthYears: StateFlow<List<String>> = expenseRepository.getMonthYears()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val categoryBudgets: StateFlow<List<CategoryBudgetProgress>> = _selectedMonthYear
        .flatMapLatest { budgetRepository.getCategoryBudgetsWithSpend(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetSummary: StateFlow<BudgetSummary> = _selectedMonthYear
        .flatMapLatest { budgetRepository.getBudgetSummary(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetSummary())

    val availableCategories: StateFlow<List<String>> = _selectedMonthYear
        .flatMapLatest { monthYear -> expenseRepository.getCategoryNamesForMonth(monthYear) }
        .map { mergeWithDefaultCategories(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, mergeWithDefaultCategories(emptyList()))

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
