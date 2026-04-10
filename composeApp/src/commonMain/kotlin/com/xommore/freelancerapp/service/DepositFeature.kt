package com.xommore.freelancerapp.service

import androidx.compose.runtime.Composable
import com.xommore.freelancerapp.data.model.Client

@Composable
expect fun DepositAlertSettingsScreen(
    clients: List<Client>,
    onBack: () -> Unit
)
