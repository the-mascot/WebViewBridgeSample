package com.example.webviewbridgesample.model

import com.google.gson.annotations.SerializedName

data class GetTokenReq(
    @SerializedName("bswrDvsnCode") val bswrDvsnCode: String,
    @SerializedName("rivsCustIdnrId") val rivsCustIdnrId: String,
    @SerializedName("rivsApiMthoId") val rivsApiMthoId: String
)
