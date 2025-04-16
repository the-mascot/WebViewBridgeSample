package com.example.webviewbridgesample.repository

import com.example.webviewbridgesample.api.ApiService
import com.example.webviewbridgesample.model.ApiRequest
import com.example.webviewbridgesample.model.ApiResponse
import com.example.webviewbridgesample.model.GetTokenReq
import com.example.webviewbridgesample.model.GetTokenRes
import javax.inject.Inject

class ApiRepository @Inject constructor(
    private val defaultApiService: ApiService
) {
    suspend fun fetchToken(request: ApiRequest<GetTokenReq>): ApiResponse<GetTokenRes>? {
        return try {
            defaultApiService.getToken(request)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun fetchToken(apiService: ApiService, request: ApiRequest<GetTokenReq>): ApiResponse<GetTokenRes>? {
        return try {
            apiService.getToken(request)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}