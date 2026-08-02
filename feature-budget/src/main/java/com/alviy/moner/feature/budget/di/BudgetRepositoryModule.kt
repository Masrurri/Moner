package com.alviy.moner.feature.budget.di

import com.alviy.moner.feature.budget.data.repository.BudgetRepositoryImpl
import com.alviy.moner.feature.budget.domain.repository.BudgetRepository
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