package com.example.webviewbridgesample.repository

import com.example.webviewbridgesample.api.ApiClient
import com.example.webviewbridgesample.api.ApiService
import com.example.webviewbridgesample.model.ApiRequest
import com.example.webviewbridgesample.model.ApiResponse
import com.example.webviewbridgesample.model.GetTokenReq
import com.example.webviewbridgesample.model.GetTokenRes

class ApiRepository {
    private val apiService = ApiClient.instance.create(ApiService::class.java)

    suspend fun fetchToken(request: ApiRequest<GetTokenReq>): ApiResponse<GetTokenRes>? {
        return try {
            apiService.getToken(request)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}