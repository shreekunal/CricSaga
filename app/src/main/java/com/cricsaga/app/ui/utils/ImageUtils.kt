package com.cricsaga.app.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageUtils {

    /**
     * Copies and compresses a picked image Uri to internal app storage.
     * Returns the absolute path of the saved file, or null on error.
     */
    fun saveProfileImageFromUri(context: Context, uri: Uri, userId: String): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Scale down to max 1024x1024 while maintaining aspect ratio
            val maxDimension = 1024
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                val scale = maxDimension.toFloat() / max(width, height)
                val matrix = Matrix().apply { postScale(scale, scale) }
                Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, true)
            } else {
                originalBitmap
            }

            val avatarDir = File(context.filesDir, "avatars").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(avatarDir, "avatar_${userId}.jpg")

            FileOutputStream(destinationFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
            }

            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Loads a file from disk into a Compose ImageBitmap.
     */
    fun loadBitmapFromPath(path: String?): ImageBitmap? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.exists() || !file.canRead()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Loads a file from disk into an Android Bitmap (for Canvas operations).
     */
    fun loadAndroidBitmapFromPath(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.exists() || !file.canRead()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Deletes a local profile image file if requested.
     */
    fun deleteProfileImage(path: String?) {
        if (!path.isNullOrBlank()) {
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
