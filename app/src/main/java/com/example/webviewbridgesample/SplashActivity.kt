package com.example.webviewbridgesample

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.webviewbridgesample.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var permissions: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT), SystemBarStyle.dark(Color.TRANSPARENT))

        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            if (isPermissionsAllGranted()) {
                moveToMain()
            } else {
                moveToPermission()
            }
        }, 500)
    }

    private fun isPermissionsAllGranted(): Boolean {
        permissions = mutableListOf()   // 권한 목록

        // android 10 이하에서 파일 쓰기 권한 요청
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        permissions.add(android.Manifest.permission.CAMERA)

        return permissions.all {
            ActivityCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 권한이 없을 때도 MainActivity로 이동 (권한 요청은 MainActivity에서 처리)
    private fun moveToPermission() {
        Intent(this, MainActivity::class.java).apply {
            putExtra("REQUIRE_PERMISSION", true)
            startActivity(this)
            finish()
        }
    }

    // 모든 권한이 있을 때 MainActivity로 이동
    private fun moveToMain() {
        Intent(this, MainActivity::class.java).apply {
            putExtra("REQUIRE_PERMISSION", false)
            startActivity(this)
            finish()
        }
    }
}