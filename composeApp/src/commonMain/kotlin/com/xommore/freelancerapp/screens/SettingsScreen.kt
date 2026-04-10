package com.xommore.freelancerapp.ui.screens
import com.xommore.freelancerapp.service.DepositAlertSettingsScreen as PlatformDepositSettingsScreen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xommore.freelancerapp.ui.theme.*
import com.xommore.freelancerapp.viewmodel.AuthViewModel
import com.xommore.freelancerapp.viewmodel.MainViewModel
import com.xommore.freelancerapp.service.BackupData
import com.xommore.freelancerapp.service.BackupRestoreButtons
import com.xommore.freelancerapp.service.EmailSettingsScreen as PlatformEmailSettingsScreen

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    authViewModel: AuthViewModel,
    userEmail: String? = null
) {
    val clients by viewModel.clients.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showProfileSettings by remember { mutableStateOf(false) }
    var showClientManagement by remember { mutableStateOf(false) }
    var showBackupRestore by remember { mutableStateOf(false) }
    var showEmailSettings by remember { mutableStateOf(false) }
    var showDepositSettings by remember { mutableStateOf(false) }

    // 하위 화면 분기
    when {
        showProfileSettings -> {
            ProfileSettingsScreen(
                profile = userProfile,
                onBack = { showProfileSettings = false },
                onSave = { viewModel.saveUserProfile(it) }
            )
        }
        showClientManagement -> {
            ClientManagementScreen(
                clients = clients,
                onBack = { showClientManagement = false },
                onAddClient = { viewModel.addClient(it) },
                onUpdateClient = { viewModel.updateClient(it) },
                onDeleteClient = { viewModel.deleteClient(it) }
            )
        }
        showBackupRestore -> {
            BackupRestoreScreen(
                backupData = BackupData(projects, clients, userProfile),
                onBack = { showBackupRestore = false },
                onRestoreMerge = { p, c, u -> viewModel.restoreMerge(p, c, u) },
                onRestoreOverwrite = { p, c, u -> viewModel.restoreOverwrite(p, c, u) },
                onClearData = { viewModel.clearAllData() }
            )
        }
        showEmailSettings -> {
            PlatformEmailSettingsScreen(onBack = { showEmailSettings = false })
        }
        showDepositSettings -> {
            PlatformDepositSettingsScreen(
                clients = clients,
                onBack = { showDepositSettings = false }
            )
        }
        else -> {
            // 메인 설정 화면
            Column(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text("설정", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 계정 정보
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(28.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = "프로필", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        userProfile?.name?.ifBlank { "내 계정" } ?: "내 계정",
                                        fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        userEmail ?: "이메일 없음",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 관리 섹션
                    item {
                        Text("관리", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                    }

                    item {
                        SettingsMenuItem(
                            icon = Icons.Default.Person, iconColor = MaterialTheme.colorScheme.primary,
                            title = "내 프로필",
                            subtitle = if (userProfile?.name?.isNotBlank() == true)
                                "${userProfile?.name} · ${userProfile?.phone?.ifBlank { "연락처 미등록" }}"
                            else "이름, 연락처, 계좌 정보 등록",
                            onClick = { showProfileSettings = true }
                        )
                    }

                    item {
                        SettingsMenuItem(
                            icon = Icons.Default.Business, iconColor = Blue,
                            title = "클라이언트 관리",
                            subtitle = "${clients.size}개의 클라이언트",
                            onClick = { showClientManagement = true }
                        )
                    }

                    item {
                        SettingsMenuItem(
                            icon = Icons.Default.Email, iconColor = StatusPaidText,
                            title = "이메일 발송 설정",
                            subtitle = "Gmail 앱 비밀번호 설정",
                            onClick = { showEmailSettings = true }
                        )
                    }

                    // 입금 알림 섹션
                    item {
                        Text("입금 알림", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                    }

                    item {
                        SettingsMenuItem(
                            icon = Icons.Default.Notifications, iconColor = StatusPaidText,
                            title = "입금 알림 설정",
                            subtitle = "은행 입금 감지 및 알림",
                            onClick = { showDepositSettings = true }
                        )
                    }

                    // 데이터 관리
                    item {
                        Text("데이터 관리", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                    }

                    item {
                        SettingsMenuItem(
                            icon = Icons.Default.CloudUpload, iconColor = StatusPaidText,
                            title = "데이터 백업/복원",
                            subtitle = "프로젝트 ${projects.size}개, 클라이언트 ${clients.size}개",
                            onClick = { showBackupRestore = true }
                        )
                    }

                    // 앱 정보
                    item {
                        Text("앱 정보", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(Icons.Default.Info, contentDescription = "버전", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                    Text("앱 버전", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Text("1.0.0", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            }
                        }
                    }

                    // 로그아웃
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = "로그아웃", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("로그아웃", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }

            // 로그아웃 다이얼로그
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("로그아웃", fontWeight = FontWeight.Bold) },
                    text = { Text("정말 로그아웃 하시겠습니까?") },
                    confirmButton = {
                        Button(onClick = { showLogoutDialog = false; authViewModel.signOut() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("로그아웃") }
                    },
                    dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("취소") } }
                )
            }
        }
    }
}

@Composable
private fun SettingsMenuItem(icon: ImageVector, iconColor: Color, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(42.dp).background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "이동", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}