package com.example.whatsdel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.whatsdel.R
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.ui.components.LoadingIndicator
import com.example.whatsdel.utils.DateUtils
import com.example.whatsdel.utils.MediaDownloadHelper
import java.io.File

@Composable
fun MediaViewerScreen(
    onBackClick: () -> Unit = {},
    viewModel: MediaViewerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    when {
        uiState.isLoading -> {
            LoadingIndicator()
        }
        uiState.error != null -> {
            MediaErrorView(error = uiState.error!!)
        }
        uiState.message != null -> {
            val message = uiState.message!!
            Box(modifier = Modifier.fillMaxSize()) {
                when (message.mediaType) {
                    "image" -> ImageViewer(message = message, onDeleteClick = { showDeleteDialog = true })
                    "video" -> VideoViewer(message = message, onDeleteClick = { showDeleteDialog = true })
                    "voice_note" -> AudioViewer(
                        message = message,
                        isPlaying = uiState.isPlaying,
                        progress = uiState.playbackProgress,
                        onTogglePlayback = { viewModel.togglePlayback() },
                        onDeleteClick = { showDeleteDialog = true }
                    )
                    "sticker" -> StickerViewer(message = message, onDeleteClick = { showDeleteDialog = true })
                    else -> MediaUnavailableView()
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Media File?") },
                        text = { Text("Are you sure you want to delete this backed-up media file from WhatsDel and remove it from local storage?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    viewModel.deleteMediaFile(context) {
                                        onBackClick()
                                    }
                                }
                            ) {
                                Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageViewer(
    message: MessageEntity,
    onDeleteClick: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val displayPath = message.localMediaPath ?: message.thumbnailPath
    val hasMedia = displayPath != null && File(displayPath).exists()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 5f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (hasMedia) {
                AsyncImage(
                    model = File(displayPath!!),
                    contentDescription = "Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    contentScale = ContentScale.Fit
                )
            } else {
                MediaPlaceholder(icon = Icons.Filled.Image, label = "Image")
            }
        }

        MediaInfoBar(message = message, onDeleteClick = onDeleteClick)
    }
}

@Composable
private fun VideoViewer(
    message: MessageEntity,
    onDeleteClick: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    val videoPath = message.localMediaPath
    val hasVideoFile = videoPath != null && File(videoPath).exists()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (isPlaying && hasVideoFile) {
                AndroidView(
                    factory = { context ->
                        android.widget.VideoView(context).apply {
                            setVideoPath(videoPath)
                            val mediaController = android.widget.MediaController(context)
                            mediaController.setAnchorView(this)
                            setMediaController(mediaController)
                            setOnCompletionListener {
                                isPlaying = false
                            }
                            start()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val displayPath = message.thumbnailPath ?: message.localMediaPath
                if (displayPath != null && File(displayPath).exists()) {
                    AsyncImage(
                        model = File(displayPath),
                        contentDescription = "Video Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    MediaPlaceholder(icon = Icons.Filled.Videocam, label = "Video")
                }

                FloatingActionButton(
                    onClick = {
                        if (hasVideoFile) {
                            isPlaying = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        MediaInfoBar(message = message, onDeleteClick = onDeleteClick)
    }
}

@Composable
private fun AudioViewer(
    message: MessageEntity,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlayback: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Voice Note",
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Voice Note",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "From ${message.sender}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        FloatingActionButton(
            onClick = onTogglePlayback,
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        MediaInfoBar(message = message, onDeleteClick = onDeleteClick)
    }
}

@Composable
private fun StickerViewer(
    message: MessageEntity,
    onDeleteClick: () -> Unit
) {
    val displayPath = message.localMediaPath ?: message.thumbnailPath
    val hasMedia = displayPath != null && File(displayPath).exists()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (hasMedia) {
                AsyncImage(
                    model = File(displayPath!!),
                    contentDescription = "Sticker",
                    modifier = Modifier
                        .size(250.dp)
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                MediaPlaceholder(icon = Icons.Filled.StickyNote2, label = "Sticker")
            }
        }

        MediaInfoBar(message = message, onDeleteClick = onDeleteClick)
    }
}

@Composable
private fun MediaInfoBar(
    message: MessageEntity,
    onDeleteClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val filePath = message.localMediaPath

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = message.sender,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = DateUtils.getRelativeTime(message.timestamp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (filePath != null && File(filePath).exists()) {
                        IconButton(
                            onClick = {
                                MediaDownloadHelper.downloadMediaToPublicStorage(
                                    context = context,
                                    srcFile = File(filePath),
                                    mediaType = message.mediaType ?: "media"
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GetApp,
                                contentDescription = "Download Media",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Media",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            if (message.chatName != message.sender) {
                Text(
                    text = message.chatName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (message.message.isNotBlank() && !message.message.contains("received")) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MediaPlaceholder(icon: ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.media_unavailable),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun MediaErrorView(error: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.BrokenImage,
            contentDescription = "Error",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MediaUnavailableView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.BrokenImage,
            contentDescription = "Unavailable",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.media_unavailable),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.media_unavailable_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
