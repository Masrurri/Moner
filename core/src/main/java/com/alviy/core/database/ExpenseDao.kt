package com.alviy.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.alviy.core.database.entity.CategorySummariesEntity
import com.alviy.core.database.entity.ExpenseCategoryEntity
import com.alviy.core.database.entity.ExpenseCategoryWithTransactions
import com.alviy.core.database.entity.ExpenseSummariesEntity
import com.alviy.core.database.entity.ExpenseSummaryEntity
import com.alviy.core.database.entity.ProfileEntity
import com.alviy.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface ExpenseDao {
    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    // Safe insert: ignores if categoryName + monthYear already exists
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: ExpenseCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExpenseSummary(summary: ExpenseSummaryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategorySummaries(summary: CategorySummariesEntity)

    // LOAD
    @Transaction
    @Query("SELECT * FROM profile LIMIT 1")
    fun getProfile(): Flow<ProfileEntity>

    @Query("SELECT * FROM expense_category WHERE monthYear = :monthYear")
    fun getCategories(monthYear: String): Flow<List<ExpenseCategoryEntity>>

    @Query(
        """
    SELECT DISTINCT monthYear
    FROM expense_category
    ORDER BY monthYear DESC
    """
    )
    fun getDistinctMonthYears(): Flow<List<String>>

    @Transaction
    @Query("SELECT * FROM expense_category WHERE monthYear = :monthYear")
    fun getExpenseJournal(monthYear: String): Flow<List<ExpenseCategoryWithTransactions>>

    @Transaction
    @Query("SELECT * FROM expense_summary WHERE monthYear = :monthYear LIMIT 1")
    fun getExpenseSummaries(monthYear: String): Flow<ExpenseSummariesEntity?>

    @Query("SELECT * FROM category_summary WHERE monthYear = :monthYear ORDER BY totalExpense DESC")
    fun getCategorySummaries(monthYear: String): Flow<List<CategorySummariesEntity>>

    // DELETE
    @Query("DELETE FROM expense_transaction WHERE transactionId = :id")
    suspend fun deleteTransaction(id: String)

    @Query("DELETE FROM expense_category WHERE categoryId = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)

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

    suspend fun clearAll() {
        clearTransactions()
        clearCategories()
        clearProfiles()
        clearExpenseSummary()
        clearCategorySummary()
    }

    // UPDATE
    @Query(
        """UPDATE expense_summary
        SET totalExpense = totalExpense + :amount,
        latestExpense = :amount,
        latestTransactionDate = :latestTrxDate
        WHERE monthYear = :monthYear"""
    )
    suspend fun updateExpenseSummary(
        monthYear: String,
        amount: BigDecimal,
        latestTrxDate: String
    )

    @Query(
        """
    UPDATE category_summary
    SET totalExpense = totalExpense + :amount,
    latestTrxDate = :date
    WHERE monthYear = :monthYear AND categoryId = :categoryId
    """
    )
    suspend fun updateCategorySummary(
        monthYear: String,
        categoryId: Int,
        amount: BigDecimal,
        date: String
    )
}