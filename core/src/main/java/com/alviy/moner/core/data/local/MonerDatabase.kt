package com.alviy.moner.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 12
)
@TypeConverters(Converters::class)
abstract class MonerDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun expenseSummaryDao(): ExpenseSummaryDao
    abstract fun categorySummaryDao(): CategorySummaryDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao

}