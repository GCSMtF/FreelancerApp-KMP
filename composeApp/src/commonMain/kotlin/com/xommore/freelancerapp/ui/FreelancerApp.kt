package com.xommore.freelancerapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.xommore.freelancerapp.ui.components.BottomNavBar
import com.xommore.freelancerapp.ui.screens.*
import com.xommore.freelancerapp.ui.theme.FreelancerAppTheme
import com.xommore.freelancerapp.viewmodel.AuthViewModel
import com.xommore.freelancerapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)

expect fun exitApp()

@Composable
fun FreelancerApp(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var backPressedOnce by remember { mutableStateOf(false) }

    FreelancerAppTheme {
        if (!authState.isLoggedIn) {
            LoginScreen(
                authState = authState,
                onSignIn = { email, password -> authViewModel.signIn(email, password) },
                onSignUp = { email, password -> authViewModel.signUp(email, password) },
                onPasswordReset = { email -> authViewModel.sendPasswordResetEmail(email) },
                onClearError = { authViewModel.clearError() },
                onGoogleSignInSuccess = {
                    authViewModel.refreshAuthState()
                }
            )
        } else {
            BackHandler(enabled = true) {
                if (selectedTab != 0) {
                    selectedTab = 0
                } else if (backPressedOnce) {
                    exitApp()
                } else {
                    backPressedOnce = true
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("한 번 더 누르면 앱이 종료됩니다")
                        backPressedOnce = false
                    }
                }
            }

            Scaffold(
                bottomBar = {
                    BottomNavBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (selectedTab) {
                        0 -> HomeScreen(
                            onNavigateToProjects = { statusFilter -> selectedTab = 1 },
                            viewModel = mainViewModel
                        )
                        1 -> ProjectsScreen(viewModel = mainViewModel)
                        2 -> StatementScreen(viewModel = mainViewModel)
                        3 -> StatsScreen(viewModel = mainViewModel)
                        4 -> SettingsScreen(
                            viewModel = mainViewModel,
                            authViewModel = authViewModel,
                            userEmail = authState.user?.email
                        )
                    }
                }
            }
        }
    }
}
