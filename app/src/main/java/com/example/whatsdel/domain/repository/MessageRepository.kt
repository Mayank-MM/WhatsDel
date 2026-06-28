package com.example.whatsdel.domain.repository

import com.example.whatsdel.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    fun getAllMessages(): Flow<List<MessageEntity>>

    fun getDeletedMessages(): Flow<List<MessageEntity>>

    fun getEditedMessages(): Flow<List<MessageEntity>>

    fun getMediaMessages(): Flow<List<MessageEntity>>

    fun getMessageCount(): Flow<Int>

    fun getDeletedCount(): Flow<Int>

    fun getEditedCount(): Flow<Int>

    fun getMediaCount(): Flow<Int>

    suspend fun insertMessage(message: MessageEntity): Long

    suspend fun insertMessages(messages: List<MessageEntity>)

    suspend fun updateMessage(message: MessageEntity)

    suspend fun deleteMessage(message: MessageEntity)

    suspend fun deleteAllMessages()

    fun searchMessages(query: String): Flow<List<MessageEntity>>

    suspend fun findMatchingMessage(chatName: String): MessageEntity?

    suspend fun markAsDeleted(id: Long, deletedTimestamp: Long)
}
