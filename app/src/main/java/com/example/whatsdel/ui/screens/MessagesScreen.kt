package com.example.whatsdel.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatsdel.R
import com.example.whatsdel.ui.components.EmptyState
import com.example.whatsdel.ui.components.LoadingIndicator
import com.example.whatsdel.ui.components.MessageItem
import com.example.whatsdel.ui.components.PermissionStatusCard
import com.example.whatsdel.utils.PermissionUtils
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Column

@Composable
fun MessagesScreen(
    viewModel: MessagesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // We check this every recomposition to update UI when granted
    val isNotificationEnabled = PermissionUtils.isNotificationListenerEnabled(context)

    Column(modifier = Modifier.fillMaxSize()) {
        if (!isNotificationEnabled) {
            PermissionStatusCard(
                title = stringResource(R.string.settings_notification_access),
                isGranted = false,
                icon = Icons.Outlined.Notifications,
                onActionClick = {
                    context.startActivity(PermissionUtils.notificationListenerSettingsIntent())
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
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
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = uiState.messages,
                            key = { it.id }
                        ) { message ->
                            MessageItem(message = message)
                        }
                    }
                }
            }
        }
    }
}
