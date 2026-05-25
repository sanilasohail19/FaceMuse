package com.example.facemuse

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GeminiRepository {

    // Using the free tier API Key.
    // Ensure this key is valid and has the Gemini API enabled in Google AI Studio.
    // If you receive an HTTP 403 error, this key may be invalid or expired.
    private val apiKey = "AIzaSyDkBnnNIvedF6L8GSHQerAXAX7IEzHtDWg"

    private val api = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeminiApi::class.java)

    suspend fun getAIResponse(prompt: String): String {
        return try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(listOf(Part(text = prompt)))
                )
            )
            
            val response = api.generateText(apiKey, request)
            
            val candidate = response.candidates?.firstOrNull()
            val text = candidate?.content?.parts?.firstOrNull()?.text
            
            text ?: "The AI returned an empty response."
            
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("GeminiError", "API Call Failed: $errorBody", e)
            "AI Error: ${e.message()} (See logs for details)"
        } catch (e: Exception) {
            android.util.Log.e("GeminiError", "API Call Failed", e)
            "AI service unavailable. Please check your connection or try again later. (${e.message})"
        }
    }

    suspend fun getAIResponseWithImage(prompt: String, bitmap: android.graphics.Bitmap): String {
        return try {
            val byteArrayOutputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val base64Image = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)

            val request = GeminiRequest(
                contents = listOf(
                    Content(listOf(
                        Part(text = prompt),
                        Part(inline_data = InlineData(mime_type = "image/jpeg", data = base64Image))
                    ))
                )
            )

            val response = api.generateImage(apiKey, request)

            val candidate = response.candidates?.firstOrNull()
            val text = candidate?.content?.parts?.firstOrNull()?.text

            text ?: "The AI returned an empty response."

        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("GeminiError", "API Call Failed: $errorBody", e)
            "AI Error: ${e.message()} (See logs for details)"
        } catch (e: Exception) {
            android.util.Log.e("GeminiError", "API Call Failed", e)
            "AI service unavailable. Please check your connection or try again later. (${e.message})"
        }
    }
}
