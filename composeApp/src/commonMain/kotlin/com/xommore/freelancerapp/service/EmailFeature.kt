package com.xommore.freelancerapp.service

import androidx.compose.runtime.Composable
import com.xommore.freelancerapp.data.model.Project

/**
 * 이메일 발송 + 이메일 설정 화면 (expect/actual)
 */

@Composable
expect fun EmailSettingsScreen(
    onBack: () -> Unit
)

@Composable
expect fun EmailSendButton(
    projects: List<Project>,
    selectedYear: Int,
    selectedMonth: Int,
    propsMap: Map<String, Long>
)