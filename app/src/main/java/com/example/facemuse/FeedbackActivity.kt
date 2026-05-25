package com.example.facemuse

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FeedbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        val backButton = findViewById<TextView>(R.id.tvBack)
        val submitButton = findViewById<Button>(R.id.btnSubmitFeedback)

        backButton.setOnClickListener {
            finish() // Go back to the previous screen
        }

        submitButton.setOnClickListener {
            // In a real app, you would send this feedback to a server.
            // For now, we will just show a thank you message and close the screen.
            Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
