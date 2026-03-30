package com.xommore.freelancerapp.service

import androidx.compose.runtime.Composable
import com.xommore.freelancerapp.data.model.Project
import com.xommore.freelancerapp.data.model.UserProfile

/**
 * PDF 생성/공유 기능 (expect/actual)
 * Android: PdfGenerator + Intent
 * iOS: 미구현 (Step 12)
 */

data class PdfRequest(
    val year: Int,
    val month: Int,
    val projects: List<Project>,
    val userProfile: UserProfile?,
    val clientName: String? = null,
    val propsMap: Map<String, Long> = emptyMap()
)

@Composable
expect fun PdfExportButton(
    enabled: Boolean,
    pdfRequest: PdfRequest,
    onResult: (Boolean, String) -> Unit
)

@Composable
expect fun ClipboardCopyButton(
    text: String,
    onCopied: () -> Unit
)