package com.example.facemuse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private var history: MutableList<AnalysisResult>,
    private val onDetailsClick: (AnalysisResult) -> Unit,
    private val onDeleteClick: (AnalysisResult) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateView: TextView = itemView.findViewById(R.id.history_date)
        val summaryView: TextView = itemView.findViewById(R.id.history_summary)
        val detailsButton: Button = itemView.findViewById(R.id.btnViewDetails)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val result = history[position]
        holder.dateView.text = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(result.date))
        holder.summaryView.text = result.analysis.substring(0, Math.min(result.analysis.length, 40)) + "..."

        holder.detailsButton.setOnClickListener { onDetailsClick(result) }
        holder.deleteButton.setOnClickListener { onDeleteClick(result) }
    }

    override fun getItemCount() = history.size

    fun updateData(newHistory: List<AnalysisResult>) {
        history = newHistory.toMutableList()
        notifyDataSetChanged()
    }
}
