package com.cricsaga.app.ui.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cricsaga.app.R

object NotificationHelper {

    const val CHANNEL_MATCH_EVENTS = "cricsaga_match_events"
    const val CHANNEL_COMMUNITY = "cricsaga_community"

    /**
     * Initializes Android system notification channels.
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val matchEventsChannel = NotificationChannel(
                CHANNEL_MATCH_EVENTS,
                "Match Events & Results",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Live match milestones, fall of wickets, innings breaks, and victory alerts"
                enableVibration(true)
            }

            val communityChannel = NotificationChannel(
                CHANNEL_COMMUNITY,
                "Community & Squad Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Responses to match requirements, team rosters, and turf updates"
            }

            notificationManager.createNotificationChannels(listOf(matchEventsChannel, communityChannel))
        }
    }

    /**
     * Dispatches an Android system notification for high-salience match events.
     */
    fun sendMatchEventNotification(context: Context, title: String, message: String, notificationId: Int = 101) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_MATCH_EVENTS)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Handled when POST_NOTIFICATIONS runtime permission is not granted on Android 13+
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Dispatches an Android system notification for community postings.
     */
    fun sendCommunityNotification(context: Context, title: String, message: String, notificationId: Int = 102) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_COMMUNITY)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
