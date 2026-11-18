package com.example.ui.util

import android.content.Context
import android.net.Uri
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PhotoStorageHelper {
  suspend fun saveProfilePhoto(context: Context, sourceUri: Uri): String? =
    withContext(Dispatchers.IO) {
      try {
        val photosDir = File(context.filesDir, "player_photos").apply { mkdirs() }
        val destFile = File(photosDir, "player_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
          destFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return@withContext null
        Uri.fromFile(destFile).toString()
      } catch (_: Exception) {
        null
      }
    }
}
