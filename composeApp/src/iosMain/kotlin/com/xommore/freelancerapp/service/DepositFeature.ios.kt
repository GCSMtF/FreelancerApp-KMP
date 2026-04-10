package com.xommore.freelancerapp.service

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xommore.freelancerapp.data.model.Client

@Composable
actual fun DepositAlertSettingsScreen(clients: List<Client>, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text("입금 알림은 iOS에서 지원되지 않습니다", fontSize = 14.sp)
    }
}
