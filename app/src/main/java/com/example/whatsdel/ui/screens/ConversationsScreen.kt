package com.example.whatsdel.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatsdel.ui.components.ConversationItem
import com.example.whatsdel.ui.components.EmptyState
import com.example.whatsdel.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    onConversationClick: (Long) -> Unit,
    viewModel: ConversationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Conversations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
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
                            text = { Text("Filter Conversations") },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark All Read") },
                            onClick = { showMenu = false }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Search bar with clear button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search conversations...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search"
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                singleLine = true
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
                uiState.conversations.isEmpty() && searchQuery.isNotBlank() -> {
                    EmptyState(
                        icon = Icons.Outlined.SearchOff,
                        message = "No conversations found",
                        subtitle = "No conversation matching '$searchQuery'."
                    )
                }
                uiState.conversations.isEmpty() -> {
                    EmptyState(
                        icon = Icons.AutoMirrored.Outlined.Chat,
                        message = "No conversations yet",
                        subtitle = "Captured WhatsApp conversations will appear here automatically."
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(
                            items = uiState.conversations,
                            key = { it.conversationId }
                        ) { conversation ->
                            ConversationItem(
                                conversation = conversation,
                                onClick = { onConversationClick(conversation.conversationId) },
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
