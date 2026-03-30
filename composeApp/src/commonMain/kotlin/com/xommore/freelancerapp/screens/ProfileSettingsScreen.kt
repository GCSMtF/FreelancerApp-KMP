package com.xommore.freelancerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xommore.freelancerapp.data.model.UserProfile
import com.xommore.freelancerapp.ui.theme.*

@Composable
fun ProfileSettingsScreen(
    profile: UserProfile?,
    onBack: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }
    var bankName by remember(profile) { mutableStateOf(profile?.bankName ?: "") }
    var accountNumber by remember(profile) { mutableStateOf(profile?.accountNumber ?: "") }
    var accountHolder by remember(profile) { mutableStateOf(profile?.accountHolder ?: "") }
    var businessNumber by remember(profile) { mutableStateOf(profile?.businessNumber ?: "") }
    var address by remember(profile) { mutableStateOf(profile?.address ?: "") }
    var taxRate by remember(profile) { mutableStateOf(profile?.taxRate?.toString() ?: "3.3") }

    var hasChanges by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(name, phone, email, bankName, accountNumber, accountHolder, businessNumber, address, taxRate) {
        hasChanges = name != (profile?.name ?: "") ||
                phone != (profile?.phone ?: "") ||
                email != (profile?.email ?: "") ||
                bankName != (profile?.bankName ?: "") ||
                accountNumber != (profile?.accountNumber ?: "") ||
                accountHolder != (profile?.accountHolder ?: "") ||
                businessNumber != (profile?.businessNumber ?: "") ||
                address != (profile?.address ?: "") ||
                taxRate != (profile?.taxRate?.toString() ?: "3.3")
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { if (hasChanges) showSaveDialog = true else onBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("내 프로필", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
            TextButton(
                onClick = {
                    val newProfile = UserProfile(
                        id = profile?.id ?: "default",
                        userId = profile?.userId ?: "",
                        name = name.trim(), phone = phone.trim(), email = email.trim(),
                        bankName = bankName.trim(), accountNumber = accountNumber.trim(),
                        accountHolder = accountHolder.trim(), businessNumber = businessNumber.trim(),
                        address = address.trim(), taxRate = taxRate.toDoubleOrNull() ?: 3.3
                    )
                    onSave(newProfile)
                    onBack()
                },
                enabled = hasChanges
            ) {
                Text("저장", fontWeight = FontWeight.SemiBold, color = if (hasChanges) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 기본 정보
            SectionTitle("기본 정보")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ProfileField(name, { name = it }, "이름", Icons.Default.Person, focusManager)
                    ProfileField(phone, { phone = it }, "연락처", Icons.Default.Phone, focusManager, KeyboardType.Phone, "010-0000-0000")
                    ProfileField(email, { email = it }, "이메일 (정산서 표시용)", Icons.Default.Email, focusManager, KeyboardType.Email)
                }
            }

            // 계좌 정보
            SectionTitle("계좌 정보")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ProfileField(bankName, { bankName = it }, "은행명", Icons.Default.AccountBalance, focusManager, placeholder = "예: 신한은행")
                    ProfileField(accountNumber, { accountNumber = it }, "계좌번호", Icons.Default.CreditCard, focusManager, KeyboardType.Number, "'-' 없이 입력")
                    ProfileField(accountHolder, { accountHolder = it }, "예금주", Icons.Default.Person, focusManager)
                }
            }

            // 사업자 정보
            SectionTitle("사업자 정보 (선택)")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ProfileField(businessNumber, { businessNumber = it }, "사업자등록번호", Icons.Default.Badge, focusManager, placeholder = "000-00-00000")
                    ProfileField(address, { address = it }, "주소", Icons.Default.LocationOn, focusManager)
                }
            }

            // 세율 설정
            SectionTitle("세율 설정")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = taxRate,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) taxRate = it },
                        label = { Text("세율 (%)") },
                        leadingIcon = { Icon(Icons.Default.Percent, contentDescription = "세율", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        supportingText = { Text("기본 세율: 3.3%") }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        listOf("3.3", "8.8", "10").forEach { rate ->
                            FilterChip(onClick = { taxRate = rate }, label = { Text("$rate%", fontSize = 13.sp) }, selected = taxRate == rate)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(150.dp))
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("저장하지 않은 변경사항", fontWeight = FontWeight.Bold) },
            text = { Text("변경사항을 저장하지 않고 나가시겠습니까?") },
            confirmButton = { Button(onClick = { showSaveDialog = false; onBack() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("나가기") } },
            dismissButton = { TextButton(onClick = { showSaveDialog = false }) { Text("계속 수정") } }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    focusManager: androidx.compose.ui.focus.FocusManager,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        placeholder = placeholder?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, keyboardType = keyboardType, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )
}