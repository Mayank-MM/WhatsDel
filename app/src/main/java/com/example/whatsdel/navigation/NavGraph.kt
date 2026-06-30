package com.example.whatsdel.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.whatsdel.ui.screens.DashboardScreen
import com.example.whatsdel.ui.screens.DeletedScreen
import com.example.whatsdel.ui.screens.EditedMessagesScreen
import com.example.whatsdel.ui.screens.MediaScreen
import com.example.whatsdel.ui.screens.MediaViewerScreen
import com.example.whatsdel.ui.screens.MessageDetailScreen
import com.example.whatsdel.ui.screens.MessagesScreen
import com.example.whatsdel.ui.screens.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToEdited = {
                    navController.navigate(Screen.EditedMessages.route)
                }
            )
        }
        composable(Screen.Messages.route) {
            MessagesScreen(
                onMessageClick = { messageId ->
                    navController.navigate(Screen.MessageDetail.createRoute(messageId))
                }
            )
        }
        composable(Screen.Deleted.route) {
            DeletedScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(
            route = Screen.Media.route,
            arguments = listOf(
                navArgument("filter") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) {
            MediaScreen(
                onMediaClick = { messageId ->
                    navController.navigate(Screen.MediaViewer.createRoute(messageId))
                }
            )
        }
        composable(
            route = Screen.MediaViewer.route,
            arguments = listOf(
                navArgument("messageId") {
                    type = NavType.LongType
                }
            )
        ) {
            MediaViewerScreen()
        }
        composable(Screen.EditedMessages.route) {
            EditedMessagesScreen(
                onMessageClick = { messageId ->
                    navController.navigate(Screen.MessageDetail.createRoute(messageId))
                }
            )
        }
        composable(
            route = Screen.MessageDetail.route,
            arguments = listOf(
                navArgument("messageId") {
                    type = NavType.LongType
                }
            )
        ) {
            MessageDetailScreen()
        }
    }
}
