package com.example.facemuse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class PrivacyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        val backButton = findViewById<TextView>(R.id.tvBack)
        val deleteAllDataButton = findViewById<Button>(R.id.btnDeleteAllData)
        val logoutButton = findViewById<Button>(R.id.btnLogout)

        backButton.setOnClickListener {
            finish() // Go back to the previous screen
        }

        deleteAllDataButton.setOnClickListener {
            showDeleteAllDataConfirmationDialog()
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        setupSwitches()
    }

    private fun setupSwitches() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = prefs.edit()

        val switchStoreImages = findViewById<Switch>(R.id.switchStoreImages)
        val switchStoreAnalysis = findViewById<Switch>(R.id.switchStoreAnalysis)

        // Load saved state
        switchStoreImages.isChecked = prefs.getBoolean("store_images", true)
        switchStoreAnalysis.isChecked = prefs.getBoolean("store_analysis", true)

        // Set listeners
        switchStoreImages.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("store_images", isChecked).apply()
        }

        switchStoreAnalysis.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("store_analysis", isChecked).apply()
        }
    }

    private fun showDeleteAllDataConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete All Data")
            .setMessage("Are you sure you want to permanently delete all of your data, including your account and analysis history? This action cannot be undone.")
            .setPositiveButton("Delete All") { _, _ ->
                // Perform data deletion and logout
                HistoryManager.clearHistory(this)
                FirebaseAuth.getInstance().currentUser?.delete()?.addOnCompleteListener {
                    Toast.makeText(this, "All data has been deleted.", Toast.LENGTH_SHORT).show()
                    // Restart the app at the login screen
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
