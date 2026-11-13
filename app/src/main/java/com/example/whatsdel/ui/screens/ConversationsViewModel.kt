package com.example.whatsdel.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsdel.data.entity.ConversationEntity
import com.example.whatsdel.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ConversationsUiState(
    val conversations: List<ConversationEntity> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    companion object {
        val sampleConversations = listOf(
            ConversationEntity(
                conversationId = -101L,
                conversationName = "Rahul Sharma",
                senderIdentifier = "Rahul",
                lastMessage = "Hey! Did you check the project update?",
                lastMessageTimestamp = System.currentTimeMillis() - 600_000,
                messageCount = 14,
                isGroup = false,
                unreadCount = 2,
                hasDeletedMessages = true,
                hasEditedMessages = false
            ),
            ConversationEntity(
                conversationId = -102L,
                conversationName = "Priya",
                senderIdentifier = "Priya",
                lastMessage = "📷 Photo received",
                lastMessageTimestamp = System.currentTimeMillis() - 3_600_000,
                messageCount = 8,
                isGroup = false,
                unreadCount = 0,
                hasDeletedMessages = false,
                hasEditedMessages = true
            ),
            ConversationEntity(
                conversationId = -103L,
                conversationName = "College Group",
                senderIdentifier = "College Group",
                lastMessage = "Amit: The presentation is scheduled for 10 AM tomorrow.",
                lastMessageTimestamp = System.currentTimeMillis() - 14_400_000,
                messageCount = 42,
                isGroup = true,
                unreadCount = 5,
                hasDeletedMessages = true,
                hasEditedMessages = true
            ),
            ConversationEntity(
                conversationId = -104L,
                conversationName = "Amit Kumar",
                senderIdentifier = "Amit",
                lastMessage = "🎤 Voice message (0:24)",
                lastMessageTimestamp = System.currentTimeMillis() - 86_400_000,
                messageCount = 5,
                isGroup = false,
                unreadCount = 0,
                hasDeletedMessages = true,
                hasEditedMessages = false
            ),
            ConversationEntity(
                conversationId = -105L,
                conversationName = "Family Group",
                senderIdentifier = "Family Group",
                lastMessage = "Mom: Don't forget dinner tonight!",
                lastMessageTimestamp = System.currentTimeMillis() - 172_800_000,
                messageCount = 89,
                isGroup = true,
                unreadCount = 0,
                hasDeletedMessages = false,
                hasEditedMessages = false
            )
        )
    }

    val uiState: StateFlow<ConversationsUiState> = _searchQuery
        .flatMapLatest { query ->
            messageRepository.observeAllConversations()
        }
        .combine(_searchQuery) { dbList, query ->
            val effectiveList = if (dbList.isNotEmpty()) {
                if (query.isBlank()) {
                    dbList
                } else {
                    dbList.filter {
                        it.conversationName.contains(query, ignoreCase = true) ||
                                it.senderIdentifier.contains(query, ignoreCase = true) ||
                                it.lastMessage.contains(query, ignoreCase = true)
                    }
                }
            } else if (query.isBlank()) {
                sampleConversations
            } else {
                sampleConversations.filter {
                    it.conversationName.contains(query, ignoreCase = true) ||
                            it.senderIdentifier.contains(query, ignoreCase = true) ||
                            it.lastMessage.contains(query, ignoreCase = true)
                }
            }
            ConversationsUiState(
                conversations = effectiveList,
                isLoading = false,
                searchQuery = query
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ConversationsUiState()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}

private fun <T1, T2, R> kotlinx.coroutines.flow.Flow<T1>.combine(
    other: kotlinx.coroutines.flow.Flow<T2>,
    transform: suspend (T1, T2) -> R
): kotlinx.coroutines.flow.Flow<R> = kotlinx.coroutines.flow.combine(this, other, transform)
