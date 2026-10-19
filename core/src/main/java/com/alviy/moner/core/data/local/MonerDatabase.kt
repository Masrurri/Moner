package com.alviy.moner.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alviy.moner.core.data.local.dao.CategoryBudgetDao
import com.alviy.moner.core.data.local.dao.CategorySummaryDao
import com.alviy.moner.core.data.local.dao.SpendCategoryDao
import com.alviy.moner.core.data.local.dao.SpendLimitDao
import com.alviy.moner.core.data.local.dao.SpendSummaryDao
import com.alviy.moner.core.data.local.dao.ProfileDao
import com.alviy.moner.core.data.local.dao.TransactionDao
import com.alviy.moner.core.data.local.entity.CategoryBudgetEntity
import com.alviy.moner.core.data.local.entity.CategorySummaryEntity
import com.alviy.moner.core.data.local.entity.SpendCategoryEntity
import com.alviy.moner.core.data.local.entity.SpendLimitEntity
import com.alviy.moner.core.data.local.entity.SpendSummaryEntity
import com.alviy.moner.core.data.local.entity.ProfileEntity
import com.alviy.moner.core.data.local.entity.TransactionEntity

@Database(
    entities = [
        ProfileEntity::class,
        SpendCategoryEntity::class,
        TransactionEntity::class,
        SpendSummaryEntity::class,
        CategorySummaryEntity::class,
        CategoryBudgetEntity::class,
        SpendLimitEntity::class
    ],
    version = 15
)
@TypeConverters(Converters::class)
abstract class MonerDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun spendCategoryDao(): SpendCategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun spendSummaryDao(): SpendSummaryDao
    abstract fun categorySummaryDao(): CategorySummaryDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao
    abstract fun spendLimitDao(): SpendLimitDao

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

        /** Adds spend_limit: one user-set spending limit per month. */
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `spend_limit` " +
                        "(`monthYear` TEXT NOT NULL, `amount` TEXT NOT NULL, PRIMARY KEY(`monthYear`))"
                )
            }
        }
    }
}