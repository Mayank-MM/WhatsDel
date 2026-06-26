package com.example.whatsdel.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
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
fun DeletedScreen(
    viewModel: DeletedViewModel = hiltViewModel()
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
            uiState.deletedMessages.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.DeleteSweep,
                    message = stringResource(R.string.deleted_empty),
                    subtitle = stringResource(R.string.deleted_empty_subtitle)
                )
            }
            // Phase 2: Display deleted message list here
        }
    }
}
