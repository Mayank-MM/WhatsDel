package com.example.whatsdel.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val conversationId: Long = 0,
    val conversationName: String,
    val senderIdentifier: String,
    val profileImageUri: String? = null,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val messageCount: Int = 1,
    val isGroup: Boolean = false,
    val unreadCount: Int = 0,
    val hasDeletedMessages: Boolean = false,
    val hasEditedMessages: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
