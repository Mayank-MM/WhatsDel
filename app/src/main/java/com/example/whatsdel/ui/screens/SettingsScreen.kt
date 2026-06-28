package com.example.whatsdel.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatsdel.R
import com.example.whatsdel.ui.components.PermissionStatusCard
import com.example.whatsdel.ui.components.SettingsItem
import com.example.whatsdel.utils.PermissionUtils

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // Permissions Section
        Text(
            text = stringResource(R.string.settings_section_permissions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
        )

        PermissionStatusCard(
            title = stringResource(R.string.settings_notification_access),
            isGranted = uiState.isNotificationAccessGranted,
            icon = Icons.Outlined.Notifications,
            onActionClick = {
                context.startActivity(PermissionUtils.notificationListenerSettingsIntent())
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PermissionStatusCard(
            title = stringResource(R.string.settings_storage_permission),
            isGranted = uiState.isStoragePermissionGranted,
            icon = Icons.Outlined.Storage,
            onActionClick = {
                context.startActivity(PermissionUtils.storageSettingsIntent(context))
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PermissionStatusCard(
            title = stringResource(R.string.settings_battery_optimization),
            isGranted = uiState.isBatteryOptimizationDisabled,
            icon = Icons.Outlined.BatteryStd,
            onActionClick = {
                context.startActivity(PermissionUtils.batteryOptimizationIntent())
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Database Statistics Section
        Text(
            text = "Database Statistics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatRow(label = "Total Messages", value = "${uiState.totalMessages}")
                Spacer(modifier = Modifier.height(8.dp))
                StatRow(label = "Deleted Messages", value = "${uiState.deletedMessages}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // General Section
        Text(
            text = stringResource(R.string.settings_section_general),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SettingsItem(
            title = stringResource(R.string.settings_theme),
            subtitle = stringResource(R.string.settings_theme_subtitle),
            icon = Icons.Outlined.ColorLens,
            onClick = { /* Future */ }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        SettingsItem(
            title = stringResource(R.string.settings_about),
            subtitle = stringResource(R.string.settings_about_subtitle),
            icon = Icons.Outlined.Info,
            onClick = { /* Future */ }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
