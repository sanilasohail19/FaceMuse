package com.example.facemuse

import android.text.InputType
import android.widget.EditText
import android.widget.ImageView

object ViewUtils {
    fun setupPasswordToggle(passwordField: EditText, toggleIcon: ImageView) {
        var isPasswordVisible = false
        
        toggleIcon.setOnClickListener {
            if (isPasswordVisible) {
                // Hide password
                passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleIcon.setImageResource(R.drawable.ic_eye_hidden)
            } else {
                // Show password
                passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleIcon.setImageResource(R.drawable.ic_eye_visible)
            }
            isPasswordVisible = !isPasswordVisible
            
            // Move cursor to the end
            passwordField.setSelection(passwordField.text.length)
        }
    }
}