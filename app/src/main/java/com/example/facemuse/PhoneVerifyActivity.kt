package com.example.facemuse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneVerifyActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private lateinit var phoneNumber: String
    private lateinit var otpField: EditText
    private lateinit var verifyButton: Button
    private lateinit var progressBar: ProgressBar
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_verify)

        auth = FirebaseAuth.getInstance()
        phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: ""
        
        otpField = findViewById(R.id.etOtp)
        verifyButton = findViewById(R.id.btnVerifyOtp)
        progressBar = findViewById(R.id.pbLoading)
        val resendCodeText = findViewById<android.widget.TextView>(R.id.tvResendCode)

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        startPhoneNumberVerification(phoneNumber)

        verifyButton.setOnClickListener {
            val code = otpField.text.toString()
            if (code.length == 6) {
                verifyPhoneNumberWithCode(verificationId, code)
            } else {
                Toast.makeText(this, "Enter a valid 6-digit code", Toast.LENGTH_SHORT).show()
            }
        }

        resendCodeText.setOnClickListener {
            resendPhoneNumberVerification(phoneNumber)
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        progressBar.visibility = View.VISIBLE
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            progressBar.visibility = View.GONE
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     retrieve the verification code from the SMS message.
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            progressBar.visibility = View.GONE
            Toast.makeText(this@PhoneVerifyActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            progressBar.visibility = View.GONE
            this@PhoneVerifyActivity.verificationId = verificationId
            this@PhoneVerifyActivity.resendToken = token
            Toast.makeText(this@PhoneVerifyActivity, "Code sent to $phoneNumber", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resendPhoneNumberVerification(phoneNumber: String) {
        val token = resendToken ?: return
        progressBar.visibility = View.VISIBLE
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        progressBar.visibility = View.VISIBLE
        
        val email = intent.getStringExtra("EMAIL")
        val password = intent.getStringExtra("PASSWORD")
        val name = intent.getStringExtra("NAME")

        if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
            // Sign-up flow: Create email/password account first
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        
                        // Link phone number to this new account
                        user?.linkWithCredential(credential)
                            ?.addOnCompleteListener { linkTask ->
                                if (linkTask.isSuccessful) {
                                    // Update display name
                                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build()
                                    user.updateProfile(profileUpdates)
                                    
                                    // Send verification email
                                    user.sendEmailVerification()
                                    
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(this, "Account created successfully. Please verify your email.", Toast.LENGTH_LONG).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finishAffinity()
                                } else {
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(this, "Phone linking failed: ${linkTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                    // Fallback: Proceed with just email account
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finishAffinity()
                                }
                            }
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            // Phone login flow: Just sign in with phone
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        progressBar.visibility = View.GONE
                        startActivity(Intent(this, MainActivity::class.java))
                        finishAffinity()
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Verification failed. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}