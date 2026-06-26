package com.example.whatsdel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
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

        enableEdgeToEdge()
        setContent {
            WhatsDelTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

                // Map the current route to the appropriate screen title
                val currentTitle = when (currentRoute) {
                    Screen.Dashboard.route -> stringResource(R.string.nav_dashboard)
                    Screen.Messages.route -> stringResource(R.string.nav_messages)
                    Screen.Deleted.route -> stringResource(R.string.nav_deleted)
                    Screen.Settings.route -> stringResource(R.string.nav_settings)
                    else -> stringResource(R.string.app_name)
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        WhatsDelTopAppBar(
                            title = currentTitle,
                            scrollBehavior = scrollBehavior
                        )
                    },
                    bottomBar = {
                        WhatsDelBottomNavBar(
                            items = bottomNavItems,
                            currentRoute = currentRoute,
                            onItemClick = { item ->
                                navController.navigate(item.route) {
                                    // Pop up to the start destination to avoid back stack buildup
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
