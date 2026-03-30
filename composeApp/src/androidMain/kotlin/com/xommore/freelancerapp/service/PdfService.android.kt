package com.xommore.freelancerapp.service

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun PdfExportButton(
    enabled: Boolean,
    pdfRequest: PdfRequest,
    onResult: (Boolean, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }

    Button(
        onClick = {
            isGenerating = true
            coroutineScope.launch {
                val pdfGenerator = PdfGenerator(context)
                val result = withContext(Dispatchers.IO) {
                    pdfGenerator.generateStatementPdf(
                        year = pdfRequest.year,
                        month = pdfRequest.month,
                        projects = pdfRequest.projects,
                        userProfile = pdfRequest.userProfile,
                        clientName = pdfRequest.clientName,
                        propsMap = pdfRequest.propsMap,
                        propItems = pdfRequest.propItems
                    )
                }
                isGenerating = false

                when (result) {
                    is PdfGenerator.PdfResult.Success -> {
                        onResult(true, "PDF가 생성되었습니다")
                        // 공유 인텐트
                        try {
                            val intent = pdfGenerator.createShareIntent(result.uri, result.file.name)
                            context.startActivity(Intent.createChooser(intent, "정산서 공유"))
                        } catch (e: Exception) {
                            // 뷰어로 열기 시도
                            try {
                                val viewIntent = pdfGenerator.createViewIntent(result.uri)
                                context.startActivity(viewIntent)
                            } catch (e2: Exception) {
                                Toast.makeText(context, "PDF 뷰어를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    is PdfGenerator.PdfResult.Error -> {
                        onResult(false, result.message)
                    }
                }
            }
        },
        modifier = Modifier,
        shape = RoundedCornerShape(14.dp),
        enabled = enabled && !isGenerating
    ) {
        if (isGenerating) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text("📄 PDF", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
actual fun ClipboardCopyButton(
    text: String,
    onCopied: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    OutlinedButton(
        onClick = {
            clipboardManager.setText(AnnotatedString(text))
            onCopied()
        },
        shape = RoundedCornerShape(14.dp)
    ) {
        Text("📋 복사하기", fontWeight = FontWeight.SemiBold)
    }
}