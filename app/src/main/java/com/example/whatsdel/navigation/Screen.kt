package com.example.whatsdel.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Messages : Screen("messages")
    data object Deleted : Screen("deleted")
    data object Settings : Screen("settings")
    data object Media : Screen("media?filter={filter}") {
        fun createRoute(filter: String? = null): String = "media?filter=${filter ?: ""}"
    }
    data object MediaViewer : Screen("media_viewer/{messageId}") {
        fun createRoute(messageId: Long): String = "media_viewer/$messageId"
    }
}
