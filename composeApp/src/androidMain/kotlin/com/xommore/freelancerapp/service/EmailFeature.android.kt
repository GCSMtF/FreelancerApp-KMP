package com.xommore.freelancerapp.service

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xommore.freelancerapp.data.model.Project
import com.xommore.freelancerapp.ui.components.formatCurrency
import com.xommore.freelancerapp.ui.theme.*
import kotlinx.coroutines.launch

@Composable
actual fun EmailSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val settingsManager = remember { EmailSettingsManager(context) }
    val emailService = remember { EmailService(context) }

    var senderEmail by remember { mutableStateOf(settingsManager.getSenderEmail()) }
    var appPassword by remember { mutableStateOf(settingsManager.getAppPassword()) }
    var senderName by remember { mutableStateOf(settingsManager.getSenderName()) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }

    LaunchedEffect(senderEmail, appPassword, senderName) {
        hasChanges = senderEmail != settingsManager.getSenderEmail() ||
                appPassword != settingsManager.getAppPassword() ||
                senderName != settingsManager.getSenderName()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = MaterialTheme.colorScheme.onSurface) }
            Text("이메일 발송 설정", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
            TextButton(
                onClick = {
                    settingsManager.setSenderEmail(senderEmail.trim())
                    settingsManager.setAppPassword(appPassword.trim())
                    settingsManager.setSenderName(senderName.trim())
                    hasChanges = false
                    Toast.makeText(context, "저장되었습니다", Toast.LENGTH_SHORT).show()
                },
                enabled = hasChanges && senderEmail.isNotBlank() && appPassword.isNotBlank()
            ) {
                Text("저장", fontWeight = FontWeight.SemiBold,
                    color = if (hasChanges && senderEmail.isNotBlank() && appPassword.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 안내 카드
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Blue.copy(alpha = 0.1f)), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Info, contentDescription = "안내", tint = Blue, modifier = Modifier.size(20.dp))
                        Text("Gmail 앱 비밀번호 설정 방법", fontWeight = FontWeight.SemiBold, color = Blue)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("1. Google 계정 → 보안 → 2단계 인증 활성화\n2. Google 계정 → 보안 → 앱 비밀번호\n3. 앱 선택: 메일, 기기 선택: 기타\n4. 생성된 16자리 비밀번호 입력",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                }
            }

            // 발신자 정보
            Text("발신자 정보", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(value = senderName, onValueChange = { senderName = it }, label = { Text("발신자 이름 (선택)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "이름", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }))

                    OutlinedTextField(value = senderEmail, onValueChange = { senderEmail = it }, label = { Text("발신 이메일 (Gmail) *") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "이메일", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        placeholder = { Text("example@gmail.com") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }))
                }
            }

            // 앱 비밀번호
            Text("인증 정보", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(value = appPassword, onValueChange = { appPassword = it.replace(" ", "").take(16) },
                        label = { Text("앱 비밀번호 (16자리) *") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "비밀번호", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = if (passwordVisible) "숨기기" else "보기", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        supportingText = { Text("${appPassword.length}/16자리", color = if (appPassword.length == 16) StatusPaidText else MaterialTheme.colorScheme.onSurfaceVariant) })

                    if (appPassword.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(if (appPassword.length == 16) Icons.Default.CheckCircle else Icons.Default.Warning, contentDescription = null,
                                tint = if (appPassword.length == 16) StatusPaidText else StatusPendingText, modifier = Modifier.size(18.dp))
                            Text(if (appPassword.length == 16) "올바른 형식입니다" else "16자리를 입력해주세요",
                                fontSize = 13.sp, color = if (appPassword.length == 16) StatusPaidText else StatusPendingText)
                        }
                    }
                }
            }

            // 테스트 발송
            Text("테스트", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Button(
                onClick = {
                    settingsManager.setSenderEmail(senderEmail.trim())
                    settingsManager.setAppPassword(appPassword.trim())
                    settingsManager.setSenderName(senderName.trim())
                    hasChanges = false
                    isTesting = true
                    coroutineScope.launch {
                        when (val result = emailService.sendTestEmail()) {
                            is EmailService.EmailResult.Success -> Toast.makeText(context, "✅ 테스트 메일 발송 완료!", Toast.LENGTH_LONG).show()
                            is EmailService.EmailResult.Error -> Toast.makeText(context, "❌ ${result.message}", Toast.LENGTH_LONG).show()
                        }
                        isTesting = false
                    }
                },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                enabled = senderEmail.isNotBlank() && appPassword.length == 16 && !isTesting
            ) {
                if (isTesting) { CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp); Spacer(modifier = Modifier.width(8.dp)); Text("발송 중...") }
                else { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "발송", modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("테스트 메일 발송", fontWeight = FontWeight.SemiBold) }
            }

            // 설정 초기화
            OutlinedButton(
                onClick = { settingsManager.clearSettings(); senderEmail = ""; appPassword = ""; senderName = ""; Toast.makeText(context, "설정이 초기화되었습니다", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
            ) { Icon(Icons.Default.Delete, contentDescription = "초기화", modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("설정 초기화") }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
actual fun EmailSendButton(
    projects: List<Project>,
    selectedYear: Int,
    selectedMonth: Int,
    propsMap: Map<String, Long>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val emailService = remember { EmailService(context) }
    val emailSettingsManager = remember { EmailSettingsManager(context) }
    var isSending by remember { mutableStateOf(false) }

    val clientGroupsWithEmail = remember(projects) {
        projects.filter { it.clientEmail.isNotBlank() }.groupBy { it.clientEmail }
    }

    val isConfigured = emailService.isConfigured()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = {
                if (!isConfigured) {
                    Toast.makeText(context, "설정 > 이메일 발송 설정에서 Gmail 앱 비밀번호를 설정해주세요", Toast.LENGTH_LONG).show()
                } else if (clientGroupsWithEmail.isNotEmpty()) {
                    // 첫 번째 클라이언트에게 발송 (간단 버전)
                    val (email, clientProjects) = clientGroupsWithEmail.entries.first()
                    val client = clientProjects.first()
                    val subject = "[정산서] ${selectedYear}년 ${selectedMonth}월 작업 정산"
                    val body = generateEmailBody(selectedYear, selectedMonth, client.clientName, clientProjects, propsMap)

                    isSending = true
                    coroutineScope.launch {
                        when (val result = emailService.sendEmail(email, subject, body)) {
                            is EmailService.EmailResult.Success -> Toast.makeText(context, "✅ 정산서가 발송되었습니다!", Toast.LENGTH_SHORT).show()
                            is EmailService.EmailResult.Error -> Toast.makeText(context, "❌ ${result.message}", Toast.LENGTH_LONG).show()
                        }
                        isSending = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (clientGroupsWithEmail.isNotEmpty() && isConfigured) Blue else MaterialTheme.colorScheme.outline),
            enabled = clientGroupsWithEmail.isNotEmpty() && !isSending
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp)); Text("발송 중...", fontWeight = FontWeight.SemiBold)
            } else {
                Icon(Icons.Default.Email, contentDescription = "이메일", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (clientGroupsWithEmail.isNotEmpty()) "📧 클라이언트에게 정산서 발송 (${clientGroupsWithEmail.size}명)"
                    else "📧 이메일 정보가 없습니다",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (!isConfigured) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = StatusPendingBg), shape = RoundedCornerShape(8.dp)) {
                Text("⚠️ 이메일 발송을 위해 Gmail 앱 비밀번호 설정이 필요합니다.\n설정 > 이메일 발송 설정에서 설정해주세요.",
                    fontSize = 12.sp, color = StatusPendingText, modifier = Modifier.padding(12.dp), lineHeight = 18.sp)
            }
        }
    }
}

private fun generateEmailBody(year: Int, month: Int, clientName: String, projects: List<Project>, propsMap: Map<String, Long>): String {
    val netIncome = projects.sumOf { it.netIncome }
    val totalProps = projects.sumOf { propsMap[it.id] ?: 0L }
    val finalAmount = netIncome + totalProps

    return buildString {
        appendLine("안녕하세요, ${clientName.ifBlank { "담당자" }}님.")
        appendLine()
        appendLine("${year}년 ${month}월 작업 정산 내역을 안내드립니다.")
        appendLine()
        appendLine("==========================================")
        projects.forEach { project ->
            val propAmount = propsMap[project.id] ?: 0L
            appendLine("▶ ${project.brand}")
            appendLine("   • ${project.workType.displayName} · ${project.cuts}컷 × ${formatCurrency(project.basePrice)}")
            appendLine("   • 인건비: ${formatCurrency(project.netIncome)}")
            if (propAmount > 0) appendLine("   • 소품비: ${formatCurrency(propAmount)}")
            appendLine()
        }
        appendLine("==========================================")
        appendLine("💵 최종 정산 금액: ${formatCurrency(finalAmount)}")
        appendLine("==========================================")
        appendLine()
        appendLine("감사합니다.")
    }
}