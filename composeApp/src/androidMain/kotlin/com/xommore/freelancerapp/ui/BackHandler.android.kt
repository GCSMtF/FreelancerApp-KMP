package com.xommore.freelancerapp.ui

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}

actual fun exitApp() {
    android.os.Process.killProcess(android.os.Process.myPid())
}
