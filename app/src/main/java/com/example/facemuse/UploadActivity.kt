package com.example.facemuse

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class UploadActivity : AppCompatActivity() {

    // Launcher for the image picker
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val intent = Intent(this, ResultActivity::class.java)
            intent.data = it.data?.data
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        val dropZone = findViewById<LinearLayout>(R.id.llDropZone)
        val closeButton = findViewById<ImageButton>(R.id.btnClose)

        // The entire drop zone area is clickable
        dropZone.setOnClickListener {
            openGallery()
        }

        closeButton.setOnClickListener {
            finish()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }
}
