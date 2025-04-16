package com.example.webviewbridgesample.ui.webview

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.webviewbridgesample.constants.CommonConstants
import com.example.webviewbridgesample.databinding.FragmentWebViewBinding
import com.example.webviewbridgesample.di.EnvironmentFragment
import com.example.webviewbridgesample.model.GetTokenRes
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@AndroidEntryPoint
class WebViewFragment : EnvironmentFragment() {

    private var _binding: FragmentWebViewBinding? = null
    private val binding get() = _binding!!
    private val args: WebViewFragmentArgs by navArgs()

    private val RESULT_SUCCESS = "Y"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setEnvironment(args.env)
        
        init()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    findNavController().navigateUp()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun init() {
        setupWebViewSettings(binding.webView.settings)
        binding.webView.webViewClient = CustomWebViewClient()
        binding.webView.webChromeClient = CustomWebChromeClient(requireActivity())
        binding.webView.addJavascriptInterface(WebAppInterface(), "AndroidBridge")

        val bswrDvsnCode = args.bswrDvsnCode
        val rivsCustIdnrId = args.rivsCustIdnrId
        val rivsApiMthoId = args.rivsApiMthoId
        val rqstDeptCode = args.rqstDeptCode
        val keyInCount = args.keyInCount
        val env = args.env

        viewModel.requestToken(
            bswrDvsnCode = bswrDvsnCode,
            rivsCustIdnrId = rivsCustIdnrId,
            rivsApiMthoId = rivsApiMthoId,
            rqstDeptCode = rqstDeptCode,
            keyInCount = keyInCount,
            onSuccess = { response ->
                loadWebView(env, response)
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

    private fun loadWebView(env: String, response: GetTokenRes) {
        // 환경설정에 따라 URL 설정
        val authority = if (env == "prod") {
            CommonConstants.PROD_URL.removePrefix("https://")
        } else {
            CommonConstants.QA_URL.removePrefix("https://")
        }
        
        val rivURL = Uri.Builder()
            .scheme("https")
            .authority(authority)
            .appendQueryParameter("authorization", response.accessToken)
            .appendQueryParameter("rivsRqstId", response.rivsRqstId)
            .build()

        println("요청 URL: ${rivURL.toString()}, 환경: $env")
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
            requireActivity().runOnUiThread {
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
                        findNavController().navigateUp()
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
            requireActivity().runOnUiThread {
                val permission = android.Manifest.permission.CAMERA
                if (requireContext().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 이미 있는 경우
                    println("카메라 권한이 이미 승인됨")
                } else {
                    // 권한이 없는 경우 요청
                    requestPermissionLauncher.launch(permission)
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
            findNavController().navigateUp()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한 승인
            println("카메라 권한 승인")
        } else {
            // 권한 거부
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                println("카메라 권한 거부 - 다시 요청 가능")
            } else {
                println("카메라 권한 거부 - 다시 묻지 않기 선택됨")
                // 사용자에게 설정 화면으로 이동하도록 안내
                showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("권한 설정 필요")
            .setMessage("카메라 권한이 필요합니다. 설정에서 권한을 활성화하세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
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

    class CustomWebChromeClient(private val activity: androidx.activity.ComponentActivity) : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest?) {
            val permissions = arrayOf(
                android.Manifest.permission.CAMERA
            )

            val grantedPermissions = permissions.filter { permission ->
                activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            }.toTypedArray()

            if (grantedPermissions.size == permissions.size) {
                // 모든 권한이 허용된 경우
                request?.grant(request.resources)
            } else {
                // 권한 요청 - Fragment에서 처리되도록 함
                println("웹뷰에서 카메라 권한 요청이 필요합니다")
            }
        }
    }
}
