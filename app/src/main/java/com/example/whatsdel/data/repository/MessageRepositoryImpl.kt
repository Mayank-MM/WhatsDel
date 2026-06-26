package com.example.whatsdel.data.repository

import com.example.whatsdel.data.dao.MessageDao
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : MessageRepository {

    override fun getAllMessages(): Flow<List<MessageEntity>> =
        messageDao.getAllMessages()

    override fun getDeletedMessages(): Flow<List<MessageEntity>> =
        messageDao.getDeletedMessages()

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
}
