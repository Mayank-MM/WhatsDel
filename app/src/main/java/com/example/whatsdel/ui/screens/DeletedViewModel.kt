package com.example.whatsdel.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DeletedUiState(
    val deletedMessages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val sortNewestFirst: Boolean = true
)

@HiltViewModel
class DeletedViewModel @Inject constructor(
    messageRepository: MessageRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortNewestFirst = MutableStateFlow(true)
    val sortNewestFirst: StateFlow<Boolean> = _sortNewestFirst.asStateFlow()

    val uiState: StateFlow<DeletedUiState> = combine(
        messageRepository.getDeletedMessages(),
        _searchQuery,
        _sortNewestFirst
    ) { messages, query, newestFirst ->
        val filtered = if (query.isBlank()) {
            messages
        } else {
            messages.filter { msg ->
                msg.sender.contains(query, ignoreCase = true) ||
                msg.chatName.contains(query, ignoreCase = true) ||
                msg.message.contains(query, ignoreCase = true)
            }
        }
        val sorted = if (newestFirst) {
            filtered.sortedByDescending { it.deletedTimestamp ?: it.timestamp }
        } else {
            filtered.sortedBy { it.deletedTimestamp ?: it.timestamp }
        }
        DeletedUiState(
            deletedMessages = sorted,
            searchQuery = query,
            sortNewestFirst = newestFirst
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DeletedUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleSort() {
        _sortNewestFirst.value = !_sortNewestFirst.value
    }
}
