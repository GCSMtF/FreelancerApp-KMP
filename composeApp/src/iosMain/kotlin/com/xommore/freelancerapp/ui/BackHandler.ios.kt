package com.xommore.freelancerapp.ui

import androidx.compose.runtime.Composable
import platform.posix.exit

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS에는 시스템 뒤로가기 버튼이 없음
}

actual fun exitApp() {
    exit(0)
}
