package com.example.whatsdel.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MessagesUiState(
    val messages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    messageRepository: MessageRepository
) : ViewModel() {

    val uiState: StateFlow<MessagesUiState> = messageRepository.getAllMessages()
        .map { messages ->
            MessagesUiState(messages = messages)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MessagesUiState()
        )
}
