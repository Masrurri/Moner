package com.alviy.moner.api.gmail.di

import com.alviy.moner.api.gmail.data.repository.GmailRepositoryImpl
import com.alviy.moner.api.gmail.domain.repository.GmailRepository
import com.alviy.moner.api.gmail.parser.source.ReceiptSource
import com.alviy.moner.api.gmail.parser.source.ReceiptSources
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface GmailDataModule {

    @Binds
    fun bindGmailRepository(impl: GmailRepositoryImpl): GmailRepository

    companion object {
        /** The senders the parser understands; swap this binding in tests to feed fake sources. */
        @Provides
        fun provideReceiptSources(): List<@JvmSuppressWildcards ReceiptSource> = ReceiptSources.ALL
    }
}
