package com.example.webviewbridgesample

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Navigation 컨트롤러 설정
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // 권한 요청이 필요한 경우
        val requirePermission = intent.getBooleanExtra("REQUIRE_PERMISSION", false)
        if (requirePermission) {
            requestPermissions()
        }
    }
    
    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        
        // 카메라 권한 추가
        permissions.add(android.Manifest.permission.CAMERA)
        
        // 권한 요청
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // 권한 결과 처리는 여기에 추가할 수 있습니다
            // 현재는 결과와 관계없이 홈 화면으로 이동합니다
        }
    }
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}
