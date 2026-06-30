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
    val editedMessages: Int = 0,
    val totalMedia: Int = 0,
    val imageCount: Int = 0,
    val videoCount: Int = 0,
    val audioCount: Int = 0,
    val stickerCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    messageRepository: MessageRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        messageRepository.getMessageCount(),
        messageRepository.getDeletedCount(),
        messageRepository.getEditedCount(),
        messageRepository.getMediaCount(),
        messageRepository.getImageCount(),
        messageRepository.getVideoCount(),
        messageRepository.getAudioCount(),
        messageRepository.getStickerCount()
    ) { counts ->
        DashboardUiState(
            totalMessages = counts[0],
            deletedMessages = counts[1],
            editedMessages = counts[2],
            totalMedia = counts[3],
            imageCount = counts[4],
            videoCount = counts[5],
            audioCount = counts[6],
            stickerCount = counts[7],
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )
}
