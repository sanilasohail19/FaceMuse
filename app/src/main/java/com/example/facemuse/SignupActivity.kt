package com.example.facemuse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val nameField = findViewById<EditText>(R.id.etName)
        val emailField = findViewById<EditText>(R.id.etEmail)
        val phoneField = findViewById<EditText>(R.id.etPhone)
        val passwordField = findViewById<EditText>(R.id.etPass)
        val confirmPasswordField = findViewById<EditText>(R.id.etConfirmPass)
        val createAccountButton = findViewById<Button>(R.id.btnCreateAccount)
        val loginLink = findViewById<TextView>(R.id.tvLoginLink)
        val spCountryCode = findViewById<android.widget.Spinner>(R.id.spCountryCode)
        val ivPassToggle = findViewById<android.widget.ImageView>(R.id.ivPassToggle)
        val ivConfirmPassToggle = findViewById<android.widget.ImageView>(R.id.ivConfirmPassToggle)

        // Setup password toggles
        ViewUtils.setupPasswordToggle(passwordField, ivPassToggle)
        ViewUtils.setupPasswordToggle(confirmPasswordField, ivConfirmPassToggle)

        // Set up Country Code Spinner
        val countryCodes = arrayOf("+91 (IN)", "+92 (PK)", "+1 (US)", "+44 (UK)", "+971 (UAE)", "+61 (AU)")
        val adapter = android.widget.ArrayAdapter(this, R.layout.spinner_item, countryCodes)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spCountryCode.adapter = adapter

        createAccountButton.setOnClickListener {
            val name = nameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val phone = phoneField.text.toString().trim()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()
            val selectedCountry = spCountryCode.selectedItem.toString().split(" ")[0]

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.length < 10) {
                Toast.makeText(this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Format phone number to E.164 (optional, but keep for profile if needed)
            val formattedPhone = if (phone.startsWith("+")) phone else "$selectedCountry$phone"

            // Create account directly
            createAccount(email, password, name, formattedPhone)
        }

        loginLink.setOnClickListener {
            // Navigate back to the Login screen
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun createAccount(email: String, pass: String, name: String, phone: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    // Update profile with name
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user.updateProfile(profileUpdates)

                    // Send verification email
                    user.sendEmailVerification()

                    Toast.makeText(this, "Account created! Please verify your email.", Toast.LENGTH_LONG).show()
                    
                    // Navigate to Main
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Signup failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
