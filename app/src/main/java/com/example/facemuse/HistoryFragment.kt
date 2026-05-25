package com.example.facemuse

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class HistoryFragment : Fragment() {

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyList: MutableList<AnalysisResult>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        val rvHistory = view.findViewById<RecyclerView>(R.id.rvHistory)
        val clearHistoryButton = view.findViewById<Button>(R.id.btnClearHistory)

        historyList = HistoryManager.getHistory(requireContext()).toMutableList()

        historyAdapter = HistoryAdapter(
            historyList,
            onDetailsClick = { result ->
                val intent = Intent(requireContext(), ResultActivity::class.java)
                intent.data = Uri.parse(result.imageUri)
                startActivity(intent)
            },
            onDeleteClick = { result ->
                showDeleteConfirmationDialog(result)
            }
        )

        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = historyAdapter

        clearHistoryButton.setOnClickListener {
            showClearAllConfirmationDialog()
        }

        return view
    }

    private fun showDeleteConfirmationDialog(result: AnalysisResult) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Analysis")
            .setMessage("Are you sure you want to delete this analysis record?")
            .setPositiveButton("Delete") { _, _ ->
                HistoryManager.deleteAnalysis(requireContext(), result)
                historyList.remove(result)
                historyAdapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearAllConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear All History")
            .setMessage("Are you sure you want to delete your entire analysis history? This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                HistoryManager.clearHistory(requireContext())
                historyList.clear()
                historyAdapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
