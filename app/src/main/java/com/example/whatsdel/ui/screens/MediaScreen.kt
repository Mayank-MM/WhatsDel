package com.example.whatsdel.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatsdel.R
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.ui.components.EmptyState
import com.example.whatsdel.ui.components.LoadingIndicator
import com.example.whatsdel.ui.components.MediaGridItem

@Composable
fun MediaScreen(
    onMediaClick: (Long) -> Unit,
    viewModel: MediaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedMediaForDelete by remember { mutableStateOf<MessageEntity?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar + Clear All button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search media...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search"
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            if (uiState.mediaMessages.isNotEmpty()) {
                IconButton(onClick = { showDeleteAllDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Clear All Media",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Filter chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MediaFilter.entries) { filter ->
                val isSelected = selectedFilter == filter
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    label = "chip_color"
                )

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onFilterChanged(filter) },
                    label = { Text(filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = containerColor,
                        containerColor = containerColor
                    )
                )
            }
        }

        // Content area
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
                uiState.mediaMessages.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Outlined.PermMedia,
                        message = stringResource(R.string.media_empty),
                        subtitle = stringResource(R.string.media_empty_subtitle)
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.mediaMessages,
                            key = { it.id }
                        ) { message ->
                            MediaGridItem(
                                message = message,
                                onClick = { onMediaClick(message.id) },
                                onDeleteClick = { selectedMediaForDelete = message }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete single item confirmation dialog
    selectedMediaForDelete?.let {
        AlertDialog(
            onDismissRequest = { selectedMediaForDelete = null },
            title = { Text("Delete Media File?") },
            text = { Text("Are you sure you want to delete this backed-up media file from WhatsDel and local storage?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = selectedMediaForDelete
                        selectedMediaForDelete = null
                        if (toDelete != null) {
                            viewModel.deleteMedia(toDelete, context)
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMediaForDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete ALL media confirmation dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Delete All Media Files?") },
            text = { Text("Are you sure you want to permanently delete ALL backed-up photos, videos, voice notes, and documents from WhatsDel storage and database? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllDialog = false
                        viewModel.deleteAllMedia(context)
                    }
                ) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
