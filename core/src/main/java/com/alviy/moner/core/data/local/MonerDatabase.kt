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
import com.alviy.moner.core.data.local.dao.ReceiptDao
import com.alviy.moner.core.data.local.dao.TransactionDao
import com.alviy.moner.core.data.local.entity.CategoryBudgetEntity
import com.alviy.moner.core.data.local.entity.CategorySummaryEntity
import com.alviy.moner.core.data.local.entity.SpendCategoryEntity
import com.alviy.moner.core.data.local.entity.SpendLimitEntity
import com.alviy.moner.core.data.local.entity.SpendSummaryEntity
import com.alviy.moner.core.data.local.entity.ProfileEntity
import com.alviy.moner.core.data.local.entity.ReceiptEntity
import com.alviy.moner.core.data.local.entity.TransactionEntity

@Database(
    entities = [
        ProfileEntity::class,
        SpendCategoryEntity::class,
        TransactionEntity::class,
        SpendSummaryEntity::class,
        CategorySummaryEntity::class,
        CategoryBudgetEntity::class,
        SpendLimitEntity::class,
        ReceiptEntity::class
    ],
    version = 16
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
    abstract fun receiptDao(): ReceiptDao

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

        /** Adds receipt (fetched email receipts) and expense_transaction.receiptId (the receipt a spend came from). */
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `receipt` (`id` TEXT NOT NULL, `channel` TEXT NOT NULL, " +
                        "`activity` TEXT NOT NULL, `destination` TEXT NOT NULL, `reference` TEXT, " +
                        "`amount` TEXT NOT NULL, `transactionAt` INTEGER NOT NULL, `receivedAt` INTEGER NOT NULL, " +
                        "`method` TEXT, `note` TEXT, `isRecorded` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL("ALTER TABLE `expense_transaction` ADD COLUMN `receiptId` TEXT")
            }
        }
    }
}