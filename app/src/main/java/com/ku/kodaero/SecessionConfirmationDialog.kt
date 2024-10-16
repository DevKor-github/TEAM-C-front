package com.ku.kodaero

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.ku.kodaero.databinding.DialogSecessionConfirmationBinding

class SecessionConfirmationDialog(context: Context, private val onSecessionConfirmed: () -> Unit) : Dialog(context) {

    private lateinit var binding: DialogSecessionConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogSecessionConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dialog의 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // "예" 버튼 클릭 이벤트 처리
        binding.secessionYesButton.setOnClickListener {
            onSecessionConfirmed()
            dismiss() // 다이얼로그 닫기
        }

        // "아니요" 버튼 클릭 이벤트 처리
        binding.secessionNoButton.setOnClickListener {
            dismiss() // 다이얼로그 닫기
        }
    }
}