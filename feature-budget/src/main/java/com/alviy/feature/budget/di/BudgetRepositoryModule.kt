package com.alviy.feature.budget.di

import com.alviy.core.repository.ExpenseRepository
import com.alviy.core.repository.ExpenseRepositoryImpl
import com.alviy.feature.budget.data.repository.BudgetRepositoryImpl
import com.alviy.feature.budget.domain.repository.BudgetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface BudgetRepositoryModule {

    @Binds
    fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository
}