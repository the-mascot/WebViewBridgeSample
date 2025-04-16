package com.example.webviewbridgesample.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.webviewbridgesample.R
import com.example.webviewbridgesample.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupTouchListener()
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Spinner 어댑터 설정
        val envOptions = resources.getStringArray(R.array.env_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, envOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinEnv.adapter = adapter
        
        // 모든 EditText에 포커스 변경 리스너 설정
        setupFocusChangeListeners()

        binding.callRivBtn.setOnClickListener {
            val bswrDvsnCode = binding.etBswrDvsnCode.text.toString()
            val rivsCustIdnrId = binding.etRivsCustIdnrId.text.toString()
            val rivsApiMthoId = binding.etRivsApiMthoId.text.toString()
            val rqstDeptCode = binding.etRqstDeptCode.text.toString()
            val keyInCount = binding.etKeyInCount.text.toString()
            val selectedEnvLabel = binding.spinEnv.selectedItem.toString()
            val selectedEnvValue = when (selectedEnvLabel) {
                "QA" -> "qa"
                "PROD" -> "prod"
                else -> "qa"
            }

            if (validateInputs(bswrDvsnCode, rivsCustIdnrId, rivsApiMthoId, rqstDeptCode)) {
                // 키보드 숨기기
                hideKeyboard()
                
                val action = HomeFragmentDirections.actionHomeFragmentToWebViewFragment(
                    bswrDvsnCode = bswrDvsnCode,
                    rivsCustIdnrId = rivsCustIdnrId,
                    rivsApiMthoId = rivsApiMthoId,
                    rqstDeptCode = rqstDeptCode,
                    keyInCount = keyInCount,
                    env = selectedEnvValue
                )
                findNavController().navigate(action)
            }
        }
    }
    
    private fun setupTouchListener() {
        // Fragment의 root view에 터치 리스너 추가
        binding.root.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
                view.clearFocus()
            }
            false
        }
    }
    
    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        requireActivity().currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
            it.clearFocus()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateInputs(bswrDvsnCode: String, rivsCustIdnrId: String, rivsApiMthoId: String, rqstDeptCode: String): Boolean {
        if (bswrDvsnCode.isEmpty()) {
            highlightError(binding.etBswrDvsnCode, "업무 구분 코드를 입력하세요.")
            return false
        }

        if (rivsCustIdnrId.isEmpty()) {
            highlightError(binding.etRivsCustIdnrId, "고객 식별 ID를 입력하세요.")
            return false
        }

        if (rivsApiMthoId.isEmpty()) {
            highlightError(binding.etRivsApiMthoId, "API 메서드를 입력하세요.")
            return false
        }

        if (rqstDeptCode.isEmpty()) {
            highlightError(binding.etRqstDeptCode, "업무구분코드를 입력하세요.")
            return false
        }

        return true
    }

    private fun highlightError(editText: EditText, errorMessage: String) {
        editText.error = errorMessage
        editText.requestFocus()
    }

    private fun setupFocusChangeListeners() {
        // 모든 EditText에 포커스 변경 리스너 설정
        val editTexts = listOf(
            binding.etBswrDvsnCode,
            binding.etRivsCustIdnrId,
            binding.etRivsApiMthoId,
            binding.etRqstDeptCode,
            binding.etKeyInCount
        )
        
        val focusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                hideKeyboard()
            }
        }
        
        editTexts.forEach { it.onFocusChangeListener = focusChangeListener }
    }
}