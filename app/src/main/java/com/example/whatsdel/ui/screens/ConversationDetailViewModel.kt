package com.example.whatsdel.ui.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsdel.data.entity.ConversationEntity
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.domain.repository.MessageRepository
import com.example.whatsdel.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationDetailUiState(
    val conversation: ConversationEntity? = null,
    val messages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationDetailViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val conversationId: Long = savedStateHandle.get<Long>("conversationId") ?: -1L

    private val _conversation = MutableStateFlow<ConversationEntity?>(null)
    val conversation: StateFlow<ConversationEntity?> = _conversation.asStateFlow()

    companion object {
        fun getSampleMessagesForConversation(convName: String): List<MessageEntity> {
            val now = System.currentTimeMillis()
            val senderName = convName.ifBlank { "Rahul Sharma" }
            return listOf(
                MessageEntity(
                    id = 1001,
                    sender = senderName,
                    chatName = convName,
                    message = "Hey! Good morning. Are you available for a quick chat?",
                    timestamp = now - 86_400_000 * 2,
                    packageName = "com.whatsapp",
                    notificationId = 1,
                    messageType = "text"
                ),
                MessageEntity(
                    id = 1002,
                    sender = senderName,
                    chatName = convName,
                    message = "Let me send you the project document file.",
                    timestamp = now - 86_400_000 * 2 + 120_000,
                    packageName = "com.whatsapp",
                    notificationId = 2,
                    messageType = "document",
                    hasMedia = true,
                    mediaType = "document",
                    mediaFileName = "Project_Requirements_v2.pdf",
                    mediaSize = 2_450_000L
                ),
                MessageEntity(
                    id = 1003,
                    sender = senderName,
                    chatName = convName,
                    message = "Check out this screenshot from yesterday's meeting.",
                    timestamp = now - 86_400_000,
                    packageName = "com.whatsapp",
                    notificationId = 3,
                    messageType = "media",
                    hasMedia = true,
                    mediaType = "image",
                    mediaFileName = "meeting_screenshot.jpg"
                ),
                MessageEntity(
                    id = 1004,
                    sender = senderName,
                    chatName = convName,
                    message = "Wait, let me correct the meeting time: 10:30 AM instead of 10:00 AM.",
                    timestamp = now - 86_400_000 + 300_000,
                    packageName = "com.whatsapp",
                    notificationId = 4,
                    messageType = "text",
                    isEdited = true,
                    originalMessage = "Meeting time is 10:00 AM.",
                    editedAt = now - 86_400_000 + 350_000
                ),
                MessageEntity(
                    id = 1005,
                    sender = senderName,
                    chatName = convName,
                    message = "I will send you the location link shortly.",
                    timestamp = now - 18_000_000,
                    packageName = "com.whatsapp",
                    notificationId = 5,
                    messageType = "text",
                    isDeleted = true,
                    deletedTimestamp = now - 17_900_000
                ),
                MessageEntity(
                    id = 1006,
                    sender = senderName,
                    chatName = convName,
                    message = "Voice message (0:18)",
                    timestamp = now - 7_200_000,
                    packageName = "com.whatsapp",
                    notificationId = 6,
                    messageType = "media",
                    hasMedia = true,
                    mediaType = "voice_note",
                    mediaFileName = "voice_note_18s.opus"
                ),
                MessageEntity(
                    id = 1007,
                    sender = senderName,
                    chatName = convName,
                    message = "Here is a quick video demo of the app interface.",
                    timestamp = now - 3_600_000,
                    packageName = "com.whatsapp",
                    notificationId = 7,
                    messageType = "media",
                    hasMedia = true,
                    mediaType = "video",
                    mediaFileName = "app_demo.mp4"
                ),
                MessageEntity(
                    id = 1008,
                    sender = senderName,
                    chatName = convName,
                    message = "Everything is updated. Let me know if you need anything else!",
                    timestamp = now - 600_000,
                    packageName = "com.whatsapp",
                    notificationId = 8,
                    messageType = "text"
                )
            )
        }
    }

    val uiState: StateFlow<ConversationDetailUiState> = _conversation
        .flatMapLatest { conv ->
            if (conv != null) {
                if (conv.conversationId < 0) {
                    flowOf(getSampleMessagesForConversation(conv.conversationName))
                } else {
                    messageRepository.observeMessagesForConversation(conv.conversationId, conv.conversationName)
                }
            } else {
                flowOf(emptyList())
            }
        }
        .combine(_conversation) { dbList, conv ->
            val effectiveList = if (dbList.isNotEmpty()) {
                dbList
            } else if (conv != null) {
                getSampleMessagesForConversation(conv.conversationName)
            } else {
                getSampleMessagesForConversation("Rahul Sharma")
            }

            ConversationDetailUiState(
                conversation = conv ?: ConversationsViewModel.sampleConversations.firstOrNull { it.conversationId == conversationId }
                ?: ConversationEntity(
                    conversationId = conversationId,
                    conversationName = "Rahul Sharma",
                    senderIdentifier = "Rahul",
                    lastMessage = "Hey! Did you check the project update?",
                    lastMessageTimestamp = System.currentTimeMillis(),
                    messageCount = 8,
                    isGroup = false
                ),
                messages = effectiveList,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ConversationDetailUiState()
        )

    init {
        loadConversation()
    }

    private fun loadConversation() {
        viewModelScope.launch {
            try {
                if (conversationId < 0) {
                    val sample = ConversationsViewModel.sampleConversations.firstOrNull { it.conversationId == conversationId }
                    _conversation.value = sample ?: ConversationEntity(
                        conversationId = conversationId,
                        conversationName = "Rahul Sharma",
                        senderIdentifier = "Rahul",
                        lastMessage = "Hey! Did you check the project update?",
                        lastMessageTimestamp = System.currentTimeMillis(),
                        messageCount = 8,
                        isGroup = false
                    )
                } else {
                    val conv = messageRepository.getConversationById(conversationId)
                    _conversation.value = conv
                    if (conv != null) {
                        messageRepository.markConversationAsRead(conv.conversationId, conv.conversationName)
                    }
                }
            } catch (e: Exception) {
                _conversation.value = null
            }
        }
    }

    fun exportConversation(context: Context) {
        viewModelScope.launch {
            try {
                val state = uiState.value
                val convName = state.conversation?.conversationName ?: "Conversation"
                val messagesList = state.messages

                val sb = StringBuilder()
                sb.appendLine("========================================")
                sb.appendLine("WhatsDel Conversation Export: $convName")
                sb.appendLine("Exported On: ${DateUtils.formatTimestamp(System.currentTimeMillis())}")
                sb.appendLine("Total Messages: ${messagesList.size}")
                sb.appendLine("========================================\n")

                for (msg in messagesList) {
                    val timeStr = DateUtils.formatTimestamp(msg.timestamp)
                    val prefix = when {
                        msg.isDeleted -> "[DELETED] "
                        msg.isEdited -> "[EDITED] "
                        msg.hasMedia -> "[${(msg.mediaType ?: "MEDIA").uppercase()}] "
                        else -> ""
                    }
                    val body = if (msg.message.isNotBlank()) msg.message else (msg.mediaFileName ?: "Attachment")
                    sb.appendLine("[$timeStr] ${msg.sender}: $prefix$body")
                    if (msg.isEdited && !msg.originalMessage.isNullOrBlank()) {
                        sb.appendLine("   └ Original Text: ${msg.originalMessage}")
                    }
                }

                val exportText = sb.toString()

                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, exportText)
                    putExtra(Intent.EXTRA_SUBJECT, "WhatsDel Export - $convName")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Export Conversation")
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(shareIntent)

                Toast.makeText(context, "Export ready for $convName!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("ConversationDetailVM", "Failed to export conversation", e)
                Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun clearConversationHistory(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val conv = _conversation.value
                val convName = conv?.conversationName ?: ""
                messageRepository.clearConversationHistory(conversationId, convName)
                Toast.makeText(context, "Captured history cleared for $convName", Toast.LENGTH_SHORT).show()
                onSuccess()
            } catch (e: Exception) {
                Log.e("ConversationDetailVM", "Failed to clear conversation history", e)
                Toast.makeText(context, "Failed to clear history: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun <T1, T2, R> kotlinx.coroutines.flow.Flow<T1>.combine(
    other: kotlinx.coroutines.flow.Flow<T2>,
    transform: suspend (T1, T2) -> R
): kotlinx.coroutines.flow.Flow<R> = kotlinx.coroutines.flow.combine(this, other, transform)
