package com.cricsaga.app.ui.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.cricsaga.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val downloadUrl: String,
    val changelog: String,
    val isForceUpdate: Boolean = false,
    val isUpdateAvailable: Boolean = false,
    val sha256Checksum: String? = null
)

object UpdateManager {

    /**
     * Checks if a new version is available by fetching the version manifest JSON.
     * Can query a public raw URL on GitHub Releases, Cloudflare Pages, or a backend API.
     */
    suspend fun checkForUpdates(
        context: Context? = null,
        manifestUrl: String = "https://wyycmghdkcgdmoaaleki.supabase.co/storage/v1/object/public/app-updates/version.json"
    ): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val currentCode = if (context != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getPackageInfo(context.packageName, 0).versionCode
                    }
                } catch (_: Exception) {
                    BuildConfig.VERSION_CODE
                }
            } else {
                BuildConfig.VERSION_CODE
            }

            val url = URL(manifestUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()
                val json = JSONObject(response)

                val latestVersionCode = when {
                    json.has("latest_version_code") -> json.getInt("latest_version_code")
                    json.has("version_code") -> json.getInt("version_code")
                    else -> currentCode
                }
                val latestVersionName = when {
                    json.has("latest_version_name") -> json.getString("latest_version_name")
                    json.has("version_name") -> json.getString("version_name")
                    else -> BuildConfig.VERSION_NAME
                }
                val downloadUrl = when {
                    json.has("apk_url") -> json.getString("apk_url")
                    json.has("download_url") -> json.getString("download_url")
                    else -> ""
                }
                val changelog = when {
                    json.has("changelog") -> json.getString("changelog")
                    json.has("release_notes") -> json.getString("release_notes")
                    else -> "Performance improvements and bug fixes."
                }
                val isForceUpdate = json.optBoolean("is_force_update", false)
                val sha256 = json.optString("sha256", "").takeIf { it.isNotBlank() }

                val isUpdateAvailable = latestVersionCode > currentCode

                Result.success(
                    AppUpdateInfo(
                        latestVersionCode = latestVersionCode,
                        latestVersionName = latestVersionName,
                        downloadUrl = downloadUrl,
                        changelog = changelog,
                        isForceUpdate = isForceUpdate,
                        isUpdateAvailable = isUpdateAvailable,
                        sha256Checksum = sha256
                    )
                )
            } else {
                Result.failure(Exception("HTTP error ${connection.responseCode} while fetching version info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Computes the SHA-256 checksum of a local file.
     */
    fun calculateFileSha256(file: File): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Downloads and installs the APK from the specified download URL using Android DownloadManager.
     */
    fun downloadAndInstallApk(
        context: Context,
        apkUrl: String,
        versionName: String,
        expectedSha256: String? = null
    ) {
        try {
            val appContext = context.applicationContext
            val fileName = "CricSaga_v${versionName}.apk"
            val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(apkUrl)

            val request = DownloadManager.Request(uri).apply {
                setTitle("CricSaga Update v$versionName")
                setDescription("Downloading latest CricSaga update...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalFilesDir(appContext, Environment.DIRECTORY_DOWNLOADS, fileName)
                setMimeType("application/vnd.android.package-archive")
            }

            val downloadId = downloadManager.enqueue(request)

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(receiverContext: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                    if (id == downloadId) {
                        try {
                            appContext.unregisterReceiver(this)
                        } catch (_: Exception) {}

                        val file = File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                        if (file.exists() && file.length() > 0) {
                            launchPackageInstaller(appContext, file, expectedSha256)
                        }
                    }
                }
            }

            ContextCompat.registerReceiver(
                appContext,
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Prompts the system package installer to install the downloaded APK with integrity verification.
     */
    fun launchPackageInstaller(context: Context, apkFile: File, expectedSha256: String? = null) {
        if (!apkFile.exists() || apkFile.length() <= 0) {
            android.util.Log.e("UpdateManager", "APK file does not exist or is empty.")
            return
        }

        if (!expectedSha256.isNullOrBlank()) {
            val actualHash = calculateFileSha256(apkFile)
            if (!actualHash.equals(expectedSha256.trim(), ignoreCase = true)) {
                android.util.Log.e("UpdateManager", "APK integrity check failed! Expected: $expectedSha256, Actual: $actualHash")
                try {
                    apkFile.delete()
                } catch (_: Exception) {}
                return
            }
        } else {
            // Graceful fallback: Verify that the APK package archive is valid and matches our package name
            val archiveInfo = context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            if (archiveInfo == null || archiveInfo.packageName != context.packageName) {
                android.util.Log.e("UpdateManager", "APK verification failed: invalid archive or package name mismatch (${archiveInfo?.packageName})")
                try {
                    apkFile.delete()
                } catch (_: Exception) {}
                return
            }
            android.util.Log.w("UpdateManager", "No expected SHA-256 provided. Proceeding after verifying package: ${archiveInfo.packageName}")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                return
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(installIntent)
    }
}
