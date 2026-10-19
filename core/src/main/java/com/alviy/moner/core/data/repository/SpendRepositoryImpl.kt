package com.alviy.moner.core.data.repository

import androidx.room.withTransaction
import com.alviy.moner.core.common.ext.fullMonthYearFormatter
import com.alviy.moner.core.common.ext.toFullMonthYear
import com.alviy.moner.core.data.local.MonerDatabase
import com.alviy.moner.core.data.local.dao.CategoryBudgetDao
import com.alviy.moner.core.data.local.dao.CategorySummaryDao
import com.alviy.moner.core.data.local.dao.ExpenseCategoryDao
import com.alviy.moner.core.data.local.dao.ExpenseSummaryDao
import com.alviy.moner.core.data.local.dao.ProfileDao
import com.alviy.moner.core.data.local.dao.TransactionDao
import com.alviy.moner.core.data.local.entity.CategorySummaryEntity
import com.alviy.moner.core.data.local.entity.ExpenseCategoryEntity
import com.alviy.moner.core.data.local.entity.ExpenseSummaryEntity
import com.alviy.moner.core.data.local.entity.TransactionEntity
import com.alviy.moner.core.data.mapper.toExpenseItem
import com.alviy.moner.core.data.mapper.toListExpenseItem
import com.alviy.moner.core.data.mapper.toSpendSummary
import com.alviy.moner.core.domain.model.Profile
import com.alviy.moner.core.domain.model.SpendSummary
import com.alviy.moner.core.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val database: MonerDatabase,
    private val profileDao: ProfileDao,
    private val expenseCategoryDao: ExpenseCategoryDao,
    private val transactionDao: TransactionDao,
    private val expenseSummaryDao: ExpenseSummaryDao,
    private val categorySummaryDao: CategorySummaryDao,
    private val categoryBudgetDao: CategoryBudgetDao
) : ExpenseRepository {

    override fun getProfile(): Flow<Profile> =
        profileDao.getProfile().map {
            Profile(it?.name.orEmpty())
        }

    override fun getCategories(monthYear: String) =
        expenseCategoryDao.getCategories(monthYear).map { list -> list.map { it.toExpenseItem() } }

    override fun getCategoryNamesForMonth(monthYear: String): Flow<List<String>> =
        expenseCategoryDao.getCategoryNamesForMonth(monthYear)

    override suspend fun insertCategory(
        name: String,
        monthYear: String?
    ) {
        expenseCategoryDao.insertCategory(
            ExpenseCategoryEntity(
                categoryName = name,
                dateCreated = LocalDateTime.now().toString(),
                monthYear = monthYear ?: LocalDateTime.now().toFullMonthYear()
            )
        )
    }

    override suspend fun deleteCategoryById(categoryId: Int) =
        expenseCategoryDao.deleteCategoryById(categoryId)

    override fun getExpenseJournal(monthYear: String) =
        expenseCategoryDao.getExpenseJournal(monthYear).map { result -> result.toListExpenseItem() }

    override fun getSpendSummary(monthYear: String): Flow<SpendSummary> {
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(WeekFields.of(Locale.getDefault()).firstDayOfWeek))
        return combine(
            expenseSummaryDao.getExpenseSummary(monthYear),
            transactionDao.getSpendBetween(today.startOfDayIso(), today.plusDays(1).startOfDayIso()),
            transactionDao.getSpendBetween(weekStart.startOfDayIso(), weekStart.plusWeeks(1).startOfDayIso())
        ) { summary, spendToday, spendWeek ->
            summary.toSpendSummary(totalSpendToday = spendToday, totalSpendWeek = spendWeek)
        }
    }

    // Same format as stored transaction dates (LocalDateTime.toString()), so range bounds compare correctly.
    private fun LocalDate.startOfDayIso(): String = atStartOfDay().toString()

    override fun getMonthYears(): Flow<List<String>> =
        expenseCategoryDao.getMonthYears()
            .map { result ->
                result.sortedByDescending {
                    YearMonth.parse(it, fullMonthYearFormatter())
                }
            }

    override suspend fun addExpense(
        categoryName: String,
        date: String,
        desc: String,
        source: String,
        amount: BigDecimal,
        note: String?
    ) = database.withTransaction {
        val monthYear = LocalDateTime.parse(date).toFullMonthYear()

        // ensure category exists
        val categoryId = expenseCategoryDao.insertCategoryIfNotExist(
            ExpenseCategoryEntity(
                categoryName = categoryName,
                dateCreated = date,
                monthYear = monthYear
            )
        )

        // insert transaction
        transactionDao.insertTransaction(
            TransactionEntity(
                categoryId = categoryId,
                date = date,
                desc = desc,
                source = source,
                amount = amount,
                note = note
            )
        )

        // update category summary
        val updatedCategory = categorySummaryDao.updateCategorySummary(
            monthYear,
            categoryId,
            amount,
            date
        )

        if (updatedCategory == 0) {
            categorySummaryDao.insertCategorySummary(
                CategorySummaryEntity(
                    monthYear = monthYear,
                    categoryId = categoryId,
                    totalExpense = amount,
                    latestTrxDate = date
                )
            )
        }

        // update expense summary
        val updatedSummary = expenseSummaryDao.updateExpenseSummary(monthYear, amount)

        if (updatedSummary == 0) {
            expenseSummaryDao.insertExpenseSummary(
                ExpenseSummaryEntity(monthYear = monthYear, totalExpense = amount)
            )
        }
    }

    override suspend fun clearAllTable() = database.withTransaction {
        transactionDao.clearTransactions()
        expenseCategoryDao.clearCategories()
        profileDao.clearProfiles()
        expenseSummaryDao.clearExpenseSummary()
        categorySummaryDao.clearCategorySummary()
        categoryBudgetDao.clearCategoryBudgets()
    }
}
