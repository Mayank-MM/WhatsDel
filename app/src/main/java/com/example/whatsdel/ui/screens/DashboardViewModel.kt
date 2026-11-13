package com.example.whatsdel.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.domain.repository.MessageRepository
import com.example.whatsdel.utils.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val conversationsCount: Int = 0,
    val totalMessages: Int = 0,
    val deletedMessages: Int = 0,
    val editedMessages: Int = 0,
    val totalMedia: Int = 0,
    val imageCount: Int = 0,
    val videoCount: Int = 0,
    val audioCount: Int = 0,
    val stickerCount: Int = 0,
    val recentActivity: List<MessageEntity> = emptyList(),
    val isNotificationListenerActive: Boolean = true,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    messageRepository: MessageRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        messageRepository.getConversationCount(),
        messageRepository.getMessageCount(),
        messageRepository.getDeletedCount(),
        messageRepository.getEditedCount(),
        messageRepository.getMediaCount(),
        messageRepository.getImageCount(),
        messageRepository.getVideoCount(),
        messageRepository.getAudioCount(),
        messageRepository.getStickerCount(),
        messageRepository.observeActiveMessages()
    ) { args ->
        val convCount = args[0] as Int
        val totalMsgs = args[1] as Int
        val delMsgs = args[2] as Int
        val edMsgs = args[3] as Int
        val mediaMsgs = args[4] as Int
        val imgCount = args[5] as Int
        val vidCount = args[6] as Int
        val audCount = args[7] as Int
        val stkCount = args[8] as Int
        @Suppress("UNCHECKED_CAST")
        val activeMsgs = args[9] as List<MessageEntity>

        val listenerActive = PermissionUtils.isNotificationListenerEnabled(context)

        DashboardUiState(
            conversationsCount = convCount,
            totalMessages = totalMsgs,
            deletedMessages = delMsgs,
            editedMessages = edMsgs,
            totalMedia = mediaMsgs,
            imageCount = imgCount,
            videoCount = vidCount,
            audioCount = audCount,
            stickerCount = stkCount,
            recentActivity = activeMsgs.take(5),
            isNotificationListenerActive = listenerActive,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )
}
