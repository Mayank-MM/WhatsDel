package com.example.whatsdel

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.whatsdel.navigation.NavGraph
import com.example.whatsdel.navigation.Screen
import com.example.whatsdel.navigation.bottomNavItems
import com.example.whatsdel.ui.components.WhatsDelBottomNavBar
import com.example.whatsdel.ui.components.WhatsDelTopAppBar
import com.example.whatsdel.ui.theme.WhatsDelTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences("whatsdel_prefs", MODE_PRIVATE)

        enableEdgeToEdge()
        setContent {
            var appTheme by remember {
                mutableStateOf(sharedPrefs.getString("app_theme", "system") ?: "system")
            }

            DisposableEffect(Unit) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                    if (key == "app_theme") {
                        appTheme = prefs.getString("app_theme", "system") ?: "system"
                    }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            val systemDark = isSystemInDarkTheme()
            val useDarkTheme = when (appTheme) {
                "light" -> false
                "dark" -> true
                else -> systemDark
            }

            WhatsDelTheme(darkTheme = useDarkTheme, dynamicColor = false) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

                val currentTitle = when {
                    currentRoute == Screen.Dashboard.route -> stringResource(R.string.nav_dashboard)
                    currentRoute == Screen.Messages.route -> stringResource(R.string.nav_messages)
                    currentRoute == Screen.Conversations.route -> "Conversations"
                    currentRoute?.startsWith("conversation_detail") == true -> "Conversation History"
                    currentRoute == Screen.Deleted.route -> stringResource(R.string.nav_deleted)
                    currentRoute == Screen.Settings.route -> stringResource(R.string.nav_settings)
                    currentRoute == Screen.EditedMessages.route -> stringResource(R.string.nav_edited)
                    currentRoute?.startsWith("media_viewer") == true -> stringResource(R.string.nav_media)
                    currentRoute?.startsWith("media") == true -> stringResource(R.string.nav_media)
                    currentRoute?.startsWith("message_detail") == true -> stringResource(R.string.message_detail)
                    else -> stringResource(R.string.app_name)
                }

                val isSplash = currentRoute == Screen.Splash.route

                val navigateTo = intent?.getStringExtra("navigate_to")
                val shouldNavigateToDeleted = navigateTo == "deleted"

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        if (!isSplash) {
                            WhatsDelTopAppBar(
                                title = currentTitle,
                                scrollBehavior = scrollBehavior
                            )
                        }
                    },
                    bottomBar = {
                        if (!isSplash) {
                            WhatsDelBottomNavBar(
                                items = bottomNavItems,
                                currentRoute = currentRoute,
                                onItemClick = { item ->
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        navigateToDeleted = shouldNavigateToDeleted
                    )
                }
            }
        }
    }
}
