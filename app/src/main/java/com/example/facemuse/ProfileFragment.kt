package com.example.facemuse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val deleteAllDataButton = view.findViewById<Button>(R.id.btnDeleteAllData)
        val logoutButton = view.findViewById<Button>(R.id.btnLogout)

        deleteAllDataButton.setOnClickListener {
            showDeleteAllDataConfirmationDialog()
        }

        logoutButton.setOnClickListener {
            performLogout()
        }

        setupSwitches(view)

        return view
    }

    private fun setupSwitches(view: View) {
        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val switchStoreImages = view.findViewById<Switch>(R.id.switchStoreImages)
        val switchStoreAnalysis = view.findViewById<Switch>(R.id.switchStoreAnalysis)

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
        AlertDialog.Builder(requireContext())
            .setTitle("Delete All Data")
            .setMessage("Are you sure you want to permanently delete all of your data, including your account and analysis history? This action cannot be undone.")
            .setPositiveButton("Delete All") { _, _ ->
                // Perform data deletion and logout
                HistoryManager.clearHistory(requireContext())
                FirebaseAuth.getInstance().currentUser?.delete()?.addOnCompleteListener {
                    Toast.makeText(requireContext(), "All data has been deleted.", Toast.LENGTH_SHORT).show()
                    performLogout()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInClient.signOut().addOnCompleteListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}
