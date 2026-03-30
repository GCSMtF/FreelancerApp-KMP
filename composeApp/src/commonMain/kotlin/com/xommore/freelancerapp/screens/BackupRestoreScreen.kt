package com.xommore.freelancerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xommore.freelancerapp.data.model.Client
import com.xommore.freelancerapp.data.model.Project
import com.xommore.freelancerapp.data.model.UserProfile
import com.xommore.freelancerapp.service.BackupData
import com.xommore.freelancerapp.service.BackupRestoreButtons

@Composable
fun BackupRestoreScreen(
    backupData: BackupData,
    onBack: () -> Unit,
    onRestoreMerge: (List<Project>, List<Client>, UserProfile?) -> Unit,
    onRestoreOverwrite: (List<Project>, List<Client>, UserProfile?) -> Unit,
    onClearData: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("데이터 백업/복원", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
        ) {
            // 플랫폼별 백업/복원 버튼 (expect/actual)
            BackupRestoreButtons(
                backupData = backupData,
                onRestoreMerge = onRestoreMerge,
                onRestoreOverwrite = onRestoreOverwrite,
                onClearData = onClearData
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}