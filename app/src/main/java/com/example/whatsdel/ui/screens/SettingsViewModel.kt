package com.example.whatsdel.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsdel.domain.repository.MessageRepository
import com.example.whatsdel.utils.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsUiState(
    val isNotificationAccessGranted: Boolean = false,
    val isStoragePermissionGranted: Boolean = false,
    val isBatteryOptimizationDisabled: Boolean = false,
    val totalMessages: Int = 0,
    val deletedMessages: Int = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    messageRepository: MessageRepository
) : ViewModel() {

    private val _permissionState = MutableStateFlow(
        SettingsUiState(
            isNotificationAccessGranted = PermissionUtils.isNotificationListenerEnabled(context),
            isStoragePermissionGranted = PermissionUtils.isStoragePermissionGranted(context),
            isBatteryOptimizationDisabled = PermissionUtils.isBatteryOptimizationDisabled(context)
        )
    )

    val uiState: StateFlow<SettingsUiState> = combine(
        _permissionState,
        messageRepository.getMessageCount(),
        messageRepository.getDeletedCount()
    ) { permissions, totalCount, deletedCount ->
        permissions.copy(
            totalMessages = totalCount,
            deletedMessages = deletedCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _permissionState.value
    )

    fun refreshPermissionStatus() {
        _permissionState.update { currentState ->
            currentState.copy(
                isNotificationAccessGranted = PermissionUtils.isNotificationListenerEnabled(context),
                isStoragePermissionGranted = PermissionUtils.isStoragePermissionGranted(context),
                isBatteryOptimizationDisabled = PermissionUtils.isBatteryOptimizationDisabled(context)
            )
        }
    }
}
