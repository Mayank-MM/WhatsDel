package com.example.whatsdel.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Message
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatsdel.R
import com.example.whatsdel.ui.components.EmptyState
import com.example.whatsdel.ui.components.LoadingIndicator

@Composable
fun MessagesScreen(
    viewModel: MessagesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                LoadingIndicator()
            }
            uiState.messages.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.Message,
                    message = stringResource(R.string.messages_empty),
                    subtitle = stringResource(R.string.messages_empty_subtitle)
                )
            }
            // Phase 2: Display message list here
        }
    }
}
