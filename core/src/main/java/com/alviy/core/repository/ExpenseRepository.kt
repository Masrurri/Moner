package com.alviy.core.repository

import com.alviy.core.model.ExpenseItem
import com.alviy.core.model.ExpenseSummary
import com.alviy.core.model.Profile
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface ExpenseRepository {

    fun getProfile(): Flow<Profile>

    fun getCategories(monthYear: String): Flow<List<ExpenseItem>>

    fun getExpenseJournal(monthYear: String): Flow<List<ExpenseItem>>

    fun getExpenseSummary(monthYear: String): Flow<ExpenseSummary>

    fun getMonthYears(): Flow<List<String>>

    suspend fun addExpense(
        categoryName: String,
        date: String,
        desc: String,
        source: String,
        amount: BigDecimal
    )

    suspend fun insertCategory(
        name: String,
        monthYear: String? = null
    )

    suspend fun deleteCategoryById(categoryId: Int)

    suspend fun clearAllTable()
}