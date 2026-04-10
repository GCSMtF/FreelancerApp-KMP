package com.xommore.freelancerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xommore.freelancerapp.ui.theme.*
import com.xommore.freelancerapp.viewmodel.AuthState

@Composable
fun LoginScreen(
    authState: AuthState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onPasswordReset: (String) -> Unit,
    onClearError: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showPasswordResetDialog by remember { mutableStateOf(false) }

    // 에러 스낵바
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authState.error) {
        authState.error?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    // 비밀번호 재설정 성공 알림
    LaunchedEffect(authState.passwordResetSent) {
        if (authState.passwordResetSent) {
            snackbarHostState.showSnackbar("비밀번호 재설정 이메일이 발송되었습니다")
        }
    }

    // 비밀번호 재설정 다이얼로그
    if (showPasswordResetDialog) {
        PasswordResetDialog(
            onDismiss = { showPasswordResetDialog = false },
            onConfirm = { resetEmail ->
                onPasswordReset(resetEmail)
                showPasswordResetDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Navy, NavyDark)
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 로고
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Blue, Navy)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "F",
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 앱 이름
                Text(
                    text = "프리랜서 정산",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "프리랜서를 위한 스마트 수익 관리",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 입력 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 탭 전환 (로그인 / 회원가입)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Background, RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            Button(
                                onClick = { isSignUpMode = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isSignUpMode) Navy else Color.Transparent,
                                    contentColor = if (!isSignUpMode) Color.White else TextSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Text("로그인", fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { isSignUpMode = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSignUpMode) Navy else Color.Transparent,
                                    contentColor = if (isSignUpMode) Color.White else TextSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Text("회원가입", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 이메일 입력
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("이메일") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "이메일",
                                    tint = TextSecondary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = Navy,
                                focusedLabelColor = Navy
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 비밀번호 입력
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("비밀번호") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "비밀번호",
                                    tint = TextSecondary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "숨기기" else "보기",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = Navy,
                                focusedLabelColor = Navy
                            )
                        )

                        // 비밀번호 확인 (회원가입 모드에서만)
                        if (isSignUpMode) {
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("비밀번호 확인") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "비밀번호 확인",
                                        tint = TextSecondary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                                supportingText = {
                                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                                        Text(
                                            "비밀번호가 일치하지 않습니다",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = Navy,
                                    focusedLabelColor = Navy
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 로그인/회원가입 버튼
                        Button(
                            onClick = {
                                if (isSignUpMode) {
                                    if (password == confirmPassword) {
                                        onSignUp(email, password)
                                    }
                                } else {
                                    onSignIn(email, password)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Navy),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !authState.isLoading &&
                                    email.isNotBlank() &&
                                    password.isNotBlank() &&
                                    (!isSignUpMode || password == confirmPassword)
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isSignUpMode) "회원가입" else "로그인",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // 비밀번호 찾기 (로그인 모드에서만)
                        if (!isSignUpMode) {
                            TextButton(
                                onClick = { showPasswordResetDialog = true },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(
                                    text = "비밀번호를 잊으셨나요?",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 하단 안내
                Text(
                    text = "계속 진행하면 이용약관 및 개인정보처리방침에\n동의하는 것으로 간주됩니다.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * 비밀번호 재설정 다이얼로그
 */
@Composable
private fun PasswordResetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var resetEmail by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = {
            Text(
                text = "비밀번호 재설정",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "가입한 이메일 주소를 입력하면\n비밀번호 재설정 링크를 보내드립니다.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = resetEmail,
                    onValueChange = { resetEmail = it },
                    label = { Text("이메일") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "이메일",
                            tint = TextSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Navy,
                        focusedLabelColor = Navy
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(resetEmail) },
                enabled = resetEmail.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                Text("발송")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = TextSecondary)
            }
        }
    )
}