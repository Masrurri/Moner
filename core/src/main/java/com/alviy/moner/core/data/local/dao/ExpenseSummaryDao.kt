package com.alviy.moner.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.alviy.moner.core.data.local.entity.ExpenseSummaryEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface ExpenseSummaryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExpenseSummary(summary: ExpenseSummaryEntity)

    @Transaction
    @Query(
        """
        SELECT *
        FROM expense_summary
        WHERE monthYear = :monthYear
        LIMIT 1
        """
    )
    fun getExpenseSummary(
        monthYear: String
    ): Flow<ExpenseSummaryEntity?>

    @Query(
        """
        SELECT *
        FROM expense_summary
        WHERE monthYear = :monthYear
        LIMIT 1
        """
    )
    suspend fun getExpenseSummaryOnce(monthYear: String): ExpenseSummaryEntity?

    @Query(
        """
        UPDATE expense_summary
        SET totalExpense = totalExpense + :amount
        WHERE monthYear = :monthYear
        """
    )
    suspend fun updateExpenseSummary(
        monthYear: String,
        amount: BigDecimal
    ): Int

    @Query("DELETE FROM expense_summary")
    suspend fun clearExpenseSummary()
}
