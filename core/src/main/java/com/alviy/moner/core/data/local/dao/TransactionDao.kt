package com.alviy.moner.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alviy.moner.core.data.local.entity.TransactionEntity

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

    @Query("DELETE FROM expense_transaction")
    suspend fun clearTransactions()
}
