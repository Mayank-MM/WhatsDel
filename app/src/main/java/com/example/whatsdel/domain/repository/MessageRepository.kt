package com.example.whatsdel.domain.repository

import com.example.whatsdel.data.entity.MessageEditHistoryEntity
import com.example.whatsdel.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    fun observeActiveMessages(): Flow<List<MessageEntity>>

    fun observeDeletedMessages(): Flow<List<MessageEntity>>

    fun getEditedMessages(): Flow<List<MessageEntity>>

    fun getMediaMessages(): Flow<List<MessageEntity>>

    fun getMessageCount(): Flow<Int>

    fun getDeletedCount(): Flow<Int>

    fun getEditedCount(): Flow<Int>

    fun getMediaCount(): Flow<Int>

    // Phase 4: Media
    fun observeAllMedia(): Flow<List<MessageEntity>>

    fun observeMediaByType(type: String): Flow<List<MessageEntity>>

    fun getImageCount(): Flow<Int>

    fun getVideoCount(): Flow<Int>

    fun getAudioCount(): Flow<Int>

    fun getStickerCount(): Flow<Int>

    fun searchMedia(query: String): Flow<List<MessageEntity>>

    suspend fun insertMessage(message: MessageEntity): Long

    suspend fun insertMessages(messages: List<MessageEntity>)

    suspend fun updateMessage(message: MessageEntity)

    suspend fun deleteMessage(message: MessageEntity)

    suspend fun deleteAllMessages()

    fun searchActiveMessages(query: String): Flow<List<MessageEntity>>

    fun searchDeletedMessages(query: String): Flow<List<MessageEntity>>

    fun searchEditedMessages(query: String): Flow<List<MessageEntity>>

    suspend fun findMatchingMessage(chatName: String): MessageEntity?

    suspend fun getRecentActiveMessages(): List<MessageEntity>

    suspend fun findMessageByNotificationId(notificationId: Int): MessageEntity?

    suspend fun findMessageBySenderAndNotificationId(sender: String, notificationId: Int): MessageEntity?

    suspend fun markAsDeleted(id: Long, deletedTimestamp: Long, isDeleted: Boolean = true)

    suspend fun markMessageEdited(id: Long, editedAt: Long, newText: String, originalText: String)

    suspend fun getMessageById(id: Long): MessageEntity?

    // Phase 4: Edit History
    suspend fun insertEditHistory(entry: MessageEditHistoryEntity): Long

    fun observeEditHistory(messageId: Long): Flow<List<MessageEditHistoryEntity>>

    suspend fun getEditHistory(messageId: Long): List<MessageEditHistoryEntity>

    suspend fun countDuplicateEdits(messageId: Long, newText: String): Int
}
