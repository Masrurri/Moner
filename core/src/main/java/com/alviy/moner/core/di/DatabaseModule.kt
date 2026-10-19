package com.alviy.moner.core.di

import android.content.Context
import androidx.room.Room
import com.alviy.moner.core.data.local.MonerDatabase
import com.alviy.moner.core.data.local.dao.CategoryBudgetDao
import com.alviy.moner.core.data.local.dao.CategorySummaryDao
import com.alviy.moner.core.data.local.dao.ExpenseCategoryDao
import com.alviy.moner.core.data.local.dao.ExpenseSummaryDao
import com.alviy.moner.core.data.local.dao.ProfileDao
import com.alviy.moner.core.data.local.dao.TransactionDao
import com.alviy.moner.core.data.local.migration.MonerMigrations
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
            .addMigrations(*MonerMigrations.ALL)
            .fallbackToDestructiveMigrationFrom(*MonerMigrations.UNMIGRATABLE_VERSIONS)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    fun provideProfileDao(db: MonerDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideExpenseCategoryDao(db: MonerDatabase): ExpenseCategoryDao = db.expenseCategoryDao()

    @Provides
    fun provideTransactionDao(db: MonerDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideExpenseSummaryDao(db: MonerDatabase): ExpenseSummaryDao = db.expenseSummaryDao()

    @Provides
    fun provideCategorySummaryDao(db: MonerDatabase): CategorySummaryDao = db.categorySummaryDao()

    @Provides
    fun provideCategoryBudgetDao(db: MonerDatabase): CategoryBudgetDao = db.categoryBudgetDao()

}
