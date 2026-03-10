package com.alviy.feature.setting.viewmodel

import androidx.lifecycle.viewModelScope
import com.alviy.core.base.BaseViewModel
import com.alviy.core.model.ExpenseItem
import com.alviy.core.model.Profile
import com.alviy.core.model.TransactionItem
import com.alviy.core.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
): BaseViewModel() {
    val mockTransactions = listOf(
        TransactionItem(
            categoryName = "Makan",
            date = "2026-03-10T16:42:18.347",
            desc = "Nasi Padang",
            source = "Qris",
            amount = 20000.toBigDecimal()
        ),
        TransactionItem(
            categoryName = "Makan",
            date = "2026-03-10T16:42:18.347",
            desc = "Mie Goreng",
            source = "Qris",
            amount = 20000.toBigDecimal()
        ),
        TransactionItem(
            categoryName = "Makan",
            date = "2026-03-10T16:42:18.347",
            desc = "Seafood",
            source = "Qris",
            amount = 20000.toBigDecimal()
        ),
        TransactionItem(
            categoryName = "Transport",
            date = "2026-03-10T16:42:18.347",
            desc = "KRL",
            source = "Qris",
            amount = 20000.toBigDecimal()
        ),
        TransactionItem(
            categoryName = "Transport",
            date = "2026-03-10T16:42:18.347",
            desc = "Ojol",
            source = "Qris",
            amount = 20000.toBigDecimal()
        )
    )

    val categories = listOf(
        "Makan" to "March 2026",
        "Transport" to "February 2026",
        "Makan" to "February 2026",
        "Health" to "February 2026",
        "Health"  to "March 2026",
        "Transport"  to "March 2026",
        "Makan" to "January 2026",
        "Health" to "January 2026",
        "Transport" to "January 2026",
        "Holiday" to "December 2025"
    )

    fun generateTemplateCategory() = viewModelScope.launch {
        categories.forEach { expenseRepository.insertCategory(it.first, it.second) }
    }

    fun clearAllTable() = viewModelScope.launch { expenseRepository.clearAllTable() }
}