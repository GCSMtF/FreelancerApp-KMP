package com.xommore.freelancerapp.service

import androidx.compose.runtime.Composable
import com.xommore.freelancerapp.data.model.Client
import com.xommore.freelancerapp.data.model.Project
import com.xommore.freelancerapp.data.model.UserProfile

/**
 * 백업/복원 기능 (expect/actual)
 * Android: BackupManager + FileProvider + ActivityResult
 * iOS: 미구현 (Step 12)
 */

data class BackupData(
    val projects: List<Project>,
    val clients: List<Client>,
    val userProfile: UserProfile?
)

@Composable
expect fun BackupRestoreButtons(
    backupData: BackupData,
    onRestoreMerge: (List<Project>, List<Client>, UserProfile?) -> Unit,
    onRestoreOverwrite: (List<Project>, List<Client>, UserProfile?) -> Unit,
    onClearData: () -> Unit
)