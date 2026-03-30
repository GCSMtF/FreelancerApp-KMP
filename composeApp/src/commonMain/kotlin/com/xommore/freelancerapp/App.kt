package com.xommore.freelancerapp

import androidx.compose.runtime.Composable
import com.xommore.freelancerapp.ui.theme.FreelancerAppTheme

/**
 * 기본 App 진입점
 * Android: MainActivity에서 FreelancerApp을 직접 호출
 * iOS: MainViewController에서 이 함수를 호출할 수 있음
 */
@Composable
fun App() {
    FreelancerAppTheme {
        // Android에서는 MainActivity가 FreelancerApp을 직접 호출하므로
        // 이 함수는 iOS 진입점으로 활용 가능
    }
}