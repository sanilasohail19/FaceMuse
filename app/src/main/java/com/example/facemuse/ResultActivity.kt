package com.example.facemuse

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ResultActivity : AppCompatActivity() {

    private val TAG = "AI_DEBUG"
    private var detectedIssues: String = "No specific issues detected"
    private var imageUri: Uri? = null

    // New variables to hold the fetched routine
    private var morningRoutine: String? = null
    private var nightRoutine: String? = null

    private val OPENROUTER_KEY = "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"
    private val client = OkHttpClient.Builder()
        .connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val ivResult = findViewById<ImageView>(R.id.ivResult)
        val layout = findViewById<LinearLayout>(R.id.llAnalysisItems)
        val routineButton = findViewById<Button>(R.id.btnRoutine)
        val assistantButton = findViewById<Button>(R.id.btnAskQuestions)
        val viewHistoryText = findViewById<TextView>(R.id.tvViewHistory)

        imageUri = intent.data
        if (imageUri != null) {
            ivResult.setImageURI(imageUri)
            analyzeBriefly(imageUri!!, layout, routineButton)
        } else {
            handleAnalysisError(routineButton, "No image URI provided.")
        }

        viewHistoryText.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("NAVIGATE_TO_TAB", "nav_history")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        routineButton.setOnClickListener {
            val intent = Intent(this, RoutineActivity::class.java)
            // Pass the pre-fetched routine to the next activity
            intent.putExtra("MORNING_ROUTINE", morningRoutine)
            intent.putExtra("NIGHT_ROUTINE", nightRoutine)
            // Also pass the issues, so RoutineActivity can use it as a fallback
            intent.putExtra("DETECTED_ISSUES", detectedIssues)
            startActivity(intent)
        }

        assistantButton.setOnClickListener {
            val intent = Intent(this, ChatBotActivity::class.java)
            intent.putExtra("SKIN_ISSUES", detectedIssues)
            startActivity(intent)
        }
    }

    private fun analyzeBriefly(uri: Uri, layout: LinearLayout, btn: Button) {
        btn.isEnabled = false
        btn.text = "Detecting Issues..."

        try {
            // Compress and Resize Image to prevent timeouts
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                handleAnalysisError(btn, "Could not decode image.")
                return
            }

            // Resize to max 800px width or height while maintaining aspect ratio
            val maxDimension = 800
            val scale = Math.min(maxDimension.toFloat() / originalBitmap.width, maxDimension.toFloat() / originalBitmap.height)
            val finalWidth = if (scale < 1) (originalBitmap.width * scale).toInt() else originalBitmap.width
            val finalHeight = if (scale < 1) (originalBitmap.height * scale).toInt() else originalBitmap.height
            
            val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true)

            val outputStream = java.io.ByteArrayOutputStream()
            resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, outputStream)
            val bytes = outputStream.toByteArray()
            val base64Image = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)

            val prompt = "Analyze the user's face from the image. Your response MUST be ONLY a single, raw JSON object. It must contain these keys: 'hydration' (string, e.g., 'Dehydrated', 'Normal', 'Hydrated'), 'hydration_pc' (integer, 0-100), 'texture' (string, e.g., 'Rough', 'Smooth'), 'texture_pc' (integer, 0-100), 'skin_tone' (string, e.g., 'Uneven', 'Even'), 'skin_tone_pc' (integer, 0-100), and 'issues' (string of comma-separated values, e.g., 'Acne, Dark Circles, Redness'). Do not include any other text or markdown."

            val json = JSONObject().apply {
                put("model", "anthropic/claude-3-haiku")
                put("messages", JSONArray().put(JSONObject().apply {
                    put("role", "user")
                    put("content", JSONArray()
                        .put(JSONObject().put("type", "text").put("text", prompt))
                        .put(JSONObject().put("type", "image_url").put("image_url", JSONObject().put("url", "data:image/jpeg;base64,$base64Image")))
                    )
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
                    handleAnalysisError(btn, "API call failed: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        try {
                            val responseJson = JSONObject(body)
                            if (responseJson.has("choices") && responseJson.getJSONArray("choices").length() > 0) {
                                val content = responseJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()
                                val cleanContent = content.removePrefix("```json").removeSuffix("```").trim()

                                try {
                                    val data = JSONObject(cleanContent)
                                    detectedIssues = data.optString("issues", "No specific issues detected.")

                                    val analysisResult = AnalysisResult(
                                        date = System.currentTimeMillis(),
                                        imageUri = uri.toString(),
                                        analysis = cleanContent
                                    )
                                    HistoryManager.saveAnalysis(this@ResultActivity, analysisResult)

                                    runOnUiThread {
                                        layout.removeAllViews()
                                        addResultRow(layout, "Hydration", data.optString("hydration", "N/A"), data.optInt("hydration_pc", 0))
                                        addResultRow(layout, "Texture", data.optString("texture", "N/A"), data.optInt("texture_pc", 0))
                                        addResultRow(layout, "Skin Tone", data.optString("skin_tone", "N/A"), data.optInt("skin_tone_pc", 0))
                                        addResultRow(layout, "Issues", detectedIssues, -1)
                                        
                                        // NEW: Trigger routine generation after showing analysis
                                        fetchPersonalizedRoutine(detectedIssues, btn)
                                    }
                                } catch (e: JSONException) {
                                    handleAnalysisError(btn, "Failed to parse JSON: $cleanContent | Error: ${e.message}")
                                }
                            } else {
                                handleAnalysisError(btn, "API response did not contain 'choices'. Body: $body")
                            }
                        } catch (e: Exception) {
                            handleAnalysisError(btn, "Failed to parse response body: ${e.message}. Body: $body")
                        }
                    } else {
                        handleAnalysisError(btn, "API call not successful. Code: ${response.code}. Body: $body")
                    }
                }
            })
        } catch (e: Exception) {
            handleAnalysisError(btn, "Fatal Error: ${e.message}")
        }
    }

    // NEW FUNCTION to get the routine based on the detected issues
    private fun fetchPersonalizedRoutine(issues: String, btn: Button) {
        runOnUiThread {
            btn.text = "Generating Routine..."
        }

        val prompt = "Based on these skin issues: '$issues', create a personalized daily skincare routine. The response must contain separate sections for MORNING and NIGHT. For each section, list numbered steps with a title and a short, clear description. Format it strictly like this example: MORNING: 1. Cleanse: Gently wash your face. NIGHT: 1. Cleanse: Remove makeup and wash."

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
            .post(json.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Routine fetch failed: ${e.message}")
                runOnUiThread {
                    btn.isEnabled = true
                    btn.text = "Get Full Solution"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    try {
                        val content = JSONObject(body).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
                        
                        val parts = content.split(Regex("NIGHT:?", RegexOption.IGNORE_CASE))
                        morningRoutine = parts.getOrNull(0)?.replace(Regex("MORNING:?", RegexOption.IGNORE_CASE), "")?.trim()
                        nightRoutine = parts.getOrNull(1)?.trim()
                        
                        runOnUiThread {
                            btn.isEnabled = true
                            btn.text = "Get Full Solution"
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse routine response: $body")
                        runOnUiThread {
                            btn.isEnabled = true
                            btn.text = "Get Full Solution" 
                        }
                    }
                } else {
                     Log.e(TAG, "Routine fetch API error: ${response.code} - $body")
                     runOnUiThread {
                        btn.isEnabled = true
                        btn.text = "Get Full Solution"
                    }
                }
            }
        })
    }

    private fun handleAnalysisError(btn: Button, logMessage: String) {
        Log.e(TAG, logMessage)
        detectedIssues = "Analysis failed. Please try again."
        runOnUiThread {
            btn.isEnabled = true
            btn.text = "Analysis Failed"
            val layout = findViewById<LinearLayout>(R.id.llAnalysisItems)
            layout.removeAllViews()
            val errorTextView = TextView(this)
            val userFriendlyMessage = when {
                logMessage.contains("Code: 401") -> "Authentication failed. Your API Key seems to be invalid."
                logMessage.contains("API call failed") -> "Network error. Please check your internet connection."
                logMessage.contains("Failed to parse") -> "The AI returned an unexpected response."
                else -> "An unexpected error occurred."
            }
            errorTextView.text = "Error: $userFriendlyMessage"
            errorTextView.setPadding(24, 24, 24, 24)
            layout.addView(errorTextView)
        }
    }

    private fun addResultRow(layout: LinearLayout, name: String, status: String, pc: Int) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_analysis_row, layout, false)
        view.findViewById<TextView>(R.id.tvAnalysisName).text = name
        view.findViewById<TextView>(R.id.tvAnalysisStatus).text = status
        
        val percentageTextView = view.findViewById<TextView>(R.id.tvAnalysisPercentage)

        if (pc >= 0) {
            percentageTextView.text = "$pc%"
            percentageTextView.visibility = View.VISIBLE
        } else {
            percentageTextView.visibility = View.GONE
        }
        layout.addView(view)
    }
}