package com.alviy.moner.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alviy.moner.core.data.local.dao.CategoryBudgetDao
import com.alviy.moner.core.data.local.dao.CategorySummaryDao
import com.alviy.moner.core.data.local.dao.ExpenseCategoryDao
import com.alviy.moner.core.data.local.dao.ExpenseSummaryDao
import com.alviy.moner.core.data.local.dao.ProfileDao
import com.alviy.moner.core.data.local.dao.TransactionDao
import com.alviy.moner.core.data.local.entity.CategoryBudgetEntity
import com.alviy.moner.core.data.local.entity.CategorySummaryEntity
import com.alviy.moner.core.data.local.entity.ExpenseCategoryEntity
import com.alviy.moner.core.data.local.entity.ExpenseSummaryEntity
import com.alviy.moner.core.data.local.entity.ProfileEntity
import com.alviy.moner.core.data.local.entity.TransactionEntity

@Database(
    entities = [
        ProfileEntity::class,
        ExpenseCategoryEntity::class,
        TransactionEntity::class,
        ExpenseSummaryEntity::class,
        CategorySummaryEntity::class,
        CategoryBudgetEntity::class
    ],
    version = 14
)
@TypeConverters(Converters::class)
abstract class MonerDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun expenseSummaryDao(): ExpenseSummaryDao
    abstract fun categorySummaryDao(): CategorySummaryDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao

    companion object {
        /** Drops expense_summary.latestExpense / latestTransactionDate; today/week spend is now queried live. */
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `expense_summary_new` " +
                        "(`monthYear` TEXT NOT NULL, `totalExpense` TEXT NOT NULL, PRIMARY KEY(`monthYear`))"
                )
                db.execSQL(
                    "INSERT INTO `expense_summary_new` (`monthYear`, `totalExpense`) " +
                        "SELECT `monthYear`, `totalExpense` FROM `expense_summary`"
                )
                db.execSQL("DROP TABLE `expense_summary`")
                db.execSQL("ALTER TABLE `expense_summary_new` RENAME TO `expense_summary`")
            }
        }
    }
}