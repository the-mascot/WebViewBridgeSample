package com.example.webviewbridgesample.api

import com.example.webviewbridgesample.model.ApiRequest
import com.example.webviewbridgesample.model.ApiResponse
import com.example.webviewbridgesample.model.GetTokenReq
import com.example.webviewbridgesample.model.GetTokenRes
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/rmtIdVer/getToken")
    suspend fun getToken(@Body request: ApiRequest<GetTokenReq>): ApiResponse<GetTokenRes>
}