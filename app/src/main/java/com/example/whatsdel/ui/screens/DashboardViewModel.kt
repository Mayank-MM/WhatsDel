package com.example.whatsdel.ui.screens

import androidx.lifecycle.ViewModel
import com.example.whatsdel.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // In Phase 2, this will collect real data from the repository via Flow.
    // For now, the default fake values in DashboardUiState are used.
}
