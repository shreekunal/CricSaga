package com.cricsaga.app.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageUtils {

    private val imageHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * Calculates an optimal power-of-two inSampleSize to protect against Out-Of-Memory (OOM) errors.
     */
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Decodes a memory-efficient subsampled Bitmap from a content Uri.
     */
    fun decodeSampledBitmapFromUri(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            if (options.outWidth <= 0 || options.outHeight <= 0) return null

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Compresses an image from Uri into WebP format (lossy, quality 75, max 300x300),
     * and returns it as a Base64 string for zero-cost cloud storage in database text columns.
     */
    fun compressUriToBase64WebP(
        context: Context,
        uri: Uri,
        maxDimension: Int = 300,
        quality: Int = 75
    ): String? {
        return try {
            val originalBitmap = decodeSampledBitmapFromUri(context, uri, maxDimension * 2, maxDimension * 2)
                ?: return null

            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                val scale = maxDimension.toFloat() / max(width, height)
                val matrix = Matrix().apply { postScale(scale, scale) }
                Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }
            scaledBitmap.compress(format, quality, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decodes a Base64 string into a Compose ImageBitmap.
     */
    fun decodeBase64ToBitmap(base64String: String?): ImageBitmap? {
        return decodeBase64ToAndroidBitmap(base64String)?.asImageBitmap()
    }

    /**
     * Decodes a Base64 string into an Android Bitmap.
     */
    fun decodeBase64ToAndroidBitmap(base64String: String?): Bitmap? {
        if (base64String.isNullOrBlank()) return null
        return try {
            val cleaned = if (base64String.contains(",")) {
                base64String.substringAfter(",")
            } else {
                base64String
            }
            val bytes = Base64.decode(cleaned, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Copies and compresses a picked image Uri to internal app storage in WebP format (< 30 KB).
     * Returns the absolute path of the saved file, or null on error.
     */
    fun saveProfileImageFromUri(context: Context, uri: Uri, userId: String): String? {
        return try {
            val originalBitmap = decodeSampledBitmapFromUri(context, uri, 600, 600)
                ?: return null

            // Scale down to max 300x300 for crisp avatars while maintaining small footprint
            val maxDimension = 300
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
            val destinationFile = File(avatarDir, "avatar_${userId}.webp")

            val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }

            FileOutputStream(destinationFile).use { out ->
                scaledBitmap.compress(format, 75, out)
            }

            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decodeUriToBitmap(context: Context, uri: Uri): ImageBitmap? {
        val uriString = uri.toString()
        if (uriString.startsWith("data:image") || (!uriString.startsWith("content://") && !uriString.startsWith("file://") && uriString.length > 100)) {
            val base64Bitmap = decodeBase64ToBitmap(uriString)
            if (base64Bitmap != null) return base64Bitmap
        }
        return try {
            val bitmap = decodeSampledBitmapFromUri(context, uri, 1080, 1080)
            bitmap?.asImageBitmap() ?: loadBitmapFromPath(uri.path)
        } catch (e: Exception) {
            loadBitmapFromPath(uri.path ?: uriString)
        }
    }

    /**
     * Loads a file from disk or Base64 string into a Compose ImageBitmap.
     */
    fun loadBitmapFromPath(path: String?): ImageBitmap? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("data:image") || (!path.startsWith("/") && path.length > 100)) {
            val base64Bitmap = decodeBase64ToBitmap(path)
            if (base64Bitmap != null) return base64Bitmap
        }
        val file = File(path)
        if (!file.exists() || !file.canRead()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Loads a file from disk or Base64 string into an Android Bitmap (for Canvas operations).
     */
    fun loadAndroidBitmapFromPath(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("data:image") || (!path.startsWith("/") && path.length > 100)) {
            val base64Bitmap = decodeBase64ToAndroidBitmap(path)
            if (base64Bitmap != null) return base64Bitmap
        }
        val file = File(path)
        if (!file.exists() || !file.canRead()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Downloads an avatar from a remote URL (such as a Google Account photo) and caches it
     * locally into internal storage as WebP. Returns the local absolute path.
     */
    suspend fun downloadAndCacheProfileImage(
        context: Context,
        imageUrl: String?,
        userId: String
    ): String? = withContext(Dispatchers.IO) {
        if (imageUrl.isNullOrBlank()) return@withContext null
        try {
            val safeUserId = userId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val avatarDir = File(context.filesDir, "avatars").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(avatarDir, "avatar_${safeUserId}.webp")

            val request = Request.Builder().url(imageUrl).build()
            imageHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val bytes = response.body?.bytes() ?: return@withContext null
                val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext null

                val maxDimension = 300
                val width = originalBitmap.width
                val height = originalBitmap.height
                val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                    val scale = maxDimension.toFloat() / max(width, height)
                    val matrix = Matrix().apply { postScale(scale, scale) }
                    Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, true)
                } else {
                    originalBitmap
                }

                val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }

                FileOutputStream(destinationFile).use { out ->
                    scaledBitmap.compress(format, 80, out)
                }

                destinationFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
