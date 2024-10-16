package com.ku.kodaero

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.ku.kodaero.databinding.DialogLoginConfirmationBinding

class LoginConfirmationDialog(context: Context, private val onLoginConfirmed: () -> Unit) : Dialog(context) {

    private lateinit var binding: DialogLoginConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogLoginConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dialog의 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // "예" 버튼 클릭 이벤트 처리
        binding.loginYesButton.setOnClickListener {
            onLoginConfirmed() // 로그아웃 처리
            dismiss() // 다이얼로그 닫기
        }

        // "아니요" 버튼 클릭 이벤트 처리
        binding.loginNoButton.setOnClickListener {
            dismiss() // 다이얼로그 닫기
        }
    }
}