package com.example.facemuse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            start()
            return
        }

        setContentView(R.layout.activity_login)

        val emailField = findViewById<EditText>(R.id.etEmail)
        val passwordField = findViewById<EditText>(R.id.etPass)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val signUpText = findViewById<TextView>(R.id.tvSignUp)
        val forgotPasswordText = findViewById<TextView>(R.id.tvForgotPassword)
        val phoneLoginButton = findViewById<Button>(R.id.btnPhoneLogin)
        val ivPassToggle = findViewById<ImageView>(R.id.ivPassToggle)

        // Setup password toggle
        ViewUtils.setupPasswordToggle(passwordField, ivPassToggle)

        loginButton.setOnClickListener { 
            val email = emailField.text.toString()
            val pass = passwordField.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener { result ->
                        start()
                    }
                    .addOnFailureListener { 
                        Toast.makeText(this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        phoneLoginButton.setOnClickListener {
            startActivity(Intent(this, PhoneLoginActivity::class.java))
        }

        signUpText.setOnClickListener { 
            startActivity(Intent(this, SignupActivity::class.java))
        }

        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun start() {
        // Navigate to the Dashboard (MainActivity) after a successful login
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}
