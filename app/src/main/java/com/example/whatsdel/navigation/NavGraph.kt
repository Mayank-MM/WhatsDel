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
import com.example.whatsdel.ui.screens.MediaScreen
import com.example.whatsdel.ui.screens.MediaViewerScreen
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
            DashboardScreen()
        }
        composable(Screen.Messages.route) {
            MessagesScreen()
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
    }
}
