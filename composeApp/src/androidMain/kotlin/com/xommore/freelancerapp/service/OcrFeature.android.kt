package com.xommore.freelancerapp.service

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.xommore.freelancerapp.ui.components.EditablePropItem
import com.xommore.freelancerapp.ui.components.formatCurrency
import com.xommore.freelancerapp.ui.theme.*
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
actual fun OcrScanButtons(
    isScanning: Boolean,
    onScanResult: (List<EditablePropItem>) -> Unit,
    onScanningChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showCamera by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<ReceiptScanResult?>(null) }
    var showAmountConfirm by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            onScanningChange(true)
            coroutineScope.launch {
                val scanner = ReceiptScanner(context)
                when (val result = scanner.scanReceipt(it)) {
                    is ReceiptScanner.ScanResult.Success -> { scanResult = result.result; showAmountConfirm = true }
                    is ReceiptScanner.ScanResult.Error -> { /* 에러 */ }
                }
                onScanningChange(false)
                scanner.close()
            }
        }
    }

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = { showCamera = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = !isScanning) {
            Icon(Icons.Default.CameraAlt, contentDescription = "카메라", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp)); Text("영수증 촬영")
        }
        OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = !isScanning) {
            if (isScanning) { CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) }
            else { Icon(Icons.Default.PhotoLibrary, contentDescription = "갤러리", modifier = Modifier.size(18.dp)) }
            Spacer(modifier = Modifier.width(8.dp)); Text(if (isScanning) "스캔 중..." else "갤러리")
        }
    }

    // 카메라 다이얼로그
    if (showCamera) {
        CameraDialog(
            onImageCaptured = { bitmap ->
                showCamera = false
                onScanningChange(true)
                coroutineScope.launch {
                    val scanner = ReceiptScanner(context)
                    when (val result = scanner.scanReceiptFromBitmap(bitmap)) {
                        is ReceiptScanner.ScanResult.Success -> { scanResult = result.result; showAmountConfirm = true }
                        is ReceiptScanner.ScanResult.Error -> { /* 에러 */ }
                    }
                    onScanningChange(false)
                    scanner.close()
                }
            },
            onDismiss = { showCamera = false }
        )
    }

    // 금액 확인 다이얼로그
    if (showAmountConfirm && scanResult != null) {
        AmountConfirmDialog(
            scanResult = scanResult!!,
            onConfirm = { name, amount, imageUri ->
                onScanResult(listOf(
                    EditablePropItem(
                        name = name,
                        amount = amount.toString(),
                        receiptUri = imageUri
                    )
                ))
                showAmountConfirm = false; scanResult = null
            },
            onDismiss = { showAmountConfirm = false; scanResult = null }
        )
    }
}

// =====================================================
// 금액 확인 다이얼로그 (간소화)
// =====================================================

@Composable
private fun AmountConfirmDialog(
    scanResult: ReceiptScanResult,
    onConfirm: (name: String, amount: Long, imageUri: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("소품비") }
    var amountText by remember { mutableStateOf(scanResult.detectedAmount?.toString() ?: "") }
    var showAllAmounts by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("영수증 금액 확인", fontWeight = FontWeight.Bold)
                if (scanResult.detectedAmount != null) {
                    Text("인식된 금액: ${formatCurrency(scanResult.detectedAmount)}", fontSize = 13.sp, color = StatusPaidText, modifier = Modifier.padding(top = 4.dp))
                } else {
                    Text("금액을 인식하지 못했습니다. 직접 입력해주세요.", fontSize = 13.sp, color = StatusPendingText, modifier = Modifier.padding(top = 4.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 영수증 이미지 (저장됨 표시)
                if (scanResult.imageUri != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StatusPaidBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Receipt, contentDescription = "영수증", tint = StatusPaidText, modifier = Modifier.size(20.dp))
                            Text("영수증 이미지가 저장되었습니다", fontSize = 13.sp, color = StatusPaidText, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // 소품명 입력
                OutlinedTextField(
                    value = itemName, onValueChange = { itemName = it },
                    label = { Text("소품명") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
                )

                // 금액 입력
                OutlinedTextField(
                    value = amountText, onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("금액") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("원") }
                )

                // 인식된 금액 후보 (여러개일 때)
                if (scanResult.allAmounts.size > 1) {
                    TextButton(onClick = { showAllAmounts = !showAllAmounts }) {
                        Icon(
                            if (showAllAmounts) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "더보기", modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("인식된 금액 후보 ${scanResult.allAmounts.size}개", fontSize = 13.sp)
                    }

                    if (showAllAmounts) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                scanResult.allAmounts.take(10).forEach { amount ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                            .clickable { amountText = amount.toString() }.padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(formatCurrency(amount), fontSize = 14.sp)
                                        if (amount.toString() == amountText) {
                                            Icon(Icons.Default.Check, contentDescription = "선택됨", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount > 0 && itemName.isNotBlank()) {
                        onConfirm(itemName, amount, scanResult.imageUri?.toString())
                    }
                },
                enabled = (amountText.toLongOrNull() ?: 0L) > 0 && itemName.isNotBlank()
            ) { Text("추가", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

// =====================================================
// 카메라 다이얼로그
// =====================================================

@Composable
private fun CameraDialog(onImageCaptured: (Bitmap) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCameraPermission = it }
    LaunchedEffect(Unit) { if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            if (hasCameraPermission) {
                CameraContent(onImageCaptured = onImageCaptured, onDismiss = onDismiss)
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "카메라", modifier = Modifier.size(64.dp), tint = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("카메라 권한이 필요합니다", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onDismiss, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("취소") }
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) { Text("권한 허용") }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraContent(onImageCaptured: (Bitmap) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isCapturing by remember { mutableStateOf(false) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) { onDispose { cameraExecutor.shutdown() } }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx -> PreviewView(ctx).apply { layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); scaleType = PreviewView.ScaleType.FILL_CENTER } },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val future = ProcessCameraProvider.getInstance(context)
                future.addListener({
                    val provider = future.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
                    try { provider.unbindAll(); provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture) } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(context))
            }
        )

        IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
            Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
        }

        Box(modifier = Modifier.fillMaxWidth(0.9f).aspectRatio(0.7f).align(Alignment.Center).border(2.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp)))

        Column(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.7f)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("영수증을 프레임 안에 맞춰주세요", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(20.dp))
            IconButton(
                onClick = {
                    if (!isCapturing) {
                        isCapturing = true
                        imageCapture?.takePicture(ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                val bitmap = imageProxyToBitmap(imageProxy); imageProxy.close(); isCapturing = false
                                bitmap?.let { onImageCaptured(it) }
                            }
                            override fun onError(exception: ImageCaptureException) { isCapturing = false }
                        })
                    }
                },
                modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White)
            ) {
                if (isCapturing) CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Navy, strokeWidth = 3.dp)
                else Icon(Icons.Default.CameraAlt, contentDescription = "촬영", tint = Navy, modifier = Modifier.size(32.dp))
            }
        }
    }
}

private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    val buffer = imageProxy.planes[0].buffer; val bytes = ByteArray(buffer.remaining()); buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val rotation = imageProxy.imageInfo.rotationDegrees
    return if (rotation != 0 && bitmap != null) Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(rotation.toFloat()) }, true) else bitmap
}