package com.alviy.core.di

import com.alviy.core.repository.ExpenseRepository
import com.alviy.core.repository.ExpenseRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository
}