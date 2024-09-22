package com.devkor.kodaero

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.devkor.kodaero.databinding.DialogDeveloperAuthBinding

class DeveloperAuthDialog(context: Context, private val onPasswordVerified: (Boolean) -> Unit) : Dialog(context) {

    private lateinit var binding: DialogDeveloperAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogDeveloperAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dialog의 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 터치 영역 설정
        binding.touchArea.setOnClickListener {
            binding.passwordInput.hint = ""  // 힌트 제거
            binding.passwordInput.requestFocus()
            binding.passwordInput.performClick()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.passwordInput, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.passwordInput.gravity = Gravity.CENTER

        // 입력 필드의 포커스 및 텍스트 변경 이벤트 처리
        binding.passwordInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.passwordInput.gravity = Gravity.CENTER
            }
        }

        binding.passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 0) {
                    binding.passwordInput.gravity = Gravity.CENTER
                } else {
                    binding.passwordInput.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                }
            }
        })

        // 확인 버튼 클릭 이벤트 처리
        binding.confirmButton.setOnClickListener {
            val enteredPassword = binding.passwordInput.text.toString()

            if (enteredPassword.equals("Mask", ignoreCase = true)) {
                onPasswordVerified(true)  // 패스워드가 일치하는 경우
                Toast.makeText(context, "인증이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(context, "잘못된 암호입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
