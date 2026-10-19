package com.alviy.moner.api.gmail.di

import com.alviy.moner.api.gmail.data.repository.GmailRepository
import com.alviy.moner.api.gmail.data.repository.GmailRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface GmailRepositoryModule {
    @Binds
    fun bindGmailRepository(impl: GmailRepositoryImpl): GmailRepository
}