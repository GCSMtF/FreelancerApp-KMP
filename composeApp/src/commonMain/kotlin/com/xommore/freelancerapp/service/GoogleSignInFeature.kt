package com.xommore.freelancerapp.service

import androidx.compose.runtime.Composable

@Composable
expect fun GoogleSignInButton(
    onSignInSuccess: () -> Unit,
    onSignInError: (String) -> Unit
)
