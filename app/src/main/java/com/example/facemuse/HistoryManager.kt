package com.example.facemuse

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object HistoryManager {

    private const val PREFS_NAME = "FaceMuseHistory"
    private const val KEY_HISTORY = "analysis_history"

    fun saveAnalysis(context: Context, result: AnalysisResult) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val history = getHistory(context).toMutableList()
        history.add(0, result) // Add new result to the top of the list
        val json = gson.toJson(history)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun getHistory(context: Context): List<AnalysisResult> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(KEY_HISTORY, null)
        return if (json != null) {
            val type = object : TypeToken<List<AnalysisResult>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun deleteAnalysis(context: Context, result: AnalysisResult) {
        val history = getHistory(context).toMutableList()
        history.removeAll { it.date == result.date } // Remove by unique timestamp
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = gson.toJson(history)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun clearHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
