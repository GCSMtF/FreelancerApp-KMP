package com.xommore.freelancerapp.service

import androidx.compose.runtime.Composable
import com.xommore.freelancerapp.ui.components.EditablePropItem

@Composable
actual fun OcrScanButtons(
    isScanning: Boolean,
    onScanResult: (List<EditablePropItem>) -> Unit,
    onScanningChange: (Boolean) -> Unit
) {
    // iOS에서는 OCR 미지원 — 빈 구현
}