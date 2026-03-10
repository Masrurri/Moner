package com.alviy.core.di

import android.content.Context
import androidx.room.Room
import com.alviy.core.database.ExpenseDao
import com.alviy.core.database.MonerDatabase
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
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideExpenseDao(db: MonerDatabase): ExpenseDao = db.expenseDao()

}