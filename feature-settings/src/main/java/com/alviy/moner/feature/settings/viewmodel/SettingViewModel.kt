package com.alviy.moner.feature.settings.viewmodel

import com.alviy.moner.core.common.AppPreferences
import com.alviy.moner.core.common.base.BaseViewModel
import com.alviy.moner.core.domain.model.TransactionItem
import com.alviy.moner.core.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    val appPreferences: AppPreferences
) : BaseViewModel() {

    private val sampleTransactions = listOf(
        TransactionItem(
            categoryName = "Food",
            date = "2026-03-10T16:42:18.347",
            desc = "Nasi Padang",
            source = "QRIS",
            amount = 45000.toBigDecimal()
        ),
        TransactionItem(
            categoryName = "Transport",
            date = "2026-03-10T09:15:00.000",
            desc = "Gojek Ride",
            source = "E-Wallet",
            amount = 22000.toBigDecimal()
        )
    )

    private val sampleCategories = listOf(
        "Food" to "March 2026",
        "Transport" to "March 2026",
        "Shopping" to "March 2026"
    )

    fun generateSampleData() = launchSafely {
        sampleCategories.forEach { expenseRepository.insertCategory(it.first, it.second) }
        sampleTransactions.forEach {
            expenseRepository.addExpense(
                categoryName = it.categoryName,
                date = it.date,
                desc = it.desc,
                source = it.source,
                amount = it.amount
            )
        }
    }

    fun clearAllTable() = launchSafely { expenseRepository.clearAllTable() }
}
