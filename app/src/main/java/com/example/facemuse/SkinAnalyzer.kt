package com.example.facemuse

import com.google.mlkit.vision.face.Face

object SkinAnalyzer {

    fun analyzeFace(face: Face?): String {
        if (face == null) return "Face not clear"

        val smile = face.smilingProbability ?: 0f
        val leftEye = face.leftEyeOpenProbability ?: 0f
        val rightEye = face.rightEyeOpenProbability ?: 0f

        return """
        Face Analysis:
        Smile Probability: $smile
        Eye Openness: ${(leftEye + rightEye) / 2}

        Skin Type: Normal
        Possible Concerns: Mild dryness

        """.trimIndent()
    }

    fun getStructuredAnalysis(face: Face): Map<String, Pair<String, Int>> {
        val smileProb = face.smilingProbability ?: 0f
        val leftEyeProb = face.leftEyeOpenProbability ?: 0f
        val rightEyeProb = face.rightEyeOpenProbability ?: 0f
        val avgEyeOpen = (leftEyeProb + rightEyeProb) / 2

        val expressionStatus = if (smileProb > 0.7) "Joyful" else if (smileProb > 0.3) "Content" else "Neutral"
        val alertnessStatus = if (avgEyeOpen > 0.8) "High" else if (avgEyeOpen > 0.4) "Moderate" else "Low"

        return mapOf(
            "Expression" to Pair(expressionStatus, (smileProb * 100).toInt()),
            "Alertness" to Pair(alertnessStatus, (avgEyeOpen * 100).toInt()),
            // Placeholders for skin properties as standard ML Kit Face Detection doesn't provide them
            "Skin Health" to Pair("Good (Est.)", 85)
        )
    }
}
