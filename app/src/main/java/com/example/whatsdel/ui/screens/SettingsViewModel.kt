package com.example.whatsdel.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.whatsdel.utils.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsUiState(
    val isNotificationAccessGranted: Boolean = false,
    val isStoragePermissionGranted: Boolean = false,
    val isBatteryOptimizationDisabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshPermissionStatus()
    }

    fun refreshPermissionStatus() {
        _uiState.update { currentState ->
            currentState.copy(
                isNotificationAccessGranted = PermissionUtils.isNotificationListenerEnabled(context),
                isStoragePermissionGranted = PermissionUtils.isStoragePermissionGranted(context),
                isBatteryOptimizationDisabled = PermissionUtils.isBatteryOptimizationDisabled(context)
            )
        }
    }
}
