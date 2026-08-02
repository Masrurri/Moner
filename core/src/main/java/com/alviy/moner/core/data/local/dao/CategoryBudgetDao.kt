package com.alviy.moner.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alviy.moner.core.data.local.entity.CategoryBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryBudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategoryBudget(entity: CategoryBudgetEntity)

    @Query("SELECT * FROM category_budget ORDER BY categoryName")
    fun getCategoryBudgets(): Flow<List<CategoryBudgetEntity>>

    @Query("DELETE FROM category_budget WHERE categoryName = :categoryName")
    suspend fun deleteCategoryBudget(categoryName: String)

    @Query("DELETE FROM category_budget")
    suspend fun clearCategoryBudgets()
}
