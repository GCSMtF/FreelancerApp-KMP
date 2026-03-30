package com.xommore.freelancerapp.service

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xommore.freelancerapp.data.model.Client
import com.xommore.freelancerapp.data.model.Project
import com.xommore.freelancerapp.data.model.UserProfile
import com.xommore.freelancerapp.ui.theme.*
import kotlinx.coroutines.launch

@Composable
actual fun BackupRestoreButtons(
    backupData: BackupData,
    onRestoreMerge: (List<Project>, List<Client>, UserProfile?) -> Unit,
    onRestoreOverwrite: (List<Project>, List<Client>, UserProfile?) -> Unit,
    onClearData: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val backupManager = remember { BackupManager(context) }

    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var restoreResult by remember { mutableStateOf<BackupManager.RestoreResult.Success?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            isRestoring = true
            coroutineScope.launch {
                when (val result = backupManager.restoreFromUri(it)) {
                    is BackupManager.RestoreResult.Success -> {
                        restoreResult = result
                        showRestoreDialog = true
                    }
                    is BackupManager.RestoreResult.Error -> {
                        Toast.makeText(context, "❌ ${result.message}", Toast.LENGTH_LONG).show()
                    }
                }
                isRestoring = false
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 현재 데이터 현황
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("현재 데이터 현황", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DataCountItem("프로젝트", backupData.projects.size)
                    DataCountItem("클라이언트", backupData.clients.size)
                    DataCountItem("프로필", if (backupData.userProfile != null) 1 else 0)
                }
            }
        }

        // 백업 버튼
        Button(
            onClick = {
                isBackingUp = true
                coroutineScope.launch {
                    val result = backupManager.createBackup(backupData.projects, backupData.clients, backupData.userProfile)
                    isBackingUp = false
                    when (result) {
                        is BackupManager.BackupResult.Success -> {
                            val intent = backupManager.createShareIntent(result.uri, result.file.name)
                            context.startActivity(Intent.createChooser(intent, "백업 파일 저장"))
                            Toast.makeText(context, "✅ 백업 파일이 생성되었습니다", Toast.LENGTH_SHORT).show()
                        }
                        is BackupManager.BackupResult.Error -> {
                            Toast.makeText(context, "❌ ${result.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isBackingUp && (backupData.projects.isNotEmpty() || backupData.clients.isNotEmpty())
        ) {
            if (isBackingUp) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("백업 중...")
            } else {
                Icon(Icons.Default.CloudUpload, contentDescription = "백업", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("백업 파일 생성", fontWeight = FontWeight.SemiBold)
            }
        }

        // 복원 버튼
        Button(
            onClick = { filePickerLauncher.launch(arrayOf("application/json")) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            enabled = !isRestoring
        ) {
            if (isRestoring) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("파일 읽는 중...")
            } else {
                Icon(Icons.Default.CloudDownload, contentDescription = "복원", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("백업 파일에서 복원", fontWeight = FontWeight.SemiBold)
            }
        }

        // 데이터 초기화 버튼
        OutlinedButton(
            onClick = { showClearDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = "삭제", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("전체 데이터 삭제", fontWeight = FontWeight.SemiBold)
        }
    }

    // 복원 옵션 다이얼로그
    if (showRestoreDialog && restoreResult != null) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false; restoreResult = null },
            title = { Text("데이터 복원", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("백업 파일 내용:")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("프로젝트: ${restoreResult!!.projects.size}개", fontSize = 14.sp)
                            Text("클라이언트: ${restoreResult!!.clients.size}개", fontSize = 14.sp)
                            Text("프로필: ${if (restoreResult!!.profile != null) "있음" else "없음"}", fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("복원 방식을 선택하세요:", fontWeight = FontWeight.Medium)
                }
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onRestoreMerge(restoreResult!!.projects, restoreResult!!.clients, restoreResult!!.profile)
                            showRestoreDialog = false; restoreResult = null
                            Toast.makeText(context, "✅ 데이터가 병합되었습니다", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue)
                    ) { Text("병합 (기존 데이터 유지)") }

                    Button(
                        onClick = {
                            onRestoreOverwrite(restoreResult!!.projects, restoreResult!!.clients, restoreResult!!.profile)
                            showRestoreDialog = false; restoreResult = null
                            Toast.makeText(context, "✅ 데이터가 복원되었습니다", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("덮어쓰기 (기존 데이터 삭제)") }
                }
            },
            dismissButton = { TextButton(onClick = { showRestoreDialog = false; restoreResult = null }) { Text("취소") } }
        )
    }

    // 전체 삭제 확인 다이얼로그
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("⚠️ 전체 데이터 삭제", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text("정말로 모든 데이터를 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다!") },
            confirmButton = {
                Button(
                    onClick = { onClearData(); showClearDialog = false; Toast.makeText(context, "모든 데이터가 삭제되었습니다", Toast.LENGTH_SHORT).show() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("삭제") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("취소") } }
        )
    }
}

@Composable
private fun DataCountItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$count", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}