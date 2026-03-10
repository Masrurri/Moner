package com.alviy.feature.budget.di

import com.alviy.feature.budget.data.remote.api.BudgetApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object BudgetModule {
    @Provides
    fun provideBudgetApi(retrofit: Retrofit): BudgetApi = retrofit.create(BudgetApi::class.java)
}