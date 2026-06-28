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
    val mediaPath: String? = null
)
