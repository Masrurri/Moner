package com.alviy.moner.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alviy.moner.core.data.local.entity.SpendHistoryEntry
import com.alviy.moner.core.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM expense_transaction WHERE transactionId = :id")
    suspend fun deleteTransaction(id: Int)

    @Query(
        """
        SELECT COALESCE(SUM(t.amount), 0)
        FROM expense_transaction t
        JOIN expense_category c ON t.categoryId = c.categoryId
        WHERE c.categoryName = :categoryName
        AND t.date BETWEEN :startDate AND :endDate
        """
    )
    suspend fun getSpendForCategoryBetween(
        categoryName: String,
        startDate: String,
        endDate: String
    ): java.math.BigDecimal

    /** Total spent in [startDate, endDate); dates are ISO LocalDateTime strings, so they compare lexically. */
    @Query(
        """
        SELECT COALESCE(SUM(amount), 0)
        FROM expense_transaction
        WHERE date >= :startDate AND date < :endDate
        """
    )
    fun getSpendBetween(startDate: String, endDate: String): Flow<java.math.BigDecimal>

    /** Latest [limit] transactions of [monthYear] with their category name, newest first. */
    @Query(
        """
        SELECT t.transactionId, t.categoryId, c.categoryName, t.date, t.desc, t.source, t.amount, t.note
        FROM expense_transaction t
        JOIN expense_category c ON t.categoryId = c.categoryId
        WHERE c.monthYear = :monthYear
        ORDER BY t.date DESC
        LIMIT :limit
        """
    )
    fun getSpendHistory(monthYear: String, limit: Int): Flow<List<SpendHistoryEntry>>

    @Query("DELETE FROM expense_transaction")
    suspend fun clearTransactions()
}
