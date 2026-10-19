package com.alviy.moner.api.gmail.di

import com.alviy.moner.api.gmail.data.remote.api.GmailApi
import com.alviy.moner.core.network.ApiBaseUrl
import com.alviy.moner.core.network.RetrofitFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Network bindings for :api-gmail. Core never sees this module. */
@Module
@InstallIn(SingletonComponent::class)
object ApiGmailModule {

    @Provides
    @Singleton
    fun provideGmailApi(factory: RetrofitFactory): GmailApi = factory.create(ApiBaseUrl.GMAIL)
}
