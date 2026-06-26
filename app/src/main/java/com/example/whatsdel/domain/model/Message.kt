package com.example.whatsdel.domain.model

data class Message(
    val id: Long = 0,
    val sender: String,
    val chatName: String,
    val message: String,
    val timestamp: Long,
    val messageType: String,
    val isDeleted: Boolean = false,
    val isEdited: Boolean = false,
    val mediaPath: String? = null
)
