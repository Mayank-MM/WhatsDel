package com.example.whatsdel.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsdel.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val totalMessages: Int = 0,
    val deletedMessages: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    messageRepository: MessageRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        messageRepository.getMessageCount(),
        messageRepository.getDeletedCount()
    ) { totalCount, deletedCount ->
        DashboardUiState(
            totalMessages = totalCount,
            deletedMessages = deletedCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )
}
