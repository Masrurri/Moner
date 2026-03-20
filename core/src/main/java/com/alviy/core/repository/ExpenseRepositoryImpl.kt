package com.alviy.core.repository

import com.alviy.core.database.ExpenseDao
import com.alviy.core.database.entity.ExpenseCategoryEntity
import com.alviy.core.model.Profile
import com.alviy.core.utils.Utils.fullMonthYearFormatter
import com.alviy.core.utils.Utils.toExpenseItem
import com.alviy.core.utils.Utils.toExpenseSummary
import com.alviy.core.utils.Utils.toFullMonthYear
import com.alviy.core.utils.Utils.toListExpenseItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun getProfile(): Flow<Profile> =
        dao.getProfile().map {
            Profile(it.name)
        }

    override fun getCategories(monthYear: String) =
        dao.getCategories(monthYear).map { list -> list.map { it.toExpenseItem() } }

    override suspend fun insertCategory(
        name: String,
        monthYear: String?
    ) {
        dao.insertCategory(
            ExpenseCategoryEntity(
                categoryName = name,
                dateCreated = LocalDateTime.now().toString(),
                monthYear = monthYear ?: LocalDateTime.now().toFullMonthYear()
            )
        )
    }

    override suspend fun deleteCategoryById(categoryId: Int) =
        dao.deleteCategoryById(categoryId)

    override fun getExpenseJournal(monthYear: String) =
        dao.getExpenseJournal(monthYear).map { result -> result.toListExpenseItem() }

    override fun getExpenseSummary(monthYear: String) =
        dao.getExpenseSummary(monthYear).map { result -> result.toExpenseSummary() }

    override fun getMonthYears(): Flow<List<String>> =
        dao.getMonthYears()
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
        amount: BigDecimal
    ) {
        dao.addExpense(
            categoryName = categoryName,
            date = date,
            desc = desc,
            source = source,
            amount = amount
        )
    }

    override suspend fun clearAllTable() =
        dao.clearAll()
}