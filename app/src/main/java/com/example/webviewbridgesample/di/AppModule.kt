package com.example.webviewbridgesample.di

import com.example.webviewbridgesample.api.ApiClient
import com.example.webviewbridgesample.api.ApiService
import com.example.webviewbridgesample.repository.ApiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EnvironmentValue

@Module
@InstallIn(ViewModelComponent::class)
object AppModule {

    @Provides
    fun provideApiService(apiClient: ApiClient, @EnvironmentValue env: String): ApiService {
        return apiClient.getInstance(env).create(ApiService::class.java)
    }

    @Provides
    fun provideApiClient(): ApiClient {
        return ApiClient
    }

    @Provides
    @ViewModelScoped
    fun provideApiRepository(apiService: ApiService): ApiRepository {
        return ApiRepository(apiService)
    }
} 