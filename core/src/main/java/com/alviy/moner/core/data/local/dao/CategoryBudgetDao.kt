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

    @Query("SELECT * FROM category_budget WHERE monthYear = :monthYear ORDER BY categoryName")
    fun getCategoryBudgets(monthYear: String): Flow<List<CategoryBudgetEntity>>

    @Query("DELETE FROM category_budget WHERE categoryName = :categoryName AND monthYear = :monthYear")
    suspend fun deleteCategoryBudget(categoryName: String, monthYear: String)

    @Query("DELETE FROM category_budget")
    suspend fun clearCategoryBudgets()
}
