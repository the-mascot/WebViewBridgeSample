package com.example.webviewbridgesample.ui.webview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webviewbridgesample.api.ApiClient
import com.example.webviewbridgesample.api.ApiService
import com.example.webviewbridgesample.model.*
import com.example.webviewbridgesample.repository.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebViewViewModel @Inject constructor(
    private val repository: ApiRepository,
    private val savedStateHandle: SavedStateHandle,
    private val apiClient: ApiClient
) : ViewModel() {
    
    private val _env = MutableStateFlow("qa")
    val env: StateFlow<String> = _env.asStateFlow()
    private lateinit var apiService: ApiService
    
    init {
        savedStateHandle.get<String>("env")?.let {
            setEnvironment(it)
        }
    }
    
    fun setEnvironment(environment: String) {
        _env.value = environment
        // 환경 설정이 변경되면 저장
        savedStateHandle["env"] = environment
        
        // API 서비스도 현재 환경 설정에 맞게 업데이트
        apiService = apiClient.getInstance(environment).create(ApiService::class.java)
    }

    fun requestToken(
        bswrDvsnCode: String, 
        rivsCustIdnrId: String, 
        rivsApiMthoId: String, 
        rqstDeptCode: String,
        keyInCount: String, 
        onSuccess: (GetTokenRes) -> Unit, 
        onFailure: () -> Unit
    ) {
        val request = ApiRequest(
            header = emptyMap(),
            payload = GetTokenReq(bswrDvsnCode, rivsCustIdnrId, rivsApiMthoId, rqstDeptCode, keyInCount)
        )

        viewModelScope.launch {
            val currentEnv = _env.value
            // 현재 설정된 환경 값으로 토큰 요청
            val response = repository.fetchToken(apiService, request)
            response?.payload?.let { onSuccess(it) } ?: onFailure()
        }
    }
} 