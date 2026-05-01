package com.spendrixa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.spendrixa.app.data.repository.TransactionRepository
import com.spendrixa.app.firebase.FirebaseAuthService
import com.spendrixa.app.ui.screens.AddTransactionScreen
import com.spendrixa.app.ui.screens.DashboardScreen
import com.spendrixa.app.ui.screens.HistoryScreen
import com.spendrixa.app.ui.screens.InsightsScreen
import com.spendrixa.app.ui.screens.LoginScreen
import com.spendrixa.app.ui.theme.SpendrixaTheme

private object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val ADD_TRANSACTION = "add_transaction"
    const val HISTORY = "history"
    const val INSIGHTS = "insights"
}

class MainActivity : ComponentActivity() {

    private lateinit var authService: FirebaseAuthService
    private lateinit var repository: TransactionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authService = FirebaseAuthService(FirebaseAuth.getInstance())
        repository = TransactionRepository()

        setContent {
            SpendrixaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.spendrixa.app.ui.theme.Background
                ) {
                    SpendrixaApp(
                        authService = authService,
                        repository = repository,
                        onLoginStateChanged = { }
                    )
                }
            }
        }
    }
}

@Composable
fun SpendrixaApp(
    authService: FirebaseAuthService,
    repository: TransactionRepository,
    onLoginStateChanged: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val currentUser by authService.currentUser.collectAsState()
    val isLoggedIn = currentUser != null

    LaunchedEffect(isLoggedIn) {
        onLoginStateChanged(isLoggedIn)
    }

    val startDestination = if (isLoggedIn) Routes.DASHBOARD else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authService = authService,
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                repository = repository,
                onAddTransaction = { navController.navigate(Routes.ADD_TRANSACTION) },
                onViewHistory = {
                    navController.navigate(Routes.HISTORY) {
                        launchSingleTop = true
                    }
                },
                onViewInsights = {
                    navController.navigate(Routes.INSIGHTS) {
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    authService.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ADD_TRANSACTION) {
            AddTransactionScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onNavigateDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateInsights = {
                    navController.navigate(Routes.INSIGHTS) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.INSIGHTS) {
            InsightsScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onNavigateDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateHistory = {
                    navController.navigate(Routes.HISTORY) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
