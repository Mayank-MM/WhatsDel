package com.example.whatsdel.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.whatsdel.MainActivity
import com.example.whatsdel.R

object NotificationHelper {

    private const val TAG = "NotificationHelper"
    const val CHANNEL_ID = "whatsdel_deleted_messages"
    const val CHANNEL_NAME = "Deleted Message Alerts"
    const val CHANNEL_DESCRIPTION = "Alerts when a WhatsApp message is deleted"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDeletedMessageNotification(
        context: Context,
        sender: String,
        chatName: String,
        originalMessage: String,
        mediaType: String? = null,
        messageId: Long = System.currentTimeMillis()
    ) {
        createNotificationChannel(context)

        // Android 13+ runtime permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Skipping notification.")
                return
            }
        }

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Log.w(TAG, "Notifications disabled for this app. Skipping notification.")
            return
        }

        // PendingIntent to launch MainActivity directly to Deleted Messages tab
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "deleted")
            putExtra("message_id", messageId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (messageId % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (chatName.isNotBlank() && chatName != sender) {
            "Message Deleted in $chatName"
        } else {
            "Message Deleted by $sender"
        }

        val contentText = when {
            originalMessage.isNotBlank() -> {
                if (chatName.isNotBlank() && chatName != sender) {
                    "$sender: $originalMessage"
                } else {
                    originalMessage
                }
            }
            mediaType != null -> {
                "$sender deleted a $mediaType"
            }
            else -> {
                "$sender deleted a message"
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationId = (messageId % 100000).toInt().coerceAtLeast(1)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            Log.d(TAG, "Posted deleted message notification for $sender (ID: $notificationId)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post deleted message notification", e)
        }
    }
}
