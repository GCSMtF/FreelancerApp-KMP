package com.xommore.freelancerapp.service

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xommore.freelancerapp.data.model.Client
import com.xommore.freelancerapp.data.model.Project
import com.xommore.freelancerapp.data.model.UserProfile

@Composable
actual fun BackupRestoreButtons(
    backupData: BackupData,
    onRestoreMerge: (List<Project>, List<Client>, UserProfile?) -> Unit,
    onRestoreOverwrite: (List<Project>, List<Client>, UserProfile?) -> Unit,
    onClearData: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
        Text("백업/복원은 iOS에서 아직 지원되지 않습니다", fontSize = 14.sp)
    }
}