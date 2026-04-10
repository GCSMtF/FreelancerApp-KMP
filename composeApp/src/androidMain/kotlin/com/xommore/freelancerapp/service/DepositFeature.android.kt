package com.xommore.freelancerapp.service

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xommore.freelancerapp.data.model.Client
import com.xommore.freelancerapp.ui.theme.*

fun isNotificationServiceEnabled(context: android.content.Context): Boolean {
    val flat = android.provider.Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat?.contains(context.packageName) == true
}

@Composable
actual fun DepositAlertSettingsScreen(clients: List<Client>, onBack: () -> Unit) {
    val context = LocalContext.current
    val depositAlertManager = remember { DepositAlertManager(context) }
    val focusManager = LocalFocusManager.current

    var bankName by remember { mutableStateOf(depositAlertManager.getAccountInfo()?.first ?: "") }
    var accountNumber by remember { mutableStateOf(depositAlertManager.getAccountInfo()?.second ?: "") }
    var registeredClients by remember { mutableStateOf(depositAlertManager.getRegisteredClients()) }
    var newClientName by remember { mutableStateOf("") }
    var hasChanges by remember { mutableStateOf(false) }
    var depositAlertEnabled by remember { mutableStateOf(depositAlertManager.isEnabled()) }
    var isPermissionGranted by remember { mutableStateOf(isNotificationServiceEnabled(context)) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = MaterialTheme.colorScheme.onSurface) }
            Text("입금 알림 설정", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
            TextButton(onClick = {
                depositAlertManager.saveAccountInfo(bankName.trim(), accountNumber.trim())
                hasChanges = false
                Toast.makeText(context, "저장되었습니다", Toast.LENGTH_SHORT).show()
            }, enabled = hasChanges) {
                Text("저장", fontWeight = FontWeight.SemiBold, color = if (hasChanges) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // 알림 활성화 토글
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.size(42.dp).background(
                            if (isPermissionGranted && depositAlertEnabled) StatusPaidText.copy(alpha = 0.1f) else StatusPendingText.copy(alpha = 0.1f), RoundedCornerShape(12.dp)
                        ), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Notifications, contentDescription = "알림",
                                tint = if (isPermissionGranted && depositAlertEnabled) StatusPaidText else StatusPendingText, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("입금 알림", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                if (isPermissionGranted) { if (depositAlertEnabled) "✓ 활성화됨" else "비활성화됨" } else "알림 권한 필요",
                                fontSize = 13.sp, color = if (isPermissionGranted && depositAlertEnabled) StatusPaidText else StatusPendingText
                            )
                        }
                    }
                    Switch(checked = depositAlertEnabled && isPermissionGranted, onCheckedChange = {
                        if (isPermissionGranted) { depositAlertEnabled = it; depositAlertManager.setEnabled(it) }
                        else { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                    })
                }
            }

            if (!isPermissionGranted) {
                Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                    Text("알림 접근 권한 설정")
                }
            }

            // 모니터링 계좌
            Text("모니터링 계좌", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(value = bankName, onValueChange = { bankName = it; hasChanges = true }, label = { Text("은행명") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = "은행", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        placeholder = { Text("예: 신한은행") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }))
                    OutlinedTextField(value = accountNumber, onValueChange = { accountNumber = it; hasChanges = true }, label = { Text("계좌번호") },
                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = "계좌", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }))
                }
            }

            // 모니터링 대상
            Text("모니터링 대상", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = newClientName, onValueChange = { newClientName = it }, placeholder = { Text("입금자 이름 입력") },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = {
                                if (newClientName.isNotBlank()) { depositAlertManager.addClient(newClientName.trim()); registeredClients = depositAlertManager.getRegisteredClients(); newClientName = ""; focusManager.clearFocus() }
                            }))
                        Button(onClick = {
                            if (newClientName.isNotBlank()) { depositAlertManager.addClient(newClientName.trim()); registeredClients = depositAlertManager.getRegisteredClients(); newClientName = "" }
                        }, enabled = newClientName.isNotBlank(), shape = RoundedCornerShape(10.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "추가", modifier = Modifier.size(18.dp))
                        }
                    }

                    Text("등록된 대상 (${registeredClients.size}명)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    registeredClients.forEach { clientName ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = RoundedCornerShape(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Person, contentDescription = "클라이언트", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                    Text(clientName, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                IconButton(onClick = { depositAlertManager.removeClient(clientName); registeredClients = depositAlertManager.getRegisteredClients() }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "삭제", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 입금 기록
            val depositRecords = depositAlertManager.getDepositRecords()
            if (depositRecords.isNotEmpty()) {
                Text("최근 입금 감지 기록", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        depositRecords.takeLast(5).reversed().forEach { record ->
                            val formattedAmount = String.format("%,d", record.amount)
                            val dateFormat = java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.KOREA)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${record.clientName} · ${formattedAmount}원", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(dateFormat.format(java.util.Date(record.timestamp)), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { depositAlertManager.clearDepositRecords(); Toast.makeText(context, "기록이 삭제되었습니다", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red), border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))) {
                            Text("기록 삭제", fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
