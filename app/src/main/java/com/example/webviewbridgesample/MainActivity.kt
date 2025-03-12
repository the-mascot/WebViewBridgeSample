package com.example.webviewbridgesample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.webviewbridgesample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.callRivBtn.setOnClickListener {
            val bswrDvsnCode = binding.etBswrDvsnCode.text.toString()
            val rivsCustIdnrId = binding.etRivsCustIdnrId.text.toString()
            val rivsApiMthoId = binding.etRivsApiMthoId.text.toString()

            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra("BSWR_DVSN_CODE", bswrDvsnCode)
                putExtra("RIVS_CUST_IDNR_ID", rivsCustIdnrId)
                putExtra("RIVS_API_MTHO_ID", rivsApiMthoId)
            }
            startActivity(intent)
        }
    }

}
