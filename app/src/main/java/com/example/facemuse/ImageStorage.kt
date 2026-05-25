package com.example.facemuse

import android.content.Context
import android.net.Uri
import java.io.File

object ImageStorage {

    fun save(context: Context, uri: Uri): String {
        val input = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "${System.currentTimeMillis()}.jpg")
        file.outputStream().use { input?.copyTo(it) }
        return file.absolutePath
    }
}
