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

    // Comprehensive list of WhatsApp deletion indicator texts across versions and languages
    private val deletionIndicators = listOf(
        "this message was deleted",
        "you deleted this message",
        "message was deleted",
        "deleted this message",
        // Hindi
        "यह मैसेज डिलीट कर दिया गया",
        "आपने यह मैसेज डिलीट कर दिया",
        // Spanish
        "este mensaje fue eliminado",
        "eliminaste este mensaje",
        // Portuguese
        "esta mensagem foi apagada",
        "você apagou esta mensagem"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        // Only process WhatsApp notifications
        if (packageName != "com.whatsapp" && packageName != "com.whatsapp.w4b") {
            return
        }

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Extract information safely without early returns
        val title = extras.getString(Notification.EXTRA_TITLE)?.trim() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""

        // Also extract from EXTRA_TEXT_LINES as WhatsApp sometimes buries it there
        val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        val textLinesString = textLines?.joinToString("\n") ?: ""
        
        val fullText = "$text\n$textLinesString".lowercase()
        val titleLower = title.lowercase()

        // WhatsApp groups usually have the group name in EXTRA_CONVERSATION_TITLE or combined in title
        val conversationTitle = extras.getString(Notification.EXTRA_CONVERSATION_TITLE)?.trim()
        val isGroup = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false)
        val isSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0

        // Parse sender and chat name accurately
        val sender: String
        val chatName: String

        if (isGroup) {
            chatName = conversationTitle ?: title.substringBefore(":").trim()
            sender = if (conversationTitle != null) {
                title
            } else {
                title.substringAfter(":").trim()
            }
        } else {
            sender = title
            chatName = title
        }

        val notificationId = sbn.id

        // Check if the text OR title indicates a deleted message
        val isDeletedText = deletionIndicators.any { fullText.contains(it) || titleLower.contains(it) }
        val isDeletedFuzzy = (fullText.contains("deleted") && fullText.contains("message")) || 
                             (fullText.contains("डिलीट") && fullText.contains("मैसेज"))

        if (isDeletedText || isDeletedFuzzy) {
            Log.d(TAG, "Deletion detected for chat: $chatName")
            handleDeletion(chatName, fullText, notificationId)
            return
        }

        // Ignore summary notifications for regular messages
        if (isSummary || text.isBlank()) {
            return
        }

        val postTime = sbn.postTime

        // Basic deduplication
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

    private fun handleDeletion(chatName: String, fullText: String, notificationId: Int) {
        serviceScope.launch {
            try {
                // METHOD 1: Try exact match via Notification ID (Bulletproof for updates)
                var matchingMessage = messageRepository.findMessageByNotificationId(notificationId)

                // METHOD 2: Try exact/LIKE match by chatName
                if (matchingMessage == null) {
                    matchingMessage = messageRepository.findMatchingMessage(chatName)
                }

                // METHOD 3: Fuzzy fallback scanning recent messages
                if (matchingMessage == null) {
                    val recentMessagesList = messageRepository.getRecentActiveMessages()
                    matchingMessage = recentMessagesList.firstOrNull { 
                        fullText.contains(it.sender.lowercase()) || fullText.contains(it.chatName.lowercase())
                    }
                }

                if (matchingMessage != null) {
                    messageRepository.markAsDeleted(
                        id = matchingMessage.id,
                        deletedTimestamp = System.currentTimeMillis(),
                        isDeleted = true
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
            if (currentTime - entry.value > 30000) {
                iterator.remove()
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
    }
}
