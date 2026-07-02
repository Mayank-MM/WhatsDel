package com.example.whatsdel.utils

import android.app.Notification
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat

object NotificationLogger {
    private const val TAG = "WhatsDelMedia"

    /**
     * Dumps all useful information from a WhatsApp notification.
     * Use sparingly (only when raw logging is enabled).
     */
    fun dumpNotification(sbn: StatusBarNotification, isLoggingEnabled: Boolean) {
        if (!isLoggingEnabled) return

        try {
            val notification = sbn.notification
            val extras = notification.extras

            Log.d(TAG, "--- START NOTIFICATION DUMP ---")
            Log.d(TAG, "ID: ${sbn.id} | PostTime: ${sbn.postTime} | Key: ${sbn.key}")
            
            // Dump basic extras
            if (extras != null) {
                for (key in extras.keySet()) {
                    val value = extras.get(key)
                    // Avoid logging raw bitmaps to logcat (too large)
                    if (value is android.graphics.Bitmap) {
                        Log.d(TAG, "Extra [$key]: Bitmap(${value.width}x${value.height})")
                    } else if (value is android.graphics.drawable.Icon) {
                        Log.d(TAG, "Extra [$key]: Icon")
                    } else if (value is Array<*>) {
                        Log.d(TAG, "Extra [$key]: Array[${value.size}] -> ${value.joinToString { it?.toString() ?: "null" }}")
                    } else {
                        Log.d(TAG, "Extra [$key]: $value")
                    }
                }
            }

            // Dump MessagingStyle explicitly
            val messagingStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
            if (messagingStyle != null) {
                Log.d(TAG, "MessagingStyle Found! Title: ${messagingStyle.conversationTitle}")
                messagingStyle.messages.forEachIndexed { index, msg ->
                    Log.d(TAG, "  Message[$index]: sender='${msg.person?.name}', text='${msg.text}', time=${msg.timestamp}")
                    Log.d(TAG, "    -> dataUri: ${msg.dataUri}, mimeType: ${msg.dataMimeType}")
                }
            } else {
                Log.d(TAG, "No MessagingStyle found.")
            }

            // Dump actions
            if (notification.actions != null) {
                Log.d(TAG, "Actions: ${notification.actions.size}")
                notification.actions.forEachIndexed { index, action ->
                    Log.d(TAG, "  Action[$index]: title='${action.title}'")
                }
            }

            Log.d(TAG, "--- END NOTIFICATION DUMP ---")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dump notification", e)
        }
    }

    fun logError(message: String, e: Throwable? = null) {
        Log.e(TAG, message, e)
    }

    fun logInfo(message: String) {
        Log.d(TAG, message)
    }
}
