package com.alviy.moner.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.alviy.moner.core.data.local.entity.ExpenseCategoryEntity
import com.alviy.moner.core.data.local.entity.ExpenseCategoryWithTransactions
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseCategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: ExpenseCategoryEntity): Long

    @Transaction
    suspend fun insertCategoryIfNotExist(category: ExpenseCategoryEntity): Int {
        val id = insertCategory(category)

        return if (id == -1L) getCategoryId(category.categoryName, category.monthYear)
        else id.toInt()
    }

    @Query(
        """
        SELECT categoryId
        FROM expense_category
        WHERE categoryName = :name
        AND monthYear = :monthYear
        LIMIT 1
        """
    )
    suspend fun getCategoryId(name: String, monthYear: String): Int

    @Query(
        """
        SELECT *
        FROM expense_category
        WHERE monthYear = :monthYear
        """
    )
    fun getCategories(monthYear: String): Flow<List<ExpenseCategoryEntity>>

    @Query(
        """
        SELECT DISTINCT categoryName
        FROM expense_category
        ORDER BY categoryName
        """
    )
    fun getDistinctCategoryNames(): Flow<List<String>>

    @Query(
        """
        SELECT monthYear
        FROM expense_category
        GROUP BY monthYear
        ORDER BY monthYear DESC
        """
    )
    fun getMonthYears(): Flow<List<String>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM expense_category
        WHERE monthYear = :monthYear
        ORDER BY dateCreated
        """
    )
    fun getExpenseJournal(
        monthYear: String
    ): Flow<List<ExpenseCategoryWithTransactions>>

    @Query("DELETE FROM expense_category WHERE categoryId = :categoryId")
    suspend fun deleteCategoryById(categoryId: Int)

    @Query("DELETE FROM expense_category")
    suspend fun clearCategories()
}
