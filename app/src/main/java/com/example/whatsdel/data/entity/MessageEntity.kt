package com.example.whatsdel.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
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
    val thumbnailPath: String? = null
)
