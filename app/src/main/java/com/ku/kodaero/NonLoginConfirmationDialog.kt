package com.ku.kodaero

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.ku.kodaero.databinding.DialogNonloginConfirmationBinding

class NonLoginConfirmationDialog(context: Context, private val onNonLoginConfirmed: () -> Unit) : Dialog(context) {

    private lateinit var binding: DialogNonloginConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogNonloginConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dialog의 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // "예" 버튼 클릭 이벤트 처리
        binding.nonLoginYesButton.setOnClickListener {
            onNonLoginConfirmed()
            dismiss()
        }
    }
}