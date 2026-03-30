package com.xommore.freelancerapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.xommore.freelancerapp.ui.components.BottomNavBar
import com.xommore.freelancerapp.ui.screens.*
import com.xommore.freelancerapp.ui.theme.FreelancerAppTheme
import com.xommore.freelancerapp.viewmodel.AuthViewModel
import com.xommore.freelancerapp.viewmodel.MainViewModel

/**
 * 메인 앱 Composable (commonMain)
 *
 * Android 전용 기능 (NotificationPermission, ThemeManager, SharedPreferences)은
 * 제거하고, 공통 로직만 포함합니다.
 * Android 전용 기능은 Step 9에서 androidMain에 추가합니다.
 */
@Composable
fun FreelancerApp(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    FreelancerAppTheme {
        if (!authState.isLoggedIn) {
            // 로그인 화면
            LoginScreen(
                authState = authState,
                onSignIn = { email, password ->
                    authViewModel.signIn(email, password)
                },
                onSignUp = { email, password ->
                    authViewModel.signUp(email, password)
                },
                onPasswordReset = { email ->
                    authViewModel.sendPasswordResetEmail(email)
                },
                onClearError = {
                    authViewModel.clearError()
                }
            )
        } else {
            // 메인 화면 (5탭 네비게이션)
            Scaffold(
                bottomBar = {
                    BottomNavBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (selectedTab) {
                        0 -> HomeScreen(
                            onNavigateToProjects = { statusFilter ->
                                // TODO: statusFilter를 ProjectsScreen에 전달
                                selectedTab = 1
                            },
                            viewModel = mainViewModel
                        )
                        1 -> ProjectsScreen(
                            viewModel = mainViewModel
                        )
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