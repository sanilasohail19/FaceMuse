package com.example.facemuse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InstructionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)

        val readyButton = findViewById<Button>(R.id.btnReady)
        val uploadLink = findViewById<TextView>(R.id.tvUploadLink)

        // "I'm Ready" button opens the camera
        readyButton.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        // "Upload" link now opens the dedicated UploadActivity
        uploadLink.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }
    }
}
