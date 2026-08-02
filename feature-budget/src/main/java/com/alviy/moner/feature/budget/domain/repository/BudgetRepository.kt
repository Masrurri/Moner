package com.alviy.moner.feature.budget.domain.repository

import com.alviy.moner.feature.budget.domain.model.BudgetPeriod
import com.alviy.moner.feature.budget.domain.model.BudgetSummary
import com.alviy.moner.feature.budget.domain.model.CategoryBudgetProgress
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface BudgetRepository {

    fun getCategoryBudgetsWithSpend(monthYear: String): Flow<List<CategoryBudgetProgress>>

    fun getBudgetSummary(monthYear: String): Flow<BudgetSummary>

    suspend fun upsertCategoryBudget(
        monthYear: String,
        categoryName: String,
        monthlyLimit: BigDecimal,
        period: BudgetPeriod,
        notifyAt80Percent: Boolean
    )

    suspend fun deleteCategoryBudget(monthYear: String, categoryName: String)
}
