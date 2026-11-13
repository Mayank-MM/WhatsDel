package com.example.whatsdel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.ui.components.EmptyState
import com.example.whatsdel.ui.components.LoadingIndicator
import com.example.whatsdel.utils.DateUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailScreen(
    onBackClick: () -> Unit = {},
    onMediaClick: (Long) -> Unit = {},
    viewModel: ConversationDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    var selectedMessageForDetail by remember { mutableStateOf<MessageEntity?>(null) }
    var selectedImageForPreview by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val convName = uiState.conversation?.conversationName ?: "Conversation"
                    val isGroup = uiState.conversation?.isGroup == true
                    val initials = getInitials(convName)

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGroup) MaterialTheme.colorScheme.tertiaryContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGroup) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = convName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Conversation History",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export Conversation") },
                            onClick = {
                                showMenu = false
                                viewModel.exportConversation(context)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Captured History") },
                            onClick = {
                                showMenu = false
                                showClearDialog = true
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                uiState.messages.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Person,
                        message = "No messages in this conversation",
                        subtitle = "Captured messages will appear here."
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Date Separator
                        item {
                            DateSeparator(text = "Today")
                        }

                        items(
                            items = uiState.messages,
                            key = { it.id }
                        ) { message ->
                            ChatBubbleItem(
                                message = message,
                                onBubbleClick = { selectedMessageForDetail = message },
                                onMediaClick = {
                                    if (message.mediaType == "image") {
                                        selectedImageForPreview = message.localMediaPath ?: message.thumbnailPath ?: "sample"
                                    } else {
                                        onMediaClick(message.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for clearing captured history
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Captured History?") },
            text = { Text("Are you sure you want to delete all captured messages for this conversation from WhatsDel?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        viewModel.clearConversationHistory(context) {
                            onBackClick()
                        }
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Message Detail Bottom Sheet
    selectedMessageForDetail?.let { msg ->
        MessageDetailBottomSheet(
            message = msg,
            onDismiss = { selectedMessageForDetail = null }
        )
    }

    // Fullscreen Image Preview Dialog
    selectedImageForPreview?.let { imgPath ->
        FullscreenImagePreviewDialog(
            imagePath = imgPath,
            onDismiss = { selectedImageForPreview = null }
        )
    }
}

@Composable
private fun DateSeparator(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            tonalElevation = 1.dp
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: MessageEntity,
    onBubbleClick: () -> Unit,
    onMediaClick: () -> Unit
) {
    val isDeleted = message.isDeleted
    val isEdited = message.isEdited
    val mediaType = message.mediaType

    val bubbleColor = when {
        isDeleted -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        isEdited -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            onClick = onBubbleClick,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Sender Header for Group Conversations
                if (message.sender != message.chatName) {
                    Text(
                        text = message.sender,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Badges row for Deleted or Edited status
                if (isDeleted || isEdited) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        if (isDeleted) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.error
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "This message was deleted",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        }

                        if (isEdited) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondary
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Edited",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Render Media Attachments
                when (mediaType) {
                    "image" -> {
                        val mediaPath = message.localMediaPath ?: message.thumbnailPath
                        val hasFile = mediaPath != null && File(mediaPath).exists()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.5f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(onClick = onMediaClick),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasFile) {
                                AsyncImage(
                                    model = File(mediaPath),
                                    contentDescription = "Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Photo",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Photo Attachment",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    "video" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.6f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .clickable(onClick = onMediaClick),
                            contentAlignment = Alignment.Center
                        ) {
                            val mediaPath = message.thumbnailPath ?: message.localMediaPath
                            if (mediaPath != null && File(mediaPath).exists()) {
                                AsyncImage(
                                    model = File(mediaPath),
                                    contentDescription = "Video",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = "Video Attachment",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            FloatingActionButton(
                                onClick = onMediaClick,
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                shape = CircleShape,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Video",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    "voice_note" -> {
                        Surface(
                            onClick = onMediaClick,
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice Note",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Voice Message",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Tap to play audio",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    "document" -> {
                        Surface(
                            onClick = onMediaClick,
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "Document",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = message.mediaFileName ?: "Document File",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "PDF • ${(message.mediaSize ?: 1_200_000L) / 1024} KB",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Text Content
                if (message.message.isNotBlank()) {
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Timestamp
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateUtils.formatTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageDetailBottomSheet(
    message: MessageEntity,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Message Metadata",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            MetadataRow(label = "Sender", value = message.sender)
            MetadataRow(label = "Chat", value = message.chatName)
            MetadataRow(label = "Captured At", value = DateUtils.formatTimestamp(message.timestamp))
            MetadataRow(label = "Message Type", value = message.messageType.uppercase())

            if (message.isEdited) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Edit History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                MetadataRow(label = "Original Text", value = message.originalMessage ?: "N/A")
                message.editedAt?.let {
                    MetadataRow(label = "Edited At", value = DateUtils.formatTimestamp(it))
                }
            }

            if (message.isDeleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Deletion Record",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                message.deletedTimestamp?.let {
                    MetadataRow(label = "Deleted At", value = DateUtils.formatTimestamp(it))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FullscreenImagePreviewDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            if (imagePath != "sample" && File(imagePath).exists()) {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = "Full Image Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Image Preview",
                        tint = Color.White,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Full Image Preview Placeholder",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun getInitials(name: String): String {
    val parts = name.trim().split(" ")
    return when {
        parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
        parts.isNotEmpty() && parts[0].isNotBlank() -> parts[0].take(2).uppercase()
        else -> "?"
    }
}
