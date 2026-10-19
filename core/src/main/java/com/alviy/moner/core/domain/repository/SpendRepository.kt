package com.alviy.moner.core.domain.repository

import com.alviy.moner.core.domain.model.ExpenseItem
import com.alviy.moner.core.domain.model.SpendSummary
import com.alviy.moner.core.domain.model.Profile
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface ExpenseRepository {

    fun getProfile(): Flow<Profile>

    fun getCategories(monthYear: String): Flow<List<ExpenseItem>>

    fun getExpenseJournal(monthYear: String): Flow<List<ExpenseItem>>

    /** Month total for [monthYear], plus live totals for today and the current week. */
    fun getSpendSummary(monthYear: String): Flow<SpendSummary>

    fun getMonthYears(): Flow<List<String>>

    fun getCategoryNamesForMonth(monthYear: String): Flow<List<String>>

    suspend fun addExpense(
        categoryName: String,
        date: String,
        desc: String,
        source: String,
        amount: BigDecimal,
        note: String? = null
    )

    suspend fun insertCategory(
        name: String,
        monthYear: String? = null
    )

    suspend fun deleteCategoryById(categoryId: Int)

    suspend fun clearAllTable()
}