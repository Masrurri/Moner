package com.alviy.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.alviy.core.database.entity.CategorySummaryEntity
import com.alviy.core.database.entity.ExpenseCategoryEntity
import com.alviy.core.database.entity.ExpenseCategoryWithTransactions
import com.alviy.core.database.entity.ExpenseSummaryEntity
import com.alviy.core.database.entity.ExpenseSummaryWithCategories
import com.alviy.core.database.entity.ProfileEntity
import com.alviy.core.database.entity.TransactionEntity
import com.alviy.core.utils.Utils.toFullMonthYear
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDateTime

@Dao
interface ExpenseDao {
    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: ExpenseCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExpenseSummary(summary: ExpenseSummaryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategorySummary(summary: CategorySummaryEntity)

    // SAFE CATEGORY INSERT
    @Transaction
    suspend fun insertCategoryIfNotExist(category: ExpenseCategoryEntity): Int {
        val id = insertCategory(category)

        return if (id == -1L) getCategoryId(category.categoryName, category.monthYear)
        else id.toInt()
    }

    // QUERY
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

    @Transaction
    @Query("SELECT * FROM profile LIMIT 1")
    fun getProfile(): Flow<ProfileEntity>

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

    @Transaction
    @Query(
        """
        SELECT *
        FROM expense_summary
        WHERE monthYear = :monthYear
        LIMIT 1
        """
    )
    fun getExpenseSummary(
        monthYear: String
    ): Flow<ExpenseSummaryEntity?>

    @Query(
        """
        SELECT *
        FROM category_summary
        WHERE monthYear = :monthYear
        ORDER BY totalExpense DESC
        """
    )
    fun getCategorySummaries(
        monthYear: String
    ): Flow<List<CategorySummaryEntity>>


    // UPDATE
    @Query(
        """
        UPDATE expense_summary
        SET totalExpense = totalExpense + :amount,
            latestExpense = :amount,
            latestTransactionDate = :date
        WHERE monthYear = :monthYear
        """
    )
    suspend fun updateExpenseSummary(
        monthYear: String,
        amount: BigDecimal,
        date: String
    ): Int

    @Query(
        """
        UPDATE category_summary
        SET totalExpense = totalExpense + :amount,
            latestTrxDate = :date
        WHERE monthYear = :monthYear
        AND categoryId = :categoryId
        """
    )
    suspend fun updateCategorySummary(
        monthYear: String,
        categoryId: Int,
        amount: BigDecimal,
        date: String
    ): Int

    // DELETE
    @Query("DELETE FROM expense_transaction WHERE transactionId = :id")
    suspend fun deleteTransaction(id: Int)

    @Query("DELETE FROM expense_category WHERE categoryId = :categoryId")
    suspend fun deleteCategoryById(categoryId: Int)

    @Query("DELETE FROM expense_transaction")
    suspend fun clearTransactions()

    @Query("DELETE FROM expense_category")
    suspend fun clearCategories()

    @Query("DELETE FROM profile")
    suspend fun clearProfiles()

    @Query("DELETE FROM expense_summary")
    suspend fun clearExpenseSummary()

    @Query("DELETE FROM category_summary")
    suspend fun clearCategorySummary()

    @Transaction
    suspend fun clearAll() {
        clearTransactions()
        clearCategories()
        clearProfiles()
        clearExpenseSummary()
        clearCategorySummary()
    }

    // MAIN BUSINESS TRANSACTION
    @Transaction
    suspend fun addExpense(
        categoryName: String,
        date: String,
        desc: String,
        source: String,
        amount: BigDecimal
    ) {
        val monthYear = LocalDateTime.parse(date).toFullMonthYear()

        //ensure category exists
        val categoryId = insertCategoryIfNotExist(
            ExpenseCategoryEntity(
                categoryName = categoryName,
                dateCreated = date,
                monthYear = monthYear
            )
        )

        //insert transaction
        insertTransaction(
            TransactionEntity(
                categoryId = categoryId,
                date = date,
                desc = desc,
                source = source,
                amount = amount
            )
        )

        //update category summary
        val updatedCategory = updateCategorySummary(
            monthYear,
            categoryId,
            amount,
            date
        )

        if (updatedCategory == 0) {
            insertCategorySummary(
                CategorySummaryEntity(
                    monthYear = monthYear,
                    categoryId = categoryId,
                    totalExpense = amount,
                    latestTrxDate = date
                )
            )
        }

        //update expense summary
        val updatedSummary = updateExpenseSummary(
            monthYear,
            amount,
            date
        )

        if (updatedSummary == 0) {
            insertExpenseSummary(
                ExpenseSummaryEntity(
                    monthYear = monthYear,
                    totalExpense = amount,
                    latestExpense = amount,
                    latestTransactionDate = date
                )
            )
        }
    }
}