package com.example.facemuse

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ChatBotActivity : AppCompatActivity() {

    private val messages = mutableListOf<Message>()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var rvChat: RecyclerView
    private lateinit var etPrompt: EditText
    private lateinit var llQuickQuestions: View

    // 🔑 PLEASE ENSURE THIS KEY IS VALID AND HAS CREDITS
    private val OPENROUTER_KEY = "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"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private var systemContext = "You are FaceMuse AI, a skincare expert."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bot)

        rvChat = findViewById(R.id.rvChat)
        etPrompt = findViewById(R.id.etPrompt)
        llQuickQuestions = findViewById(R.id.llQuickQuestions)
        val btnSend = findViewById<ImageButton>(R.id.btnSend)
        val tvBack = findViewById<TextView>(R.id.tvBack)

        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = false 
        }
        rvChat.adapter = chatAdapter

        val skinIssues = intent.getStringExtra("SKIN_ISSUES") ?: "general skin health"
        systemContext += " The user has concerns about $skinIssues."
        
        addMessage("Hello! I'm your AI Skincare Assistant. How can I help with your $skinIssues today?", false)

        tvBack.setOnClickListener { finish() }
        btnSend.setOnClickListener {
            val text = etPrompt.text.toString().trim()
            if (text.isNotEmpty()) sendMessage(text)
        }

        findViewById<Button>(R.id.q1).setOnClickListener { sendMessage("Explain my results") }
        findViewById<Button>(R.id.q2).setOnClickListener { sendMessage("Routine tips") }
        findViewById<Button>(R.id.q3).setOnClickListener { sendMessage("Is my data safe?") }
        findViewById<Button>(R.id.q4).setOnClickListener { sendMessage("Products") }
    }

    private fun sendMessage(userText: String) {
        addMessage(userText, true)
        etPrompt.text.clear()
        llQuickQuestions.visibility = View.GONE

        val typingMsg = Message("...", false, isTyping = true)
        runOnUiThread {
            messages.add(typingMsg)
            chatAdapter.notifyDataSetChanged()
            rvChat.scrollToPosition(messages.size - 1)
        }

        lifecycleScope.launch {
            val responseText = callOpenRouterApi()
            runOnUiThread {
                messages.remove(typingMsg)
                addMessage(responseText, false)
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        runOnUiThread {
            messages.add(Message(text, isUser))
            chatAdapter.notifyDataSetChanged()
            rvChat.post {
                rvChat.scrollToPosition(messages.size - 1)
            }
        }
    }

    private suspend fun callOpenRouterApi(): String = withContext(Dispatchers.IO) {
        try {
            val messagesJson = JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", systemContext))
                messages.filter { !it.isTyping }.takeLast(5).forEach {
                    put(JSONObject().put("role", if (it.isFromUser) "user" else "assistant").put("content", it.text))
                }
            }

            val request = Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .addHeader("Authorization", "Bearer $OPENROUTER_KEY")
                .post(JSONObject().apply {
                    put("model", "anthropic/claude-3-haiku")
                    put("messages", messagesJson)
                }.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (response.isSuccessful && body != null) {
                JSONObject(body).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
            } else {
                "Error: ${response.code}. Please check your API key/credits."
            }
        } catch (e: Exception) { "Network Error: ${e.message}" }
    }
}