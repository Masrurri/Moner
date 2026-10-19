package com.alviy.moner.feature.settings.viewmodel

import com.alviy.moner.core.common.base.BaseViewModel
import com.alviy.moner.core.common.theme.ThemeManager
import com.alviy.moner.core.data.local.preferences.AppPreferences
import com.alviy.moner.core.domain.model.TransactionItem
import com.alviy.moner.core.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val preferences: AppPreferences,
    private val themeManager: ThemeManager,
) : BaseViewModel() {

    val isDarkMode: Boolean get() = themeManager.isDarkMode
    val isNotificationsEnabled: Boolean get() = preferences.isNotificationsEnabled

    fun setDarkMode(enabled: Boolean) = themeManager.setDarkMode(enabled)

    fun setNotificationsEnabled(enabled: Boolean) {
        preferences.isNotificationsEnabled = enabled
    }

    /** Debug-only: fills the database with a few rows so screens can be checked quickly. */
    fun generateSampleData() = launchSafely {
        SampleData.categories.forEach { (name, monthYear) -> expenseRepository.insertCategory(name, monthYear) }
        SampleData.transactions.forEach {
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

    private object SampleData {
        private const val MONTH = "March 2026"

        val categories = listOf("Food" to MONTH, "Transport" to MONTH, "Shopping" to MONTH)

        val transactions = listOf(
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
    }
}
