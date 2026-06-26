package com.example.whatsdel.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.whatsdel.R

data class BottomNavItem(
    val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        titleResId = R.string.nav_dashboard,
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        route = Screen.Dashboard.route
    ),
    BottomNavItem(
        titleResId = R.string.nav_messages,
        selectedIcon = Icons.Filled.Message,
        unselectedIcon = Icons.Outlined.Message,
        route = Screen.Messages.route
    ),
    BottomNavItem(
        titleResId = R.string.nav_deleted,
        selectedIcon = Icons.Filled.Delete,
        unselectedIcon = Icons.Outlined.Delete,
        route = Screen.Deleted.route
    ),
    BottomNavItem(
        titleResId = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        route = Screen.Settings.route
    )
)
