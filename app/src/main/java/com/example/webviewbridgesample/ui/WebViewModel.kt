package com.example.webviewbridgesample.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webviewbridgesample.model.*
import com.example.webviewbridgesample.repository.ApiRepository
import kotlinx.coroutines.launch

class WebViewViewModel : ViewModel() {
    private val repository = ApiRepository()

    fun requestToken(bswrDvsnCode: String, rivsCustIdnrId: String, rivsApiMthoId: String, onSuccess: (GetTokenRes) -> Unit, onFailure: () -> Unit) {
        val request = ApiRequest(
            header = emptyMap(),
            payload = GetTokenReq(bswrDvsnCode, rivsCustIdnrId, rivsApiMthoId)
        )

        viewModelScope.launch {
            val response = repository.fetchToken(request)
            response?.payload?.let { onSuccess(it) } ?: onFailure()
        }
    }
}
