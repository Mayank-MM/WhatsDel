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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DeletedUiState(
    val deletedMessages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val sortNewestFirst: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DeletedViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortNewestFirst = MutableStateFlow(true)
    val sortNewestFirst: StateFlow<Boolean> = _sortNewestFirst.asStateFlow()

    private val _messagesFlow = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            messageRepository.observeDeletedMessages()
        } else {
            messageRepository.searchDeletedMessages(query)
        }
    }

    val uiState: StateFlow<DeletedUiState> = combine(
        _messagesFlow,
        _searchQuery,
        _sortNewestFirst
    ) { messages, query, newestFirst ->
        // Only sorting is done in-memory, DAO handles filtering and text search
        val sorted = if (newestFirst) {
            messages.sortedByDescending { it.deletedTimestamp ?: it.timestamp }
        } else {
            messages.sortedBy { it.deletedTimestamp ?: it.timestamp }
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
