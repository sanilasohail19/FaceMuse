package com.example.facemuse

import android.content.Intent
import android.widget.Button
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
data class RoutineStep(
    val number: Int,
    val title: String,
    val description: String
)
class RoutineActivity : AppCompatActivity() {

    private lateinit var stepsContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private val TAG = "ROUTINE_DEBUG"

    // 🔑 FIXED: Key trimmed to avoid header errors
    private val OPENROUTER_KEY = "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"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private var morningRoutine = mutableListOf<RoutineStep>()
    private var nightRoutine = mutableListOf<RoutineStep>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routine)

        stepsContainer = findViewById(R.id.llRoutineSteps)
        progressBar = findViewById(R.id.progressBar)
        val morningTab = findViewById<TextView>(R.id.tabMorning)
        val nightTab = findViewById<TextView>(R.id.tabNight)
        val backButton = findViewById<TextView>(R.id.tvBack)

        val detectedIssues = intent.getStringExtra("DETECTED_ISSUES") ?: "General health"
        val morningRoutineRaw = intent.getStringExtra("MORNING_ROUTINE")
        val nightRoutineRaw = intent.getStringExtra("NIGHT_ROUTINE")

        morningTab.setOnClickListener {
            it.background = getDrawable(R.drawable.tab_selected_background)
            nightTab.background = getDrawable(R.drawable.tab_unselected_background)
            updateRoutine(morningRoutine)
        }

        nightTab.setOnClickListener {
            it.background = getDrawable(R.drawable.tab_selected_background)
            morningTab.background = getDrawable(R.drawable.tab_unselected_background)
            updateRoutine(nightRoutine)
        }

        backButton.setOnClickListener { finish() }

        findViewById<Button>(R.id.btnAskChatbot).setOnClickListener {
            val intent = Intent(this, ChatBotActivity::class.java).apply {
                putExtra("SKIN_ISSUES", detectedIssues)
            }
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnViewHistory).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("NAVIGATE_TO_TAB", "nav_history")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        findViewById<TextView>(R.id.tvShareFeedback).setOnClickListener {
            val intent = Intent(this, FeedbackActivity::class.java)
            startActivity(intent)
        }

        if (!morningRoutineRaw.isNullOrEmpty() || !nightRoutineRaw.isNullOrEmpty()) {
            // Use pre-fetched routines
            morningRoutine.clear()
            morningRoutine.addAll(parseSteps(morningRoutineRaw ?: ""))
            nightRoutine.clear()
            nightRoutine.addAll(parseSteps(nightRoutineRaw ?: ""))
            
            progressBar.visibility = View.GONE
            stepsContainer.visibility = View.VISIBLE
            morningTab.performClick()
        } else {
            // Fallback: Fetch if not passed
            fetchRoutineFromOpenRouter(detectedIssues)
        }
    }

    private fun fetchRoutineFromOpenRouter(issues: String) {
        progressBar.visibility = View.VISIBLE
        stepsContainer.visibility = View.GONE

        val prompt = "The user has $issues. Provide a skincare routine. Format: MORNING: 1. Step: Detail. NIGHT: 1. Step: Detail."

        val json = JSONObject().apply {
            put("model", "anthropic/claude-3-haiku")
            put("messages", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            }))
        }

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Authorization", "Bearer $OPENROUTER_KEY")
            .addHeader("HTTP-Referer", "https://github.com/example/facemuse")
            .addHeader("X-Title", "FaceMuse Android")
            .post(json.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@RoutineActivity, "Check Internet", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val content = JSONObject(body).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
                    parseRoutine(content)
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        stepsContainer.visibility = View.VISIBLE
                        findViewById<TextView>(R.id.tabMorning).performClick()
                    }
                }
            }
        })
    }

    private fun parseRoutine(response: String) {
        val clean = response.replace("**", "")
        val parts = clean.split(Regex("NIGHT:?", RegexOption.IGNORE_CASE))

        if (parts.isNotEmpty()) {
            val morningText = parts[0].replace(Regex("MORNING:?", RegexOption.IGNORE_CASE), "")
            morningRoutine.clear()
            morningRoutine.addAll(parseSteps(morningText))
        }
        if (parts.size > 1) {
            nightRoutine.clear()
            nightRoutine.addAll(parseSteps(parts[1]))
        }
    }

    private fun parseSteps(text: String): List<RoutineStep> {
        val steps = mutableListOf<RoutineStep>()
        text.lines().forEach { line ->
            val regex = Regex("^(\\d+)[.)]\\s*([^:]+)(?::\\s*(.*))?$")
            regex.find(line.trim())?.let {
                val (num, title, desc) = it.destructured
                steps.add(RoutineStep(num.toInt(), title.trim(), desc.trim()))
            }
        }
        return steps
    }

    private fun updateRoutine(steps: List<RoutineStep>) {
        stepsContainer.removeAllViews()
        steps.forEach { step ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_routine_step, stepsContainer, false)
            view.findViewById<TextView>(R.id.tvStepNumber).text = step.number.toString()
            view.findViewById<TextView>(R.id.tvStepTitle).text = step.title
            view.findViewById<TextView>(R.id.tvStepDescription).text = step.description
            stepsContainer.addView(view)
        }
    }
}