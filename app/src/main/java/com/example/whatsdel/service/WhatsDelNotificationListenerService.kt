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

        // Extract basic information
        val title = extras.getString(Notification.EXTRA_TITLE)?.trim() ?: ""
        val isGroup = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false)
        val conversationTitle = extras.getString(Notification.EXTRA_CONVERSATION_TITLE)?.trim()
        val isSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0

        // Parse default sender and chat name
        val defaultSender: String
        val chatName: String

        if (isGroup) {
            chatName = conversationTitle ?: title.substringBefore(":").trim()
            defaultSender = if (conversationTitle != null) {
                title
            } else {
                title.substringAfter(":").trim()
            }
        } else {
            defaultSender = title
            chatName = title
        }

        val notificationId = sbn.id

        // Ignore summary notifications for regular messages processing to avoid duplicates
        if (isSummary) {
            return
        }

        // Extract thumbnail bitmap safely from extras as a fallback
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

        val fallbackBitmap = extractBitmapFromExtras(Notification.EXTRA_PICTURE)

        val messagingStyle = androidx.core.app.NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)

        if (messagingStyle != null && messagingStyle.messages.isNotEmpty()) {
            // Process each message in the conversation history provided by MessagingStyle
            for (msg in messagingStyle.messages) {
                val msgSender = msg.person?.name?.toString() ?: defaultSender
                val msgText = msg.text?.toString()?.trim() ?: ""
                val msgTimestamp = msg.timestamp
                val msgDataUri = msg.dataUri

                processSingleMessage(
                    chatName = chatName,
                    sender = msgSender,
                    rawText = msgText,
                    timestamp = msgTimestamp,
                    notificationId = notificationId,
                    packageName = packageName,
                    dataUri = msgDataUri,
                    fallbackBitmap = fallbackBitmap
                )
            }
        } else {
            // Fallback for notifications that do not use MessagingStyle
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
            val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            val textLinesString = textLines?.joinToString("\n") ?: ""
            val fullText = if (textLinesString.isNotBlank()) "$text\n$textLinesString" else text
            
            processSingleMessage(
                chatName = chatName,
                sender = defaultSender,
                rawText = fullText,
                timestamp = sbn.postTime,
                notificationId = notificationId,
                packageName = packageName,
                dataUri = null,
                fallbackBitmap = fallbackBitmap
            )
        }
    }

    private fun processSingleMessage(
        chatName: String,
        sender: String,
        rawText: String,
        timestamp: Long,
        notificationId: Int,
        packageName: String,
        dataUri: android.net.Uri?,
        fallbackBitmap: Bitmap?
    ) {
        val fullTextLower = rawText.lowercase()
        
        // 1. Check for deletion
        val isDeletedText = deletionIndicators.any { fullTextLower.contains(it) }
        val isDeletedFuzzy = (fullTextLower.contains("deleted") && fullTextLower.contains("message")) || 
                             (fullTextLower.contains("डिलीट") && fullTextLower.contains("मैसेज"))

        if (isDeletedText || isDeletedFuzzy) {
            handleDeletion(chatName, sender, rawText, notificationId, timestamp)
            return
        }

        if (rawText.isBlank() && dataUri == null && fallbackBitmap == null) return

        serviceScope.launch {
            try {
                // Determine media type
                val detectedMediaType = detectMediaType(rawText, rawText)
                val hasMedia = detectedMediaType != null || dataUri != null || fallbackBitmap != null
                
                var finalMediaType = detectedMediaType
                if (finalMediaType == null && dataUri != null) {
                    finalMediaType = "image"
                } else if (finalMediaType == null && fallbackBitmap != null) {
                    finalMediaType = "image"
                }

                // 2. Exact lookup by (chatName, sender, timestamp)
                val existingMessage = messageRepository.findMessageByExactTimestamp(chatName, sender, timestamp)
                
                if (existingMessage != null) {
                    // Check if it's an edit (text changed)
                    if (existingMessage.message != rawText && rawText.isNotBlank()) {
                        val originalText = existingMessage.originalMessage ?: existingMessage.message
                        Log.d(TAG, "Edit detected for $sender at $timestamp: '${existingMessage.message}' -> '$rawText'")
                        
                        messageRepository.markMessageEdited(
                            id = existingMessage.id,
                            editedAt = System.currentTimeMillis(),
                            newText = rawText,
                            originalText = originalText
                        )
                        
                        val duplicateCount = messageRepository.countDuplicateEdits(existingMessage.id, rawText)
                        if (duplicateCount == 0) {
                            val editHistory = MessageEditHistoryEntity(
                                messageId = existingMessage.id,
                                previousText = existingMessage.message,
                                newText = rawText,
                                editedTimestamp = System.currentTimeMillis()
                            )
                            messageRepository.insertEditHistory(editHistory)
                        }
                    } else if (existingMessage.hasMedia && existingMessage.updatedAt == null) {
                        // Mark media as updated (download likely finished)
                        messageRepository.updateMessage(existingMessage.copy(updatedAt = System.currentTimeMillis()))
                    } else {
                        Log.d(TAG, "Duplicate message ignored (exact match): $sender at $timestamp")
                    }
                    return@launch
                }
                
                // 3. New Message - Extract Media if present
                var mediaBitmap = fallbackBitmap
                if (mediaBitmap == null && dataUri != null) {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            val source = android.graphics.ImageDecoder.createSource(contentResolver, dataUri)
                            mediaBitmap = android.graphics.ImageDecoder.decodeBitmap(source)
                        } else {
                            @Suppress("DEPRECATION")
                            mediaBitmap = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, dataUri)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading image from dataUri", e)
                    }
                }
                
                var thumbnailPath: String? = null
                if (hasMedia && mediaBitmap != null && finalMediaType != null) {
                    val fileName = MediaStorageHelper.generateFileName(sender, timestamp, finalMediaType)
                    thumbnailPath = MediaStorageHelper.saveThumbnail(applicationContext, mediaBitmap, fileName)
                }

                val messageText = if (hasMedia) {
                    val textLower = rawText.lowercase()
                    val isOnlyIndicator = mediaPatterns.values.flatten().any { textLower == it }
                    if (isOnlyIndicator && finalMediaType != null) {
                        "${finalMediaType.replaceFirstChar { it.uppercase() }} received"
                    } else {
                        rawText 
                    }
                } else {
                    rawText
                }

                val messageEntity = MessageEntity(
                    sender = sender,
                    chatName = chatName,
                    message = messageText,
                    timestamp = timestamp,
                    packageName = packageName,
                    notificationId = notificationId,
                    messageType = if (hasMedia) "media" else "text",
                    isDeleted = false,
                    isEdited = false,
                    hasMedia = hasMedia,
                    mediaType = finalMediaType,
                    mediaUri = null,
                    mediaMimeType = when (finalMediaType) {
                        "image" -> "image/jpeg"
                        "video" -> "video/mp4"
                        "voice_note" -> "audio/ogg"
                        "sticker" -> "image/webp"
                        else -> null
                    },
                    mediaFileName = if (hasMedia && finalMediaType != null) MediaStorageHelper.generateFileName(sender, timestamp, finalMediaType) else null,
                    mediaSize = null,
                    thumbnailPath = thumbnailPath,
                    updatedAt = if (hasMedia && mediaBitmap != null) System.currentTimeMillis() else null
                )

                messageRepository.insertMessage(messageEntity)
                Log.d(TAG, "New message saved: $sender at $timestamp")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process message", e)
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

    private fun handleDeletion(chatName: String, sender: String, fullText: String, notificationId: Int, timestamp: Long) {
        serviceScope.launch {
            try {
                // METHOD 1: Try exact match via Timestamp (Bulletproof for MessagingStyle)
                var matchingMessage = messageRepository.findMessageByExactTimestamp(chatName, sender, timestamp)

                // METHOD 2: Fuzzy fallback scanning recent messages
                if (matchingMessage == null) {
                    val recentMessagesList = messageRepository.getRecentActiveMessages()
                    matchingMessage = recentMessagesList.firstOrNull { 
                        it.chatName == chatName && (fullText.contains(it.sender.lowercase()) || fullText.contains(it.chatName.lowercase()))
                    }
                }

                if (matchingMessage != null && !matchingMessage.isDeleted) {
                    messageRepository.markAsDeleted(
                        id = matchingMessage.id,
                        deletedTimestamp = System.currentTimeMillis(),
                        isDeleted = true
                    )
                    Log.d(TAG, "Message marked as deleted: ${matchingMessage.message}")
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
