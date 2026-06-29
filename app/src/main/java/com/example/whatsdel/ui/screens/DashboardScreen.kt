package com.example.whatsdel.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatsdel.R
import com.example.whatsdel.ui.components.LoadingIndicator
import com.example.whatsdel.ui.components.StatCard
import com.example.whatsdel.ui.theme.StatAudioColor
import com.example.whatsdel.ui.theme.StatDeletedColor
import com.example.whatsdel.ui.theme.StatImageColor
import com.example.whatsdel.ui.theme.StatStickerColor
import com.example.whatsdel.ui.theme.StatTotalColor
import com.example.whatsdel.ui.theme.StatVideoColor

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        LoadingIndicator()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.dashboard_overview),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StatCard(
                    title = stringResource(R.string.stat_total_messages),
                    value = uiState.totalMessages,
                    icon = Icons.Outlined.Message,
                    accentColor = StatTotalColor
                )
            }
            item {
                StatCard(
                    title = stringResource(R.string.stat_deleted_messages),
                    value = uiState.deletedMessages,
                    icon = Icons.Outlined.Delete,
                    accentColor = StatDeletedColor
                )
            }
            item {
                StatCard(
                    title = stringResource(R.string.stat_images),
                    value = uiState.imageCount,
                    icon = Icons.Outlined.Image,
                    accentColor = StatImageColor
                )
            }
            item {
                StatCard(
                    title = stringResource(R.string.stat_videos),
                    value = uiState.videoCount,
                    icon = Icons.Outlined.Videocam,
                    accentColor = StatVideoColor
                )
            }
            item {
                StatCard(
                    title = stringResource(R.string.stat_audio),
                    value = uiState.audioCount,
                    icon = Icons.Outlined.Mic,
                    accentColor = StatAudioColor
                )
            }
            item {
                StatCard(
                    title = stringResource(R.string.stat_stickers),
                    value = uiState.stickerCount,
                    icon = Icons.Outlined.StickyNote2,
                    accentColor = StatStickerColor
                )
            }
        }
    }
}
