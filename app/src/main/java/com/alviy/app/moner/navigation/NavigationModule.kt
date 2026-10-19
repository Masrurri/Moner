package com.alviy.app.moner.navigation

import com.alviy.moner.core.navigation.AppNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NavigationModule {
    @Provides
    fun provideNavigatorFactory(): AppNavigator.Factory =
        AppNavigator.Factory { navController -> AppNavigatorImpl(navController) }
}