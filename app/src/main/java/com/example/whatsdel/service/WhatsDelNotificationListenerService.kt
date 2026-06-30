package com.example.whatsdel.service

import android.app.Notification
import android.graphics.Bitmap
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.whatsdel.data.entity.MessageEditHistoryEntity
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.domain.repository.MessageRepository
import com.example.whatsdel.utils.MediaStorageHelper
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

    // Media detection patterns (WhatsApp uses these emoji+text combos in notifications)
    private val mediaPatterns = mapOf(
        "image" to listOf("📷 photo", "📷 image", "photo", "image"),
        "video" to listOf("📹 video", "🎥 video", "video"),
        "voice_note" to listOf("🎤 voice message", "🎵 audio", "voice message", "🎤", "ptt"),
        "sticker" to listOf("sticker")
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

        // Basic deduplication (include notificationId to allow edit detection)
        val hash = (sender + text + notificationId).hashCode()
        val lastSeen = recentMessages[hash]
        if (lastSeen != null && (postTime - lastSeen) < 5000) {
            Log.d(TAG, "Duplicate message ignored: $sender - $text")
            return
        }

        // Update cache and clean old entries
        recentMessages[hash] = postTime
        cleanOldCache(postTime)

        // Detect media type from notification content
        val detectedMediaType = detectMediaType(text, fullText)
        val hasMedia = detectedMediaType != null

        // Extract thumbnail bitmap safely (handling both Bitmap and Icon types)
        fun extractBitmapFromExtras(key: String): Bitmap? {
            return try {
                val obj = extras.get(key)
                when (obj) {
                    is Bitmap -> obj
                    is android.graphics.drawable.Icon -> {
                        val drawable = obj.loadDrawable(this@WhatsDelNotificationListenerService)
                        if (drawable is android.graphics.drawable.BitmapDrawable) {
                            drawable.bitmap
                        } else if (drawable != null) {
                            val bmp = Bitmap.createBitmap(
                                drawable.intrinsicWidth.takeIf { it > 0 } ?: 500,
                                drawable.intrinsicHeight.takeIf { it > 0 } ?: 500,
                                Bitmap.Config.ARGB_8888
                            )
                            val canvas = android.graphics.Canvas(bmp)
                            drawable.setBounds(0, 0, canvas.width, canvas.height)
                            drawable.draw(canvas)
                            bmp
                        } else null
                    }
                    else -> null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting bitmap for $key", e)
                null
            }
        }

        var mediaBitmap = extractBitmapFromExtras(Notification.EXTRA_PICTURE)

        // WhatsApp often uses MessagingStyle for images. Try to extract from dataUri if available
        if (mediaBitmap == null) {
            val messagingStyle = androidx.core.app.NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
            if (messagingStyle != null) {
                for (msg in messagingStyle.messages) {
                    val uri = msg.dataUri
                    if (uri != null) {
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                val source = android.graphics.ImageDecoder.createSource(contentResolver, uri)
                                mediaBitmap = android.graphics.ImageDecoder.decodeBitmap(source)
                            } else {
                                @Suppress("DEPRECATION")
                                mediaBitmap = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
                            }
                            if (mediaBitmap != null) break
                        } catch (e: Exception) {
                            Log.e(TAG, "Error loading image from MessagingStyle URI", e)
                        }
                    }
                }
            }
        }

        var thumbnailPath: String? = null

        if (hasMedia && mediaBitmap != null) {
            val fileName = MediaStorageHelper.generateFileName(sender, postTime, detectedMediaType!!)
            thumbnailPath = MediaStorageHelper.saveThumbnail(applicationContext, mediaBitmap, fileName)
            Log.d(TAG, "Media thumbnail saved: $thumbnailPath")
        }

        // Determine the caption (for media messages, the text is often a caption)
        val messageText = if (hasMedia) {
            // If text is just the media indicator, set message to the media type label
            val textLower = text.lowercase()
            val isOnlyIndicator = mediaPatterns.values.flatten().any { textLower == it }
            if (isOnlyIndicator) {
                "${detectedMediaType?.replaceFirstChar { it.uppercase() }} received"
            } else {
                text // It's a caption
            }
        } else {
            text
        }

        val messageEntity = MessageEntity(
            sender = sender,
            chatName = chatName,
            message = messageText,
            timestamp = postTime,
            packageName = packageName,
            notificationId = notificationId,
            messageType = if (hasMedia) "media" else "text",
            isDeleted = false,
            isEdited = false,
            hasMedia = hasMedia,
            mediaType = detectedMediaType,
            mediaUri = null,
            mediaMimeType = when (detectedMediaType) {
                "image" -> "image/jpeg"
                "video" -> "video/mp4"
                "voice_note" -> "audio/ogg"
                "sticker" -> "image/webp"
                else -> null
            },
            mediaFileName = if (hasMedia) MediaStorageHelper.generateFileName(sender, postTime, detectedMediaType!!) else null,
            mediaSize = null,
            thumbnailPath = thumbnailPath
        )

        Log.d(TAG, "Processing message: $sender in $chatName: $messageText (media: $hasMedia, type: $detectedMediaType)")

        serviceScope.launch {
            try {
                // EDIT DETECTION LOGIC
                // We check if a message already exists with this sender and notificationId
                val existingMessage = messageRepository.findMessageBySenderAndNotificationId(sender, notificationId)
                
                if (existingMessage != null) {
                    if (existingMessage.message != messageText) {
                        // The text has changed -> It's an edit!
                        val originalText = existingMessage.originalMessage ?: existingMessage.message
                        
                        Log.d(TAG, "Edit detected for $sender: '${existingMessage.message}' -> '$messageText'")
                        
                        // 1. Mark the message as edited in the main table
                        messageRepository.markMessageEdited(
                            id = existingMessage.id,
                            editedAt = postTime,
                            newText = messageText,
                            originalText = originalText
                        )
                        
                        // 2. Prevent duplicate edit histories
                        val duplicateCount = messageRepository.countDuplicateEdits(existingMessage.id, messageText)
                        if (duplicateCount == 0) {
                            // 3. Insert into edit history
                            val editHistory = MessageEditHistoryEntity(
                                messageId = existingMessage.id,
                                previousText = existingMessage.message,
                                newText = messageText,
                                editedTimestamp = postTime
                            )
                            messageRepository.insertEditHistory(editHistory)
                            Log.d(TAG, "Edit history saved.")
                        }
                    } else {
                        Log.d(TAG, "Duplicate notification update ignored for $sender (text identical)")
                    }
                    // Do not insert a new message entity if an existing one was found
                    return@launch
                }
                
                // If no existing message was found, insert it as a new message
                messageRepository.insertMessage(messageEntity)
                Log.d(TAG, "New message saved.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save or process edited message", e)
            }
        }
    }

    /**
     * Detects media type from the notification text.
     * Returns: "image", "video", "voice_note", "sticker", or null for plain text.
     */
    private fun detectMediaType(text: String, fullText: String): String? {
        val textLower = text.lowercase()

        // Check each media pattern
        for ((type, patterns) in mediaPatterns) {
            if (patterns.any { textLower.contains(it) || fullText.contains(it) }) {
                return type
            }
        }

        // Additional heuristic: WhatsApp sometimes sends "📷" alone for photos
        if (textLower.contains("📷") || textLower.contains("\uD83D\uDCF7")) return "image"
        if (textLower.contains("📹") || textLower.contains("🎥")) return "video"
        if (textLower.contains("🎤")) return "voice_note"

        return null
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
