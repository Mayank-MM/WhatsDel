package com.example.whatsdel.ui.screens

import android.content.Context
import android.widget.Toast
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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isNotificationAccessGranted: Boolean = false,
    val isStoragePermissionGranted: Boolean = false,
    val isBatteryOptimizationDisabled: Boolean = false,
    val totalMessages: Int = 0,
    val deletedMessages: Int = 0,
    val isRawLoggingEnabled: Boolean = false,
    val isDeletedNotificationsEnabled: Boolean = true,
    val appTheme: String = "system"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("whatsdel_prefs", Context.MODE_PRIVATE)

    private val _permissionState = MutableStateFlow(
        SettingsUiState(
            isNotificationAccessGranted = PermissionUtils.isNotificationListenerEnabled(context),
            isStoragePermissionGranted = PermissionUtils.isStoragePermissionGranted(context),
            isBatteryOptimizationDisabled = PermissionUtils.isBatteryOptimizationDisabled(context),
            isRawLoggingEnabled = sharedPrefs.getBoolean("raw_logging_enabled", false),
            isDeletedNotificationsEnabled = sharedPrefs.getBoolean("deleted_notifications_enabled", true),
            appTheme = sharedPrefs.getString("app_theme", "system") ?: "system"
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
                isBatteryOptimizationDisabled = PermissionUtils.isBatteryOptimizationDisabled(context),
                isRawLoggingEnabled = sharedPrefs.getBoolean("raw_logging_enabled", false),
                isDeletedNotificationsEnabled = sharedPrefs.getBoolean("deleted_notifications_enabled", true),
                appTheme = sharedPrefs.getString("app_theme", "system") ?: "system"
            )
        }
    }

    fun setAppTheme(theme: String) {
        sharedPrefs.edit().putString("app_theme", theme).apply()
        _permissionState.update { it.copy(appTheme = theme) }
    }

    fun toggleRawLogging(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("raw_logging_enabled", enabled).apply()
        _permissionState.update { it.copy(isRawLoggingEnabled = enabled) }
    }

    fun toggleDeletedNotifications(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("deleted_notifications_enabled", enabled).apply()
        _permissionState.update { it.copy(isDeletedNotificationsEnabled = enabled) }
    }

    fun clearAllData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                messageRepository.deleteAllMessages()
                Toast.makeText(context, "All captured database history cleared", Toast.LENGTH_SHORT).show()
                onSuccess()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to clear data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
