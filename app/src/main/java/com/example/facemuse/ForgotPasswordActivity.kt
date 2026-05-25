package com.example.facemuse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val emailField = findViewById<EditText>(R.id.etEmail)
        val sendButton = findViewById<Button>(R.id.btnSendResetLink)
        val backToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        sendButton.setOnClickListener {
            val email = emailField.text.toString()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener { 
                        Toast.makeText(this, "Password reset link sent to your email.", Toast.LENGTH_LONG).show()
                        // Optionally, navigate back to login after a delay
                        // Or let the user navigate manually
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to send reset link: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        backToLogin.setOnClickListener {
            finish() // Simply close this activity to go back
        }
    }
}
