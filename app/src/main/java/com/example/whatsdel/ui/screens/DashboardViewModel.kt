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
    val imageCount: Int = 0,
    val videoCount: Int = 0,
    val audioCount: Int = 0,
    val stickerCount: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    messageRepository: MessageRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        messageRepository.getMessageCount(),
        messageRepository.getDeletedCount(),
        messageRepository.getImageCount(),
        messageRepository.getVideoCount(),
        messageRepository.getAudioCount(),
        messageRepository.getStickerCount()
    ) { counts ->
        DashboardUiState(
            totalMessages = counts[0],
            deletedMessages = counts[1],
            imageCount = counts[2],
            videoCount = counts[3],
            audioCount = counts[4],
            stickerCount = counts[5]
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )
}
