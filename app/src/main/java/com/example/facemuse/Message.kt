package com.example.facemuse

data class Message(
    val text: String,
    val isFromUser: Boolean,
    val isTyping: Boolean = false
)