package com.alviy.moner.core.data.repository

import androidx.room.withTransaction
import com.alviy.moner.core.common.ext.fullMonthYearFormatter
import com.alviy.moner.core.common.ext.toFullMonthYear
import com.alviy.moner.core.data.local.MonerDatabase
import com.alviy.moner.core.data.local.dao.CategoryBudgetDao
import com.alviy.moner.core.data.local.dao.CategorySummaryDao
import com.alviy.moner.core.data.local.dao.ProfileDao
import com.alviy.moner.core.data.local.dao.ReceiptDao
import com.alviy.moner.core.data.local.dao.SpendCategoryDao
import com.alviy.moner.core.data.local.dao.SpendLimitDao
import com.alviy.moner.core.data.local.dao.SpendSummaryDao
import com.alviy.moner.core.data.local.dao.TransactionDao
import com.alviy.moner.core.data.local.entity.CategorySummaryEntity
import com.alviy.moner.core.data.local.entity.SpendCategoryEntity
import com.alviy.moner.core.data.local.entity.SpendLimitEntity
import com.alviy.moner.core.data.local.entity.SpendSummaryEntity
import com.alviy.moner.core.data.local.entity.TransactionEntity
import com.alviy.moner.core.data.mapper.toSpendSummary
import com.alviy.moner.core.data.mapper.toTransactionItem
import com.alviy.moner.core.domain.model.SpendSummary
import com.alviy.moner.core.domain.model.TransactionItem
import com.alviy.moner.core.domain.repository.SpendRepository
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

class SpendRepositoryImpl @Inject constructor(
    private val database: MonerDatabase,
    private val profileDao: ProfileDao,
    private val spendCategoryDao: SpendCategoryDao,
    private val transactionDao: TransactionDao,
    private val spendSummaryDao: SpendSummaryDao,
    private val spendLimitDao: SpendLimitDao,
    private val categorySummaryDao: CategorySummaryDao,
    private val categoryBudgetDao: CategoryBudgetDao,
    private val receiptDao: ReceiptDao
) : SpendRepository {

    override fun getSpendHistory(monthYear: String, limit: Int): Flow<List<TransactionItem>> =
        transactionDao.getSpendHistory(monthYear, limit).map { entries -> entries.map { it.toTransactionItem() } }

    override fun getSpendSummary(monthYear: String): Flow<SpendSummary> {
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(WeekFields.of(Locale.getDefault()).firstDayOfWeek))
        return combine(
            spendSummaryDao.getSpendSummary(monthYear),
            transactionDao.getSpendBetween(today.startOfDayIso(), today.plusDays(1).startOfDayIso()),
            transactionDao.getSpendBetween(weekStart.startOfDayIso(), weekStart.plusWeeks(1).startOfDayIso()),
            spendLimitDao.getSpendLimit(monthYear)
        ) { summary, spendToday, spendWeek, limit ->
            summary.toSpendSummary(monthYear, spendToday, spendWeek, limit)
        }
    }

    override suspend fun setSpendLimit(monthYear: String, amount: BigDecimal) =
        spendLimitDao.upsertSpendLimit(SpendLimitEntity(monthYear = monthYear, amount = amount))

    override fun getMonthYears(): Flow<List<String>> =
        spendCategoryDao.getMonthYears().map { months ->
            months.sortedByDescending { YearMonth.parse(it, fullMonthYearFormatter()) }
        }

    override suspend fun addSpend(
        categoryName: String,
        date: String,
        desc: String,
        source: String,
        amount: BigDecimal,
        note: String?,
        receiptId: String?
    ) = database.withTransaction {
        val monthYear = LocalDateTime.parse(date).toFullMonthYear()

        val categoryId = spendCategoryDao.insertCategoryIfNotExist(
            SpendCategoryEntity(categoryName = categoryName, dateCreated = date, monthYear = monthYear)
        )

        transactionDao.insertTransaction(
            TransactionEntity(
                categoryId = categoryId,
                date = date,
                desc = desc,
                source = source,
                amount = amount,
                note = note,
                receiptId = receiptId
            )
        )
        receiptId?.let { receiptDao.markRecorded(it) }

        // Keep the per-category and per-month running totals in sync (update, or insert the first row).
        if (categorySummaryDao.updateCategorySummary(monthYear, categoryId, amount, date) == 0) {
            categorySummaryDao.insertCategorySummary(
                CategorySummaryEntity(
                    monthYear = monthYear,
                    categoryId = categoryId,
                    totalSpend = amount,
                    latestTrxDate = date
                )
            )
        }
        if (spendSummaryDao.updateSpendSummary(monthYear, amount) == 0) {
            spendSummaryDao.insertSpendSummary(SpendSummaryEntity(monthYear = monthYear, totalSpend = amount))
        }
    }

    override suspend fun clearAllTable() = database.withTransaction {
        transactionDao.clearTransactions()
        spendCategoryDao.clearCategories()
        profileDao.clearProfiles()
        spendSummaryDao.clearSpendSummary()
        spendLimitDao.clearSpendLimits()
        categorySummaryDao.clearCategorySummary()
        categoryBudgetDao.clearCategoryBudgets()
        receiptDao.clearReceipts()
    }

    // Same format as stored transaction dates (LocalDateTime.toString()), so range bounds compare correctly.
    private fun LocalDate.startOfDayIso(): String = atStartOfDay().toString()
}
