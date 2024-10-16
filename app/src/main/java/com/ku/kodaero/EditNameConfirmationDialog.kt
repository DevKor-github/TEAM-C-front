package com.ku.kodaero

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.ku.kodaero.databinding.DialogEditNameConfirmationBinding

class EditNameConfirmationDialog(
    context: Context,
    private val enteredName: String, // Add entered name parameter
    private val onNameConfirmed: (String) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogEditNameConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogEditNameConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set dialog's background to be transparent
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Display entered name in the dialog's TextView (editNameLayoutText)
        binding.editNameLayoutText.text = enteredName

        // "Yes" button click event
        binding.editNameYesButton.setOnClickListener {
            onNameConfirmed(enteredName)
            dismiss() // Close dialog
        }

        // "No" button click event
        binding.editNameNoButton.setOnClickListener {
            dismiss() // Close dialog
        }
    }
}
