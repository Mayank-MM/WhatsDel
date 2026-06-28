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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MessagesUiState(
    val messages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<MessagesUiState> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                messageRepository.observeActiveMessages()
            } else {
                messageRepository.searchActiveMessages(query)
            }
        }
        .map { messages ->
            MessagesUiState(
                messages = messages,
                searchQuery = _searchQuery.value
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MessagesUiState(isLoading = true)
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
