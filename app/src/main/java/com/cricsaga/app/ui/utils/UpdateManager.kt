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
    val isUpdateAvailable: Boolean = false
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

                val latestVersionCode = json.optInt("latest_version_code", currentCode)
                val latestVersionName = json.optString("latest_version_name", BuildConfig.VERSION_NAME)
                val downloadUrl = json.optString("apk_url", "")
                val changelog = json.optString("changelog", "Performance improvements and bug fixes.")
                val isForceUpdate = json.optBoolean("is_force_update", false)

                val isUpdateAvailable = latestVersionCode > currentCode

                Result.success(
                    AppUpdateInfo(
                        latestVersionCode = latestVersionCode,
                        latestVersionName = latestVersionName,
                        downloadUrl = downloadUrl,
                        changelog = changelog,
                        isForceUpdate = isForceUpdate,
                        isUpdateAvailable = isUpdateAvailable
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
     * Downloads and installs the APK from the specified download URL using Android DownloadManager.
     */
    fun downloadAndInstallApk(context: Context, apkUrl: String, versionName: String) {
        try {
            val fileName = "CricSaga_v${versionName}.apk"
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(apkUrl)

            val request = DownloadManager.Request(uri).apply {
                setTitle("CricSaga Update v$versionName")
                setDescription("Downloading latest CricSaga update...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                setMimeType("application/vnd.android.package-archive")
            }

            val downloadId = downloadManager.enqueue(request)

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(receiverContext: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                    if (id == downloadId) {
                        try {
                            receiverContext?.unregisterReceiver(this)
                        } catch (_: Exception) {}

                        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                        if (file.exists()) {
                            launchPackageInstaller(context, file)
                        }
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    onComplete,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_NOT_EXPORTED
                )
            } else {
                context.registerReceiver(
                    onComplete,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Prompts the system package installer to install the downloaded APK.
     */
    fun launchPackageInstaller(context: Context, apkFile: File) {
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
