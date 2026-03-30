package com.xommore.freelancerapp.service

import androidx.compose.runtime.Composable
import com.xommore.freelancerapp.ui.components.EditablePropItem

/**
 * OCR 영수증 스캔 기능 (expect/actual)
 * Android: ML Kit + CameraX
 * iOS: 미구현
 */

@Composable
expect fun OcrScanButtons(
    isScanning: Boolean,
    onScanResult: (List<EditablePropItem>) -> Unit,
    onScanningChange: (Boolean) -> Unit
)