package com.alviy.app.gmail.di

import com.alviy.app.gmail.data.repository.GmailRepository
import com.alviy.app.gmail.data.repository.GmailRepositoryImpl
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