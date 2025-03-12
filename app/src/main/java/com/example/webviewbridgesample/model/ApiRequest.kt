package com.example.webviewbridgesample.model

import com.google.gson.annotations.SerializedName

data class ApiRequest<T>(
    @SerializedName("header") val header: Map<String, String> = emptyMap(),
    @SerializedName("payload") val payload: T
)
