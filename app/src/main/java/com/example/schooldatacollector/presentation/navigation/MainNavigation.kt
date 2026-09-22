package com.example.schooldatacollector.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.schooldatacollector.presentation.screen.AdminDashboardScreen
import com.example.schooldatacollector.presentation.screen.TeacherDashboardScreen
import com.example.schooldatacollector.presentation.viewmodel.AppViewModelFactory

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Admin : Screen("admin", "Admin", Icons.Default.AddModerator)
    object Teacher : Screen("teacher", "Teacher", Icons.Default.Dashboard)
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    val items = listOf(
        Screen.Admin,
        Screen.Teacher
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Admin.route,
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .consumeWindowInsets(PaddingValues(bottom = innerPadding.calculateBottomPadding()))
        ) {
            composable(Screen.Admin.route) {
                AdminDashboardScreen(
                    viewModel = viewModel(factory = AppViewModelFactory)
                )
            }
            composable(Screen.Teacher.route) {
                TeacherDashboardScreen(
                    viewModel = viewModel(factory = AppViewModelFactory)
                )
            }
        }
    }
}
