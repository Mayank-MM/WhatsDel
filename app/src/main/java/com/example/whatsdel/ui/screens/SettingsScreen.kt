package com.example.whatsdel.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
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
import com.example.whatsdel.ui.components.PermissionStatusCard
import com.example.whatsdel.ui.components.SettingsItem
import com.example.whatsdel.utils.PermissionUtils

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val themeLabel = when (uiState.appTheme) {
        "light" -> "Light Theme"
        "dark" -> "Dark Theme"
        else -> "System Default"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // General Section
        Text(
            text = "General & Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column {
                SettingsItem(
                    title = stringResource(R.string.settings_theme),
                    subtitle = themeLabel,
                    icon = Icons.Outlined.ColorLens,
                    onClick = { showThemeDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Deleted Message Alerts",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Notify when a contact deletes a message in WhatsApp",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isDeletedNotificationsEnabled,
                        onCheckedChange = { viewModel.toggleDeletedNotifications(it) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Raw Notification Logging",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Log raw diagnostic trace data for troubleshooting",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isRawLoggingEnabled,
                        onCheckedChange = { viewModel.toggleRawLogging(it) }
                    )
                }
            }
        }

        // Permissions Section
        Text(
            text = "Permissions & Monitoring",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PermissionStatusCard(
                title = stringResource(R.string.settings_notification_access),
                isGranted = uiState.isNotificationAccessGranted,
                icon = Icons.Outlined.Notifications,
                onActionClick = {
                    context.startActivity(PermissionUtils.notificationListenerSettingsIntent())
                }
            )

            PermissionStatusCard(
                title = stringResource(R.string.settings_storage_permission),
                isGranted = uiState.isStoragePermissionGranted,
                icon = Icons.Outlined.Storage,
                onActionClick = {
                    context.startActivity(PermissionUtils.storageSettingsIntent(context))
                }
            )

            PermissionStatusCard(
                title = stringResource(R.string.settings_battery_optimization),
                isGranted = uiState.isBatteryOptimizationDisabled,
                icon = Icons.Outlined.BatteryStd,
                onActionClick = {
                    context.startActivity(PermissionUtils.batteryOptimizationIntent())
                }
            )
        }

        // Storage Section
        Text(
            text = "Storage & Database",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatRow(label = "Total Captured Messages", value = "${uiState.totalMessages}")
                Spacer(modifier = Modifier.height(8.dp))
                StatRow(label = "Captured Deleted Messages", value = "${uiState.deletedMessages}")

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showClearDataDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Clear History"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Stored History")
                }
            }
        }

        // About Section
        Text(
            text = "About WhatsDel",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column {
                SettingsItem(
                    title = "Privacy Policy & Local Storage",
                    subtitle = "All captured messages are strictly stored on your device only",
                    icon = Icons.Outlined.PrivacyTip,
                    onClick = { }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem(
                    title = "App Version",
                    subtitle = "v1.0.0 • Material 3 Modern UI",
                    icon = Icons.Outlined.Info,
                    onClick = { }
                )
            }
        }

        // Footer Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Made with ❤️ by Mayank",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        "system" to "System Default",
                        "light" to "Light Theme",
                        "dark" to "Dark Theme"
                    ).forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAppTheme(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.appTheme == mode,
                                onClick = {
                                    viewModel.setAppTheme(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Clear Stored Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Stored Data?") },
            text = { Text("This action will permanently delete all captured message history and conversation records from WhatsDel's local database. Proceed?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataDialog = false
                        viewModel.clearAllData { }
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
