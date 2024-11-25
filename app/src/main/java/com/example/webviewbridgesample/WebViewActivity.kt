package com.example.webviewbridgesample

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.webviewbridgesample.databinding.ActivityMainBinding
import com.example.webviewbridgesample.databinding.ActivityWebViewBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    private val CAPTURE_CAMERA_RESULT = 3089
    private var filePathCallbackLollipop: ValueCallback<Array<Uri>>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }
    fun init() {
        binding.webView.apply {
            binding.webView.settings.javaScriptEnabled = true
            binding.webView.settings.setGeolocationEnabled(true)
            binding.webView.settings.pluginState = WebSettings.PluginState.ON
            binding.webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
            binding.webView.settings.domStorageEnabled = true
            binding.webView.settings.useWideViewPort = true
            binding.webView.addJavascriptInterface(WebAppInterface(), "AndroidApp")

            settings.javaScriptEnabled = true // 자바스크립트 허용
            settings.javaScriptCanOpenWindowsAutomatically = false
            // 팝업창을 띄울 경우가 있는데, 해당 속성을 추가해야 window.open() 이 제대로 작동 , 자바스크립트 새창도 띄우기 허용여부
            settings.setSupportMultipleWindows(false) // 새창 띄우기 허용 여부 (멀티뷰)
            settings.loadsImagesAutomatically = true // 웹뷰가 앱에 등록되어 있는 이미지 리소스를 자동으로 로드하도록 설정하는 속성
            settings.useWideViewPort = true // 화면 사이즈 맞추기 허용 여부
            settings.loadWithOverviewMode = true // 메타태그 허용 여부
            settings.setSupportZoom(true) // 화면 줌 허용여부
            settings.builtInZoomControls = false // 화면 확대 축소 허용여부
            settings.displayZoomControls = false // 줌 컨트롤 없애기.
            settings.cacheMode = WebSettings.LOAD_NO_CACHE // 웹뷰의 캐시 모드를 설정하는 속성으로써 5가지 모드
            /*
            (1) LOAD_CACHE_ELSE_NETWORK 기간이 만료돼 캐시를 사용할 수 없을 경우 네트워크를 사용합니다.
            (2) LOAD_CACHE_ONLY 네트워크를 사용하지 않고 캐시를 불러옵니다.
            (3) LOAD_DEFAULT 기본적인 모드로 캐시를 사용하고 만료된 경우 네트워크를 사용해 로드합니다.
            (4) LOAD_NORMAL 기본적인 모드로 캐시를 사용합니다.
            (5) LOAD_NO_CACHE 캐시모드를 사용하지 않고 네트워크를 통해서만 호출합니다.
             */
            settings.domStorageEnabled = true // 로컬 스토리지 사용 여부를 설정하는 속성으로 팝업창등을 '하루동안 보지 않기' 기능 사용에 필요
            settings.allowContentAccess // 웹뷰 내에서 파일 액세스 활성화 여부
            settings.userAgentString = "app" // 웹에서 해당 속성을 통해 앱에서 띄운 웹뷰로 인지 할 수 있도록 합니다.
            settings.defaultTextEncodingName = "UTF-8" // 인코딩 설정
            settings.databaseEnabled = true //Database Storage API 사용 여부 설정
        }
        binding.webView.webViewClient = CustomWebViewClient()
        binding.webView.webChromeClient = CustomWebChromeClient(activity = this)
        binding.webView.loadUrl("https://qariv.hanwhalife.com/")
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun sendResults(code: String) {
            runOnUiThread {
                finish()
                if (code == "0000") {
                    println("인증 성공")
                } else {
                    println("인증 실패")
                }
                val intent = Intent(this@WebViewActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
        }

        @JavascriptInterface
        fun requestPermission() {
            println("카메라 권한 거부 처리 실행")
            ActivityCompat.requestPermissions(this@WebViewActivity, arrayOf(android.Manifest.permission.CAMERA), 99)
        }
    }

    override fun onBackPressed() {
        if(binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 99) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 승인
                println("카메라 권한 승인")
            } else {
                // 권한 거부
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)) {
                    println("카메라 권한 거부 - 다시 요청 가능")
                } else {
                    println("카메라 권한 거부 - 다시 묻지 않기 선택됨")
                    // 사용자에게 설정 화면으로 이동하도록 안내
                    showPermissionDeniedDialog()
                }
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("권한 설정 필요")
            .setMessage("카메라 권한이 필요합니다. 설정에서 권한을 활성화하세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    class CustomWebViewClient() : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
        }

    }

    class CustomWebChromeClient(val activity: AppCompatActivity) : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest?) {
            val permission = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA)
            if (permission == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (request != null) {
                        request.grant(request.resources)
                    }
                }
            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.CAMERA), 99)
            }
        }
    }

}