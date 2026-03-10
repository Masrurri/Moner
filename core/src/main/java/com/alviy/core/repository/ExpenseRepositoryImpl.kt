package com.alviy.core.repository

import androidx.room.Transaction
import com.alviy.core.database.ExpenseDao
import com.alviy.core.database.entity.CategorySummariesEntity
import com.alviy.core.database.entity.ExpenseCategoryEntity
import com.alviy.core.database.entity.ExpenseSummaryEntity
import com.alviy.core.database.entity.ProfileEntity
import com.alviy.core.database.entity.TransactionEntity
import com.alviy.core.model.CategorySummaryData
import com.alviy.core.model.ExpenseSummaries
import com.alviy.core.model.ExpenseSummary
import com.alviy.core.model.Profile
import com.alviy.core.model.TransactionItem
import com.alviy.core.utils.Utils.fullMonthYearFormatter
import com.alviy.core.utils.Utils.toCategorySummaryData
import com.alviy.core.utils.Utils.toDateFullMonthYear
import com.alviy.core.utils.Utils.toExpenseItem
import com.alviy.core.utils.Utils.toExpenseSummary
import com.alviy.core.utils.Utils.toFullMonthYear
import com.alviy.core.utils.Utils.toListExpenseItem
import com.alviy.core.utils.Utils.toLocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override suspend fun insertExpenseSummary(summary: ExpenseSummary) {
        dao.insertExpenseSummary(
            ExpenseSummaryEntity(
                monthYear = summary.monthYear.orEmpty(),
                totalExpense = summary.totalExpense ?: BigDecimal.ZERO,
                latestExpense = summary.latestExpense ?: BigDecimal.ZERO,
                latestTransactionDate = summary.latestTransactionDate.orEmpty()
            )
        )
    }

    override suspend fun insertCategorySummary(summary: CategorySummaryData) {
        dao.insertCategorySummaries(
            CategorySummariesEntity(
                monthYear = summary.monthYear.orEmpty(),
                categoryId = summary.categoryId ?: 0,
                categoryName = summary.categoryName.orEmpty(),
                totalExpense = summary.totalExpense ?: BigDecimal.ZERO,
                latestTrxDate = summary.latestTrxDate.orEmpty()
            )
        )
    }

    override suspend fun updateExpenseSummary(monthYear: String, amount: BigDecimal, latestTrxDate: String) {
        dao.updateExpenseSummary(monthYear, amount, latestTrxDate)
    }

    override suspend fun updateCategorySummary(
        monthYear: String,
        categoryId: Int,
        amount: BigDecimal,
        date: String
    ) {
        dao.updateCategorySummary(monthYear, categoryId, amount, date)
    }

    override fun getExpenseSummaries(monthYear: String) =
        dao.getExpenseSummaries(monthYear).map { result ->
            ExpenseSummaries(
                expenseSummary = result?.summary.toExpenseSummary(),
                categorySummaries = result?.categorySummaries?.map { it.toCategorySummaryData() }.orEmpty()
            )
        }

    override fun getExpenseJournal(monthYear: String) = dao.getExpenseJournal(monthYear).map { result ->
        result.toListExpenseItem()
    }

    override fun getProfile(): Flow<Profile> = dao.getProfile().map {
        Profile(it.name)
    }

    override fun getCategories(monthYear: String) = dao.getCategories(monthYear).map { result ->
        result.map { it.toExpenseItem() }
    }

    override fun getDistinctMonthYears(): Flow<List<String>> = dao.getDistinctMonthYears().map { result ->
        result.sortedByDescending { YearMonth.parse(it, fullMonthYearFormatter()) }
    }

    override suspend fun insertCategory(name: String, monthYear: String?) {
        dao.insertCategory(
            ExpenseCategoryEntity(
                categoryName = name,
                totalExpense = BigDecimal.ZERO,
                dateCreated = LocalDateTime.now().toString(),
                monthYear = monthYear?:LocalDateTime.now().toFullMonthYear()
            )
        )
    }

    override suspend fun insertTransaction(transaction: TransactionItem) = dao.insertTransaction(
        TransactionEntity(
            categoryId = transaction.categoryId,
            categoryName = transaction.categoryName,
            date = transaction.date,
            desc = transaction.desc,
            source = transaction.source,
            amount = transaction.amount
        )
    )

    @Transaction
    override suspend fun insertTransactionWithSummaries(transaction: TransactionItem) {
        insertTransaction(transaction)
        val monthYear = LocalDateTime.parse(transaction.date).toFullMonthYear()
        // Insert monthly summary if missing
        insertExpenseSummary(
            ExpenseSummary(
                monthYear = monthYear,
                totalExpense = BigDecimal.ZERO,
                latestExpense = BigDecimal.ZERO,
                latestTransactionDate = transaction.date
            )
        )

        // Update monthly summary totals
        updateExpenseSummary(monthYear, transaction.amount, transaction.date)

        // Insert category summary if missing
        insertCategorySummary(
            CategorySummaryData(
                monthYear = monthYear,
                categoryId = transaction.categoryId,
                categoryName = transaction.categoryName,
                totalExpense = BigDecimal.ZERO,
                latestTrxDate = transaction.date
            )
        )

        // Update category totals
        updateCategorySummary(
            monthYear = monthYear,
            categoryId = transaction.categoryId,
            amount = transaction.amount,
            date = transaction.date
        )
    }

    override suspend fun deleteCategoryById(categoryId: String) = dao.deleteCategoryById(categoryId)

    override suspend fun clearAllTable() = dao.clearAll()
}