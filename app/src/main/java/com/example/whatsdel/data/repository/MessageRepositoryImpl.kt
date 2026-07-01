package com.example.whatsdel.data.repository

import com.example.whatsdel.data.dao.EditHistoryDao
import com.example.whatsdel.data.dao.MessageDao
import com.example.whatsdel.data.entity.MessageEditHistoryEntity
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val editHistoryDao: EditHistoryDao
) : MessageRepository {

    override fun observeActiveMessages(): Flow<List<MessageEntity>> =
        messageDao.observeActiveMessages()

    override fun observeDeletedMessages(): Flow<List<MessageEntity>> =
        messageDao.observeDeletedMessages()

    override fun getEditedMessages(): Flow<List<MessageEntity>> =
        messageDao.getEditedMessages()

    override fun getMediaMessages(): Flow<List<MessageEntity>> =
        messageDao.getMediaMessages()

    override fun getMessageCount(): Flow<Int> =
        messageDao.getMessageCount()

    override fun getDeletedCount(): Flow<Int> =
        messageDao.getDeletedCount()

    override fun getEditedCount(): Flow<Int> =
        messageDao.getEditedCount()

    override fun getMediaCount(): Flow<Int> =
        messageDao.getMediaCount()

    // Phase 4: Media
    override fun observeAllMedia(): Flow<List<MessageEntity>> =
        messageDao.observeAllMedia()

    override fun observeMediaByType(type: String): Flow<List<MessageEntity>> =
        messageDao.observeMediaByType(type)

    override fun getImageCount(): Flow<Int> =
        messageDao.getImageCount()

    override fun getVideoCount(): Flow<Int> =
        messageDao.getVideoCount()

    override fun getAudioCount(): Flow<Int> =
        messageDao.getAudioCount()

    override fun getStickerCount(): Flow<Int> =
        messageDao.getStickerCount()

    override fun searchMedia(query: String): Flow<List<MessageEntity>> =
        messageDao.searchMedia(query)

    override suspend fun insertMessage(message: MessageEntity): Long =
        messageDao.insertMessage(message)

    override suspend fun insertMessages(messages: List<MessageEntity>) =
        messageDao.insertMessages(messages)

    override suspend fun updateMessage(message: MessageEntity) =
        messageDao.updateMessage(message)

    override suspend fun deleteMessage(message: MessageEntity) =
        messageDao.deleteMessage(message)

    override suspend fun deleteAllMessages() =
        messageDao.deleteAllMessages()

    override fun searchActiveMessages(query: String): Flow<List<MessageEntity>> =
        messageDao.searchActiveMessages(query)

    override fun searchDeletedMessages(query: String): Flow<List<MessageEntity>> =
        messageDao.searchDeletedMessages(query)

    override fun searchEditedMessages(query: String): Flow<List<MessageEntity>> =
        messageDao.searchEditedMessages(query)

    override suspend fun findMatchingMessage(chatName: String): MessageEntity? =
        messageDao.findMatchingMessage(chatName)

    override suspend fun getRecentActiveMessages(): List<MessageEntity> =
        messageDao.getRecentActiveMessages()

    override suspend fun findMessageByNotificationId(notificationId: Int): MessageEntity? =
        messageDao.findMessageByNotificationId(notificationId)

    override suspend fun findMessageBySenderAndNotificationId(sender: String, notificationId: Int): MessageEntity? =
        messageDao.findMessageBySenderAndNotificationId(sender, notificationId)

    override suspend fun findMessageByExactTimestamp(chatName: String, sender: String, timestamp: Long): MessageEntity? =
        messageDao.findMessageByExactTimestamp(chatName, sender, timestamp)

    override suspend fun markAsDeleted(id: Long, deletedTimestamp: Long, isDeleted: Boolean) =
        messageDao.markAsDeleted(id, deletedTimestamp, isDeleted)

    override suspend fun markMessageEdited(id: Long, editedAt: Long, newText: String, originalText: String) =
        messageDao.markMessageEdited(id, editedAt, newText, originalText)

    override suspend fun getMessageById(id: Long): MessageEntity? =
        messageDao.getMessageById(id)

    // Phase 4: Edit History
    override suspend fun insertEditHistory(entry: MessageEditHistoryEntity): Long =
        editHistoryDao.insertEditHistory(entry)

    override fun observeEditHistory(messageId: Long): Flow<List<MessageEditHistoryEntity>> =
        editHistoryDao.observeEditHistory(messageId)

    override suspend fun getEditHistory(messageId: Long): List<MessageEditHistoryEntity> =
        editHistoryDao.getEditHistory(messageId)

    override suspend fun countDuplicateEdits(messageId: Long, newText: String): Int =
        editHistoryDao.countDuplicateEdits(messageId, newText)
}
