package com.example.webviewbridgesample.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object EnvironmentModule {

    // 기본 환경 설정값 제공
    @EnvironmentValue
    @Provides
    @ViewModelScoped
    fun provideEnvironment(): String {
        return "qa" // 기본값
    }
} 