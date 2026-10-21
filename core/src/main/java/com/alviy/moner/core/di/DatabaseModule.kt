package com.alviy.moner.core.di

import android.content.Context
import androidx.room.Room
import com.alviy.moner.core.data.local.MonerDatabase
import com.alviy.moner.core.data.local.dao.CategoryBudgetDao
import com.alviy.moner.core.data.local.dao.CategorySummaryDao
import com.alviy.moner.core.data.local.dao.SpendCategoryDao
import com.alviy.moner.core.data.local.dao.SpendLimitDao
import com.alviy.moner.core.data.local.dao.SpendSummaryDao
import com.alviy.moner.core.data.local.dao.ProfileDao
import com.alviy.moner.core.data.local.dao.ReceiptDao
import com.alviy.moner.core.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MonerDatabase =
        Room.databaseBuilder(
            context,
            MonerDatabase::class.java,
            "expense_db"
        )
            .addMigrations(MonerDatabase.MIGRATION_13_14, MonerDatabase.MIGRATION_14_15, MonerDatabase.MIGRATION_15_16)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideProfileDao(db: MonerDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideSpendCategoryDao(db: MonerDatabase): SpendCategoryDao = db.spendCategoryDao()

    @Provides
    fun provideTransactionDao(db: MonerDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideSpendSummaryDao(db: MonerDatabase): SpendSummaryDao = db.spendSummaryDao()

    @Provides
    fun provideCategorySummaryDao(db: MonerDatabase): CategorySummaryDao = db.categorySummaryDao()

    @Provides
    fun provideCategoryBudgetDao(db: MonerDatabase): CategoryBudgetDao = db.categoryBudgetDao()

    @Provides
    fun provideSpendLimitDao(db: MonerDatabase): SpendLimitDao = db.spendLimitDao()

    @Provides
    fun provideReceiptDao(db: MonerDatabase): ReceiptDao = db.receiptDao()

}
