package com.alviy.moner.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alviy.moner.core.data.local.entity.CategorySummaryEntity
import java.math.BigDecimal

@Dao
interface CategorySummaryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategorySummary(summary: CategorySummaryEntity)

    @Query(
        """
        UPDATE category_summary
        SET totalExpense = totalExpense + :amount,
            latestTrxDate = :date
        WHERE monthYear = :monthYear
        AND categoryId = :categoryId
        """
    )
    suspend fun updateCategorySummary(
        monthYear: String,
        categoryId: Int,
        amount: BigDecimal,
        date: String
    ): Int

    @Query(
        """
        SELECT cs.totalExpense
        FROM category_summary cs
        JOIN expense_category c ON cs.categoryId = c.categoryId
        WHERE c.categoryName = :categoryName
        AND cs.monthYear = :monthYear
        LIMIT 1
        """
    )
    suspend fun getSpendForCategoryInMonth(categoryName: String, monthYear: String): BigDecimal?

    @Query("DELETE FROM category_summary")
    suspend fun clearCategorySummary()
}
