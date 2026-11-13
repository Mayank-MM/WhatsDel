package com.example.whatsdel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.ui.components.LoadingIndicator
import com.example.whatsdel.ui.components.StatCard
import com.example.whatsdel.ui.theme.StatEditedColor
import com.example.whatsdel.ui.theme.StatMediaColor
import com.example.whatsdel.ui.theme.StatTotalColor
import com.example.whatsdel.utils.DateUtils

@Composable
fun DashboardScreen(
    onNavigateToEdited: () -> Unit,
    onNavigateToConversations: () -> Unit = {},
    onNavigateToDeleted: () -> Unit = {},
    onNavigateToMedia: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        LoadingIndicator()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overview Section Title
        item {
            Text(
                text = "Overview Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Stat Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Conversations",
                        value = uiState.conversationsCount,
                        icon = Icons.AutoMirrored.Outlined.Chat,
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = onNavigateToConversations,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Messages",
                        value = uiState.totalMessages,
                        icon = Icons.Outlined.Message,
                        accentColor = StatTotalColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Edited",
                        value = uiState.editedMessages,
                        icon = Icons.Default.Edit,
                        accentColor = StatEditedColor,
                        onClick = onNavigateToEdited,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Media Files",
                        value = uiState.totalMedia,
                        icon = Icons.Outlined.PermMedia,
                        accentColor = StatMediaColor,
                        onClick = onNavigateToMedia,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Recent Activity Title
        item {
            Text(
                text = "Recent Captured Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Recent Activity List
        if (uiState.recentActivity.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = "No captured activity yet. Incoming WhatsApp messages will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(
                items = uiState.recentActivity,
                key = { it.id }
            ) { message ->
                RecentActivityItem(
                    message = message,
                    onClick = onNavigateToConversations
                )
            }
        }
    }
}

@Composable
private fun RecentActivityItem(
    message: MessageEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            message.isDeleted -> MaterialTheme.colorScheme.errorContainer
                            message.isEdited -> MaterialTheme.colorScheme.secondaryContainer
                            message.hasMedia -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        message.isDeleted -> Icons.Default.DeleteSweep
                        message.isEdited -> Icons.Default.Edit
                        message.mediaType == "image" -> Icons.Outlined.Image
                        message.mediaType == "video" -> Icons.Outlined.Videocam
                        message.mediaType == "voice_note" -> Icons.Outlined.Mic
                        else -> Icons.Outlined.Message
                    },
                    contentDescription = null,
                    tint = when {
                        message.isDeleted -> MaterialTheme.colorScheme.onErrorContainer
                        message.isEdited -> MaterialTheme.colorScheme.onSecondaryContainer
                        message.hasMedia -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.sender,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = DateUtils.getRelativeTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = message.message.ifBlank { message.mediaFileName ?: "Media file" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (message.isDeleted) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = "Deleted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else if (message.isEdited) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    Text(
                        text = "Edited",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
