package com.xommore.freelancerapp.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume

data class ReceiptScanResult(
    val detectedAmount: Long?,           // 인식된 금액 (가장 유력한 총액)
    val allAmounts: List<Long>,          // 인식된 모든 금액 후보
    val rawText: String,                 // 원본 텍스트
    val imageUri: Uri?                   // 저장된 영수증 이미지 URI
)

class ReceiptScanner(private val context: Context) {

    private val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    sealed class ScanResult {
        data class Success(val result: ReceiptScanResult) : ScanResult()
        data class Error(val message: String) : ScanResult()
    }

    suspend fun scanReceipt(imageUri: Uri): ScanResult {
        return try {
            val bitmap = loadBitmap(imageUri) ?: return ScanResult.Error("이미지를 불러올 수 없습니다")
            processImage(bitmap)
        } catch (e: Exception) {
            ScanResult.Error("영수증 스캔 실패: ${e.message}")
        }
    }

    suspend fun scanReceiptFromBitmap(bitmap: Bitmap): ScanResult {
        return try {
            processImage(bitmap)
        } catch (e: Exception) {
            ScanResult.Error("영수증 스캔 실패: ${e.message}")
        }
    }

    private suspend fun processImage(bitmap: Bitmap): ScanResult {
        val text = recognizeText(InputImage.fromBitmap(bitmap, 0))
        val savedUri = saveReceiptImage(bitmap)

        // 금액만 추출
        val allAmounts = extractAllAmounts(text)
        val detectedAmount = findBestAmount(text, allAmounts)

        return ScanResult.Success(
            ReceiptScanResult(
                detectedAmount = detectedAmount,
                allAmounts = allAmounts.distinct().sortedDescending(),
                rawText = text,
                imageUri = savedUri
            )
        )
    }

    /**
     * 텍스트에서 모든 금액 후보 추출
     */
    private fun extractAllAmounts(text: String): List<Long> {
        val amounts = mutableListOf<Long>()

        // 쉼표 포함 금액 (예: 12,500 / 1,234,567)
        Regex("""(\d{1,3}(?:,\d{3})+)""").findAll(text).forEach { match ->
            match.value.replace(",", "").toLongOrNull()?.let { if (it >= 100) amounts.add(it) }
        }

        // 쉼표 없는 4자리 이상 금액
        Regex("""(?<!\d)(\d{4,9})(?!\d)""").findAll(text).forEach { match ->
            match.groupValues[1].toLongOrNull()?.let { if (it >= 1000 && it <= 99999999) amounts.add(it) }
        }

        return amounts
    }

    /**
     * 총액/합계 키워드 근처의 금액을 우선 선택
     */
    private fun findBestAmount(text: String, allAmounts: List<Long>): Long? {
        if (allAmounts.isEmpty()) return null

        val lines = text.split("\n").map { it.trim() }
        val totalKeywords = listOf(
            "합계", "총액", "총합", "결제금액", "결제 금액", "총 금액",
            "Total", "TOTAL", "합 계", "카드결제", "받을금액",
            "청구금액", "실결제", "승인금액", "결제액", "현금"
        )

        // 총액 키워드가 있는 라인에서 금액 찾기
        for (line in lines) {
            if (totalKeywords.any { line.contains(it) }) {
                // 쉼표 포함 금액 우선
                Regex("""(\d{1,3}(?:,\d{3})+)""").findAll(line).lastOrNull()?.let { match ->
                    match.value.replace(",", "").toLongOrNull()?.let { if (it >= 100) return it }
                }
                // 쉼표 없는 금액
                Regex("""(\d{4,9})""").findAll(line).lastOrNull()?.let { match ->
                    match.value.toLongOrNull()?.let { if (it >= 1000) return it }
                }
            }
        }

        // 키워드 없으면 가장 큰 금액 반환
        return allAmounts.maxOrNull()
    }

    private fun loadBitmap(uri: Uri): Bitmap? = try {
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    } catch (_: Exception) { null }

    private suspend fun recognizeText(image: InputImage): String = suspendCancellableCoroutine { cont ->
        recognizer.process(image)
            .addOnSuccessListener { cont.resume(it.text) }
            .addOnFailureListener { cont.resume("") }
    }

    /**
     * 영수증 이미지 저장 (정산서 첨부용)
     */
    fun saveReceiptImage(bitmap: Bitmap): Uri? = try {
        val fileName = "receipt_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
        val dir = File(context.filesDir, "receipts").also { if (!it.exists()) it.mkdirs() }
        val file = File(dir, fileName)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        Uri.fromFile(file)
    } catch (_: Exception) { null }

    fun close() { recognizer.close() }
}