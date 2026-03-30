package com.xommore.freelancerapp.service

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
actual fun PdfExportButton(
    enabled: Boolean,
    pdfRequest: PdfRequest,
    onResult: (Boolean, String) -> Unit
) {
    Button(
        onClick = { onResult(false, "iOS에서는 아직 지원되지 않습니다") },
        shape = RoundedCornerShape(14.dp),
        enabled = enabled
    ) {
        Text("📄 PDF", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
actual fun ClipboardCopyButton(
    text: String,
    onCopied: () -> Unit
) {
    OutlinedButton(
        onClick = { /* TODO: iOS 클립보드 구현 */ },
        shape = RoundedCornerShape(14.dp)
    ) {
        Text("📋 복사하기", fontWeight = FontWeight.SemiBold)
    }
}