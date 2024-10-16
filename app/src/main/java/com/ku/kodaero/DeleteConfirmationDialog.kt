package com.ku.kodaero

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.graphics.Color

class DeleteConfirmationDialog(context: Context, private val onConfirm: () -> Unit) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_delete_confirmation)

        // Dialog의 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val deleteButton = findViewById<Button>(R.id.button_delete)
        val cancelButton = findViewById<Button>(R.id.button_cancel)
        val messageTextView = findViewById<TextView>(R.id.text_message)

        messageTextView.text = "정말 삭제하시겠습니까?"

        deleteButton.setOnClickListener {
            onConfirm()
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }
}
