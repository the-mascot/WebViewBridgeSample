package com.example.webviewbridgesample.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.webviewbridgesample.ui.webview.WebViewViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment에서 환경 설정을 쉽게 전달할 수 있는 확장 함수
 */
fun Fragment.provideEnvironment(env: String) {
    if (this is InjectedEnvironment) {
        this.setEnvironment(env)
    }
}

/**
 * 환경 설정을 주입받을 수 있는 인터페이스
 */
interface InjectedEnvironment {
    fun setEnvironment(env: String)
}

/**
 * WebViewFragment에서 구현할 확장 함수
 */
@AndroidEntryPoint
abstract class EnvironmentFragment : Fragment(), InjectedEnvironment {
    val viewModel: WebViewViewModel by viewModels()
    
    override fun setEnvironment(env: String) {
        viewModel.setEnvironment(env)
    }
} 