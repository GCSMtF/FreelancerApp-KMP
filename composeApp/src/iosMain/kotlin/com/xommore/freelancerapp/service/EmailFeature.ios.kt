package com.xommore.freelancerapp.service

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xommore.freelancerapp.data.model.Project

@Composable
actual fun EmailSettingsScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text("이메일 설정은 iOS에서 아직 지원되지 않습니다", fontSize = 14.sp)
    }
}

@Composable
actual fun EmailSendButton(
    projects: List<Project>,
    selectedYear: Int,
    selectedMonth: Int,
    propsMap: Map<String, Long>
) {
    Button(
        onClick = { /* TODO */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        enabled = false
    ) {
        Text("📧 이메일 발송 (iOS 미지원)", fontWeight = FontWeight.SemiBold)
    }
}