package com.alviy.core.repository

import com.alviy.core.database.entity.ExpenseSummaryEntity
import com.alviy.core.database.entity.ProfileEntity
import com.alviy.core.database.entity.TransactionEntity
import com.alviy.core.model.CategorySummaryData
import com.alviy.core.model.ExpenseItem
import com.alviy.core.model.ExpenseSummaries
import com.alviy.core.model.ExpenseSummary
import com.alviy.core.model.Profile
import com.alviy.core.model.TransactionItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.math.BigDecimal

interface ExpenseRepository {

    suspend fun updateExpenseSummary(monthYear: String, amount: BigDecimal, latestTrxDate: String)

    suspend fun updateCategorySummary(
        monthYear: String,
        categoryId: Int,
        amount: BigDecimal,
        date: String
    )
    fun getExpenseSummaries(monthYear: String): Flow<ExpenseSummaries>

    fun getExpenseJournal(monthYear: String): Flow<List<ExpenseItem>>

    fun getProfile(): Flow<Profile>

    fun getCategories(monthYear: String): Flow<List<ExpenseItem>>

    fun getDistinctMonthYears(): Flow<List<String>>

    suspend fun insertExpenseSummary(summary: ExpenseSummary)

    suspend fun insertCategorySummary(summary: CategorySummaryData)

    suspend fun insertCategory(name: String,monthYear: String? = null)

    suspend fun insertTransaction(transaction: TransactionItem)

    suspend fun insertTransactionWithSummaries(transaction: TransactionItem)

    suspend fun deleteCategoryById(categoryId: String)

    suspend fun clearAllTable()
}