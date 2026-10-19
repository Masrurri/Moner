package com.alviy.moner.core.domain.repository

import com.alviy.moner.core.domain.model.SpendSummary
import com.alviy.moner.core.domain.model.TransactionItem
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface SpendRepository {

    /** Latest [limit] transactions of [monthYear], newest first. */
    fun getSpendHistory(monthYear: String, limit: Int = RECENT_SPEND_LIMIT): Flow<List<TransactionItem>>

    /** Month total, spend limit and live totals for today and the current week. */
    fun getSpendSummary(monthYear: String): Flow<SpendSummary>

    suspend fun setSpendLimit(monthYear: String, amount: BigDecimal)

    /** Months that already have categories, newest first. */
    fun getMonthYears(): Flow<List<String>>

    fun getCategoryNamesForMonth(monthYear: String): Flow<List<String>>

    suspend fun insertCategory(name: String, monthYear: String? = null)

    suspend fun addSpend(
        categoryName: String,
        date: String,
        desc: String,
        source: String,
        amount: BigDecimal,
        note: String? = null
    )

    suspend fun clearAllTable()

    companion object {
        const val RECENT_SPEND_LIMIT = 20
    }
}
