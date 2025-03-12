package com.example.webviewbridgesample.model

import com.google.gson.annotations.SerializedName

data class GetTokenRes(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("rivsRqstId") val rivsRqstId: String
)
