package com.ku.kodaero

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.ku.kodaero.databinding.DialogLogoutConfirmationBinding

class LogoutConfirmationDialog(context: Context, private val onLogoutConfirmed: () -> Unit) : Dialog(context) {

    private lateinit var binding: DialogLogoutConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogLogoutConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dialog의 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // "예" 버튼 클릭 이벤트 처리
        binding.logoutYesButton.setOnClickListener {
            onLogoutConfirmed() // 로그아웃 처리
            dismiss() // 다이얼로그 닫기
        }

        // "아니요" 버튼 클릭 이벤트 처리
        binding.logoutNoButton.setOnClickListener {
            dismiss() // 다이얼로그 닫기
        }
    }
}
