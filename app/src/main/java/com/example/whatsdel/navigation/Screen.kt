package com.example.whatsdel.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Messages : Screen("messages")
    data object Deleted : Screen("deleted")
    data object Settings : Screen("settings")
}
