package com.example.facemuse

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApi {
    @POST("v1beta/models/gemini-1.5-flash-001:generateContent")
    suspend fun generateText(
        @Query("key") apiKey: String,
        @Body body: GeminiRequest
    ): GeminiResponse

    @POST("v1beta/models/gemini-1.5-flash-001:generateContent")
    suspend fun generateImage(
        @Query("key") apiKey: String,
        @Body body: GeminiRequest
    ): GeminiResponse
}
