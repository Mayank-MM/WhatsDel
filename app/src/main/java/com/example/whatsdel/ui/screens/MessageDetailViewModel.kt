package com.example.whatsdel.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsdel.data.entity.MessageEditHistoryEntity
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessageDetailUiState(
    val message: MessageEntity? = null,
    val editHistory: List<MessageEditHistoryEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class MessageDetailViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val messageId: Long = checkNotNull(savedStateHandle["messageId"])

    private val _uiState = MutableStateFlow(MessageDetailUiState())
    val uiState: StateFlow<MessageDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val messageEntity = messageRepository.getMessageById(messageId)
            _uiState.update { it.copy(message = messageEntity) }

            // Observe the history so it updates live if new edits come in
            messageRepository.observeEditHistory(messageId).collectLatest { history ->
                _uiState.update { it.copy(
                    editHistory = history,
                    isLoading = false
                ) }
            }
        }
    }
}
