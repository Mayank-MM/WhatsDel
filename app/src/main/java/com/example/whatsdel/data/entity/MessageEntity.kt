package com.example.whatsdel.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val chatName: String,
    val message: String,
    val timestamp: Long,
    val packageName: String,
    val notificationId: Int,
    val messageType: String,
    val isDeleted: Boolean = false,
    val isEdited: Boolean = false,
    val mediaPath: String? = null,
    val deletedTimestamp: Long? = null,
    // Phase 4: Media fields
    val hasMedia: Boolean = false,
    val mediaType: String? = null,       // "image", "video", "voice_note", "sticker"
    val mediaUri: String? = null,
    val mediaMimeType: String? = null,
    val mediaFileName: String? = null,
    val mediaSize: Long? = null,
    val thumbnailPath: String? = null,
    // Phase 4: Edit fields
    val originalMessage: String? = null,
    val editedAt: Long? = null,
    val updatedAt: Long? = null, // Added in v6 for media download timestamps
    // Phase 5: Media Robustness
    val localMediaPath: String? = null,
    val mediaCaptureSuccess: Boolean? = null,
    val mediaCaptureFailureReason: String? = null,
    // Phase 8: Conversation History
    val conversationId: Long? = null
)
