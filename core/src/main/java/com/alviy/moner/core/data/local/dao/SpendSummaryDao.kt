package com.alviy.moner.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alviy.moner.core.data.local.entity.SpendSummaryEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface SpendSummaryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSpendSummary(summary: SpendSummaryEntity)

    @Query("SELECT * FROM expense_summary WHERE monthYear = :monthYear LIMIT 1")
    fun getSpendSummary(monthYear: String): Flow<SpendSummaryEntity?>

    /** Adds [amount] to the month total; returns 0 when the month has no row yet. */
    @Query("UPDATE expense_summary SET totalExpense = totalExpense + :amount WHERE monthYear = :monthYear")
    suspend fun updateSpendSummary(monthYear: String, amount: BigDecimal): Int

    @Query("DELETE FROM expense_summary")
    suspend fun clearSpendSummary()
}
