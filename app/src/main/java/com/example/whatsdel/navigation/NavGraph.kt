package com.example.whatsdel.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.whatsdel.ui.screens.ConversationDetailScreen
import com.example.whatsdel.ui.screens.ConversationsScreen
import com.example.whatsdel.ui.screens.DashboardScreen
import com.example.whatsdel.ui.screens.DeletedScreen
import com.example.whatsdel.ui.screens.EditedMessagesScreen
import com.example.whatsdel.ui.screens.MediaScreen
import com.example.whatsdel.ui.screens.MediaViewerScreen
import com.example.whatsdel.ui.screens.MessageDetailScreen
import com.example.whatsdel.ui.screens.MessagesScreen
import com.example.whatsdel.ui.screens.SettingsScreen
import com.example.whatsdel.ui.screens.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    navigateToDeleted: Boolean = false
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    val targetScreen = if (navigateToDeleted) Screen.Deleted.route else Screen.Dashboard.route
                    navController.navigate(targetScreen) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToEdited = {
                    navController.navigate(Screen.EditedMessages.route)
                },
                onNavigateToConversations = {
                    navController.navigate(Screen.Conversations.route)
                },
                onNavigateToDeleted = {
                    navController.navigate(Screen.Deleted.route)
                },
                onNavigateToMedia = {
                    navController.navigate(Screen.Media.createRoute())
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Conversations.route) {
            ConversationsScreen(
                onConversationClick = { conversationId ->
                    navController.navigate(Screen.ConversationDetail.createRoute(conversationId))
                }
            )
        }
        composable(
            route = Screen.ConversationDetail.route,
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.LongType
                }
            )
        ) {
            ConversationDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onMediaClick = { messageId ->
                    navController.navigate(Screen.MediaViewer.createRoute(messageId))
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
            DeletedScreen(
                onMessageClick = { messageId ->
                    navController.navigate(Screen.MediaViewer.createRoute(messageId))
                }
            )
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
            MediaViewerScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
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
