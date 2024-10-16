package com.ku.kodaero

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.ku.kodaero.databinding.DialogSuggestionConfirmationBinding

class SuggestionConfirmationDialog(
    context: Context,
    private val onSuggestionConfirmed: () -> Unit,
    private val onSuggestionDeclined: () -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogSuggestionConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogSuggestionConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dialog의 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // "예" 버튼 클릭 이벤트 처리
        binding.goToHomeButton.setOnClickListener {
            onSuggestionConfirmed()
            dismiss() // 다이얼로그 닫기
        }

        // "아니요" 버튼 클릭 이벤트 처리
        binding.suggestionSummitExitButton.setOnClickListener {
            onSuggestionDeclined()
            dismiss() // 다이얼로그 닫기
        }

        setOnCancelListener {
            onSuggestionDeclined() // Call the decline function on back button press
        }
    }
}