package com.alviy.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alviy.core.database.entity.CategorySummariesEntity
import com.alviy.core.database.entity.ExpenseCategoryEntity
import com.alviy.core.database.entity.ExpenseSummaryEntity
import com.alviy.core.database.entity.ProfileEntity
import com.alviy.core.database.entity.TransactionEntity

@Database(
    entities = [
        ProfileEntity::class,
        ExpenseCategoryEntity::class,
        TransactionEntity::class,
        ExpenseSummaryEntity::class,
        CategorySummariesEntity::class
    ],
    version = 9
)
@TypeConverters(Converters::class)
abstract class MonerDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

}