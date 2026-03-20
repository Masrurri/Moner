package com.alviy.feature.expend.journal.viewmodel

import androidx.lifecycle.viewModelScope
import com.alviy.core.base.BaseViewModel
import com.alviy.core.model.CategorySummaryData
import com.alviy.core.model.ExpenseItem
import com.alviy.core.model.ExpenseSummary
import com.alviy.core.model.Profile
import com.alviy.core.model.TransactionItem
import com.alviy.core.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpendJournalViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
): BaseViewModel() {

    var selectedMonthYear = ""
    var listCategory: List<String> = emptyList()
    private val _profile = MutableStateFlow(Profile())
    val profile: StateFlow<Profile> = _profile
    private val _monthYearListData = MutableStateFlow(emptyList<String>())
    val monthYearListData: StateFlow<List<String>> = _monthYearListData
    private val _expenseJournalData = MutableStateFlow(emptyList<ExpenseItem>())
    val expenseJournalData: StateFlow<List<ExpenseItem>> = _expenseJournalData
    private val _categoriesData = MutableStateFlow(emptyList<ExpenseItem>())
    val categoriesData: StateFlow<List<ExpenseItem>> = _categoriesData

    private val _expenseSummaries = MutableStateFlow(ExpenseSummary())
    val expenseSummaries: StateFlow<ExpenseSummary> = _expenseSummaries

    fun getExpenseSummary(monthYear: String) {
        viewModelScope.launch {
            expenseRepository.getExpenseSummary(monthYear).collect {
                _expenseSummaries.value = it
            }
        }
    }

    fun getExpenseJournal(monthYear: String) {
        viewModelScope.launch {
            expenseRepository.getExpenseJournal(monthYear).collect {
                _expenseJournalData.value = it
            }
        }
    }

    fun getDistinctMonthYears() {
        viewModelScope.launch {
            expenseRepository.getMonthYears().collect {
                _monthYearListData.value = it
            }
        }
    }

    fun getCategories(monthYear: String) {
        viewModelScope.launch {
            expenseRepository.getCategories(monthYear).collect {
                _categoriesData.value = it
            }
        }
    }

    fun getProfile() {
        viewModelScope.launch {
            expenseRepository.getProfile().collect {
                _profile.value = it
            }
        }
    }

    fun addCategory(name: String) = viewModelScope.launch { expenseRepository.insertCategory(name) }

    fun addTransaction(transaction: TransactionItem) = viewModelScope.launch {
        expenseRepository.addExpense(
            transaction.categoryName,
            transaction.date,
            transaction.desc,
            transaction.source,
            transaction.amount,
            )
    }
}