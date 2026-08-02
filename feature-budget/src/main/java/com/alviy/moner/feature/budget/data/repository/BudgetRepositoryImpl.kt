package com.alviy.moner.feature.budget.data.repository

import com.alviy.moner.core.data.local.dao.CategoryBudgetDao
import com.alviy.moner.core.data.local.dao.CategorySummaryDao
import com.alviy.moner.core.data.local.dao.TransactionDao
import com.alviy.moner.core.data.local.entity.CategoryBudgetEntity
import com.alviy.moner.feature.budget.domain.model.BudgetPeriod
import com.alviy.moner.feature.budget.domain.model.BudgetSummary
import com.alviy.moner.feature.budget.domain.model.CategoryBudgetProgress
import com.alviy.moner.feature.budget.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val categoryBudgetDao: CategoryBudgetDao,
    private val categorySummaryDao: CategorySummaryDao,
    private val transactionDao: TransactionDao
) : BudgetRepository {

    override fun getCategoryBudgetsWithSpend(monthYear: String): Flow<List<CategoryBudgetProgress>> =
        categoryBudgetDao.getCategoryBudgets(monthYear).map { budgets ->
            budgets.map { budget ->
                val period = runCatching { BudgetPeriod.valueOf(budget.period) }
                    .getOrDefault(BudgetPeriod.MONTHLY)

                val spent = when (period) {
                    BudgetPeriod.MONTHLY ->
                        categorySummaryDao.getSpendForCategoryInMonth(budget.categoryName, monthYear)
                            ?: BigDecimal.ZERO

                    BudgetPeriod.WEEKLY -> {
                        val (start, end) = currentWeekRange()
                        transactionDao.getSpendForCategoryBetween(budget.categoryName, start, end)
                    }
                }

                CategoryBudgetProgress(
                    categoryName = budget.categoryName,
                    limit = budget.monthlyLimit,
                    spent = spent,
                    period = period,
                    notifyAt80Percent = budget.notifyAt80Percent
                )
            }
        }

    override fun getBudgetSummary(monthYear: String): Flow<BudgetSummary> =
        getCategoryBudgetsWithSpend(monthYear).map { progress ->
            BudgetSummary(
                totalLimit = progress.fold(BigDecimal.ZERO) { acc, item -> acc + item.limit },
                totalSpent = progress.fold(BigDecimal.ZERO) { acc, item -> acc + item.spent }
            )
        }

    override suspend fun upsertCategoryBudget(
        monthYear: String,
        categoryName: String,
        monthlyLimit: BigDecimal,
        period: BudgetPeriod,
        notifyAt80Percent: Boolean
    ) {
        categoryBudgetDao.upsertCategoryBudget(
            CategoryBudgetEntity(
                categoryName = categoryName,
                monthYear = monthYear,
                monthlyLimit = monthlyLimit,
                period = period.name,
                notifyAt80Percent = notifyAt80Percent,
                dateCreated = LocalDateTime.now().toString()
            )
        )
    }

    override suspend fun deleteCategoryBudget(monthYear: String, categoryName: String) =
        categoryBudgetDao.deleteCategoryBudget(categoryName, monthYear)

    private fun currentWeekRange(): Pair<String, String> {
        val now = LocalDateTime.now()
        val start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .toLocalDate().atStartOfDay()
        val end = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .toLocalDate().atTime(23, 59, 59)
        return start.toString() to end.toString()
    }
}
