package com.example.facemuse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PhoneLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_login)

        val phoneField = findViewById<EditText>(R.id.etPhone)
        val sendOtpButton = findViewById<Button>(R.id.btnSendOtp)
        val backToLogin = findViewById<TextView>(R.id.tvBackToLogin)
        val spCountryCode = findViewById<android.widget.Spinner>(R.id.spCountryCode)

        // Set up Country Code Spinner
        val countryCodes = arrayOf("+91 (IN)", "+92 (PK)", "+1 (US)", "+44 (UK)", "+971 (UAE)", "+61 (AU)")
        val adapter = android.widget.ArrayAdapter(this, R.layout.spinner_item, countryCodes)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spCountryCode.adapter = adapter

        sendOtpButton.setOnClickListener {
            val phone = phoneField.text.toString().trim()
            val selectedCountry = spCountryCode.selectedItem.toString().split(" ")[0]

            if (phone.length >= 10) {
                val formattedPhone = if (phone.startsWith("+")) phone else "$selectedCountry$phone"

                val intent = Intent(this, PhoneVerifyActivity::class.java)
                intent.putExtra("PHONE_NUMBER", formattedPhone)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            }
        }

        backToLogin.setOnClickListener {
            finish()
        }
    }
}