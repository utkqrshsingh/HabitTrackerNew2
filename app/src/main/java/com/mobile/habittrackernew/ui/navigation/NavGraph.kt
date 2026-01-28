// ui/navigation/NavGraph.kt
package com.mobile.habittrackernew.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobile.habittrackernew.ui.screens.aicoach.AICoachScreen
import com.mobile.habittrackernew.ui.screens.aicoach.DebugScreen
import com.mobile.habittrackernew.ui.screens.alarms.AlarmsScreen
import com.mobile.habittrackernew.ui.screens.auth.AuthViewModel
import com.mobile.habittrackernew.ui.screens.auth.LoginScreen
import com.mobile.habittrackernew.ui.screens.auth.SignupScreen
import com.mobile.habittrackernew.ui.screens.category.HabitDetailScreen
import com.mobile.habittrackernew.ui.screens.dashboard.DashboardScreen
import com.mobile.habittrackernew.ui.screens.notifications.NotificationsScreen
import com.mobile.habittrackernew.ui.screens.progress.ProgressScreen
import com.mobile.habittrackernew.ui.screens.settings.SettingsScreen
import com.mobile.habittrackernew.ui.screens.splash.SplashScreen
import kotlinx.coroutines.delay

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    object Splash : Screen("splash", "Splash")
    object Login : Screen("login", "Login")
    object Signup : Screen("signup", "Sign Up")
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Progress : Screen("progress", "Progress", Icons.Default.BarChart)
    object AICoach : Screen("ai_coach", "AI Coach", Icons.Default.Psychology)
    object Alarms : Screen("alarms", "Alarms", Icons.Default.Alarm)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Notifications : Screen("notifications", "Notifications")
    object HabitDetail : Screen("habit/{habitId}", "Habit") {
        fun createRoute(habitId: Long) = "habit/$habitId"
    }
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Progress,
    Screen.AICoach,
    Screen.Alarms,
    Screen.Settings
)

@Composable
fun HabitTrackerNavHost(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val authState by authViewModel.uiState.collectAsState()

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == screen.route
                            } == true,
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash Screen
            composable(
                route = Screen.Splash.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                SplashScreen(
                    onSplashComplete = { isLoggedIn ->
                        val destination = if (isLoggedIn) {
                            Screen.Dashboard.route
                        } else {
                            Screen.Login.route
                        }
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Auth Screens
            composable(
                route = Screen.Login.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                LoginScreen(
                    onNavigateToSignup = {
                        navController.navigate(Screen.Signup.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.Signup.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) }
            ) {
                SignupScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSignupSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Main Screens
            composable(
                route = Screen.Dashboard.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                DashboardScreen(
                    onHabitClick = { habitId ->
                        navController.navigate(Screen.HabitDetail.createRoute(habitId))
                    },
                    onNotificationClick = {
                        navController.navigate(Screen.Notifications.route)
                    }
                )
            }

            composable(
                route = Screen.Progress.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                ProgressScreen()
            }

            composable(
                route = Screen.AICoach.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                AICoachScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Alarms.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                AlarmsScreen()
            }

            composable(
                route = Screen.Settings.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                SettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onAlarmsClick = {
                        navController.navigate(Screen.Alarms.route)
                    }
                )
            }

            composable("debug") {
                DebugScreen(
                    onBack = { navController.navigateUp() }
                )
            }

            // Notifications Screen
            composable(
                route = Screen.Notifications.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) }
            ) {
                NotificationsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Habit Detail Screen
            composable(
                route = Screen.HabitDetail.route,
                arguments = listOf(
                    navArgument("habitId") { type = NavType.LongType }
                ),
                enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) }
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getLong("habitId") ?: 0L
                HabitDetailScreen(
                    habitId = habitId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
