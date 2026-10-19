package com.alviy.moner.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.alviy.moner.core.data.local.entity.SpendCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpendCategoryDao {
    // Only used by insertCategoryIfNotExist; categories are created implicitly when a spend is added.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: SpendCategoryEntity): Long

    /** Inserts the category for its month if missing and returns its id either way. */
    @Transaction
    suspend fun insertCategoryIfNotExist(category: SpendCategoryEntity): Int {
        val id = insertCategory(category)
        return if (id == -1L) getCategoryId(category.categoryName, category.monthYear) else id.toInt()
    }

    @Query("SELECT categoryId FROM expense_category WHERE categoryName = :name AND monthYear = :monthYear LIMIT 1")
    suspend fun getCategoryId(name: String, monthYear: String): Int

    @Query("SELECT monthYear FROM expense_category GROUP BY monthYear ORDER BY monthYear DESC")
    fun getMonthYears(): Flow<List<String>>

    @Query("DELETE FROM expense_category")
    suspend fun clearCategories()
}
