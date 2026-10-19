package com.alviy.moner.core.di

import com.alviy.moner.core.domain.repository.SpendRepository
import com.alviy.moner.core.data.repository.SpendRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    fun bindSpendRepository(impl: SpendRepositoryImpl): SpendRepository
}