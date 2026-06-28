package com.example.whatsdel.ui.screens

import androidx.lifecycle.ViewModel
import com.example.whatsdel.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

data class DashboardUiState(
    val totalMessages: Int = 254,
    val deletedMessages: Int = 19,
    val editedMessages: Int = 7,
    val mediaFiles: Int = 86,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = messageRepository.getMessageCount()
        .map { totalCount ->
            DashboardUiState(totalMessages = totalCount)
        }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(totalMessages = 0)
        )
}
