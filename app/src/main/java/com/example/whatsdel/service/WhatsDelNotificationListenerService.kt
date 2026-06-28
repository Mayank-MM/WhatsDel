package com.example.whatsdel.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.domain.repository.MessageRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WhatsDelNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var messageRepository: MessageRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val TAG = "WhatsDelService"

    // Deduplication cache: hash of (sender + text) -> postTime
    private val recentMessages = mutableMapOf<Int, Long>()

    // Known WhatsApp deletion indicator texts
    private val deletionIndicators = listOf(
        "this message was deleted",
        "this message was deleted.",
        "you deleted this message",
        "you deleted this message.",
        "this message has been deleted",
        // Hindi
        "यह मैसेज डिलीट कर दिया गया",
        "आपने यह मैसेज डिलीट कर दिया"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        // Only process WhatsApp notifications
        if (packageName != "com.whatsapp" && packageName != "com.whatsapp.w4b") {
            return
        }

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Extract information
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        // WhatsApp groups usually have the group name in EXTRA_CONVERSATION_TITLE or combined in title
        val conversationTitle = extras.getString(Notification.EXTRA_CONVERSATION_TITLE)
        val isGroup = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false)
        val isSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0

        // Ignore summary notifications as they don't contain individual message text
        if (isSummary || text.isBlank()) {
            return
        }

        // Parse sender and chat name
        val sender: String
        val chatName: String

        if (isGroup) {
            chatName = conversationTitle ?: title.substringBefore(":")
            sender = if (conversationTitle != null) {
                title
            } else {
                title.substringAfter(":").trim()
            }
        } else {
            sender = title
            chatName = title
        }

        // Check if the text indicates a deleted message
        val normalizedText = text.trim().lowercase()
        if (deletionIndicators.any { normalizedText.contains(it) }) {
            Log.d(TAG, "Deletion detected for chat: $chatName")
            handleDeletion(chatName)
            return
        }

        val postTime = sbn.postTime
        val notificationId = sbn.id

        // Basic deduplication: check if we've seen this exact text from this sender recently (within 5 seconds)
        val hash = (sender + text).hashCode()
        val lastSeen = recentMessages[hash]
        if (lastSeen != null && (postTime - lastSeen) < 5000) {
            Log.d(TAG, "Duplicate message ignored: $sender - $text")
            return
        }

        // Update cache and clean old entries
        recentMessages[hash] = postTime
        cleanOldCache(postTime)

        val messageEntity = MessageEntity(
            sender = sender,
            chatName = chatName,
            message = text,
            timestamp = postTime,
            packageName = packageName,
            notificationId = notificationId,
            messageType = "text",
            isDeleted = false,
            isEdited = false
        )

        Log.d(TAG, "Saving message: $sender in $chatName: $text")

        serviceScope.launch {
            try {
                messageRepository.insertMessage(messageEntity)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save message", e)
            }
        }
    }

    private fun handleDeletion(chatName: String) {
        serviceScope.launch {
            try {
                val matchingMessage = messageRepository.findMatchingMessage(chatName)
                if (matchingMessage != null) {
                    messageRepository.markAsDeleted(
                        id = matchingMessage.id,
                        deletedTimestamp = System.currentTimeMillis()
                    )
                    Log.d(TAG, "Message marked as deleted: ${matchingMessage.message}")
                } else {
                    Log.d(TAG, "No matching message found for deletion in chat: $chatName")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle deletion", e)
            }
        }
    }

    private fun cleanOldCache(currentTime: Long) {
        val iterator = recentMessages.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            // Remove entries older than 30 seconds
            if (currentTime - entry.value > 30000) {
                iterator.remove()
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        // WhatsApp posts replacement notifications for deletions rather than just removing,
        // so detection is handled in onNotificationPosted above.
    }
}
