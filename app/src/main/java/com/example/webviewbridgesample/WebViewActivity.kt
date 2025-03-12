package com.example.webviewbridgesample

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.webviewbridgesample.databinding.ActivityWebViewBinding
import com.example.webviewbridgesample.model.GetTokenRes
import com.example.webviewbridgesample.ui.WebViewViewModel
import org.json.JSONObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private val viewModel: WebViewViewModel by viewModels()

    private val RESULT_SUCCESS = "Y"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    finish()
                }
            }
        })

    }

    private fun init() {
        setupWebViewSettings(binding.webView.settings)
        binding.webView.webViewClient = CustomWebViewClient()
        binding.webView.webChromeClient = CustomWebChromeClient(activity = this)
        binding.webView.addJavascriptInterface(WebAppInterface(), "AndroidBridge")

        val bswrDvsnCode = intent.getStringExtra("BSWR_DVSN_CODE") ?: ""
        val rivsCustIdnrId = intent.getStringExtra("RIVS_CUST_IDNR_ID") ?: ""
        val rivsApiMthoId = intent.getStringExtra("RIVS_API_MTHO_ID") ?: ""

        viewModel.requestToken(
            bswrDvsnCode = bswrDvsnCode,
            rivsCustIdnrId = rivsCustIdnrId,
            rivsApiMthoId = rivsApiMthoId,
            onSuccess = { response ->
                loadWebView(response)
            },
            onFailure = {
                println("토큰 요청 실패")
            }
        )
    }

    private fun setupWebViewSettings(settings: WebSettings) {
        settings.apply {
            javaScriptEnabled = true // 자바스크립트 허용
            domStorageEnabled = true // 로컬 스토리지 사용 여부 설정
            useWideViewPort = true // 화면 사이즈 맞추기 허용
            setSupportZoom(false) // 화면 줌 허용 여부
            builtInZoomControls = false // 확대/축소 컨트롤 허용 여부
            displayZoomControls = false // 줌 컨트롤 표시 여부
            cacheMode = WebSettings.LOAD_NO_CACHE // 캐시 모드 설정
            allowFileAccess = true // 웹뷰에서 파일 접근 허용 여부
        }
    }

    private fun loadWebView(response: GetTokenRes) {
        val rivURL = Uri.Builder()
            .scheme("https")
            .authority("qariv.hanwhalife.com")
            .appendQueryParameter("authorization", response.accessToken)
            .appendQueryParameter("rivsRqstId", response.rivsRqstId)
            .build()

        binding.webView.loadUrl(rivURL.toString())
    }

    inner class WebAppInterface {
        private var result = "N";

        private fun decodeMessage(encodedMessage: String): JSONObject {
            val subMessage = encodedMessage.substringAfter("native://callNative?")
            val decodedBytes = Base64.decode(subMessage, Base64.DEFAULT)
            val decodedString = String(decodedBytes, StandardCharsets.UTF_8)
            val message = URLDecoder.decode(decodedString, StandardCharsets.UTF_8.toString())

            return JSONObject(message)
        }

        @JavascriptInterface
        fun callNativeMethod(encodedMessage: String) {
            runOnUiThread {
                try {
                    val message: JSONObject = decodeMessage(encodedMessage)

                    val command: String = message.getString("command")
                    val args: JSONObject = message.getJSONObject("args")
                    val callbackScriptName: String? = message.getString("callbackScriptName");

                    if (command == "saveData") {
                        if (args.getString("value") == "JUMIN" || args.getString("value") == "DRIVER") {
                            result = "Y"
                        }
                    } else if (command == "webClose") {
                        finish()
                    } else if (command == "requestPermission") {
                        requestPermission()
                    }
                } catch (e: Exception) {
                    e.printStackTrace();
                }
            }
        }

        @JavascriptInterface
        fun requestPermission() {
            runOnUiThread {
                val permission = android.Manifest.permission.CAMERA
                if (ContextCompat.checkSelfPermission(this@WebViewActivity, permission) == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 이미 있는 경우
                    println("카메라 권한이 이미 승인됨")
                } else {
                    // 권한이 없는 경우 요청
                    ActivityCompat.requestPermissions(this@WebViewActivity, arrayOf(permission), 99)
                }
            }
        }

        @JavascriptInterface
        fun webClose(result: String) {
            if (result == RESULT_SUCCESS) {
                println("성공")
            } else {
                println("실패")
            }
            finish()
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
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
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
            val permissions = arrayOf(
                android.Manifest.permission.CAMERA
            )

            val grantedPermissions = permissions.filter { permission ->
                ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
            }.toTypedArray()

            if (grantedPermissions.size == permissions.size) {
                // 모든 권한이 허용된 경우
                request?.grant(request.resources)
            } else {
                // 권한 요청
                ActivityCompat.requestPermissions(activity, permissions, 99)
            }
        }
    }
}
