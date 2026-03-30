package com.xommore.freelancerapp.service

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.xommore.freelancerapp.data.model.Project
import com.xommore.freelancerapp.data.model.PropItem
import com.xommore.freelancerapp.data.model.UserProfile
import com.xommore.freelancerapp.ui.components.formatCurrency
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator(private val context: Context) {

    companion object {
        private const val TAG = "PdfGenerator"
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN_LEFT = 50f
        private const val MARGIN_TOP = 50f
        private const val MARGIN_RIGHT = 50f
        private const val COLOR_PRIMARY = 0xFF1E3A5F.toInt()
        private const val COLOR_TEXT = 0xFF333333.toInt()
        private const val COLOR_TEXT_SECONDARY = 0xFF666666.toInt()
        private const val COLOR_LINE = 0xFFDDDDDD.toInt()
        private const val COLOR_HIGHLIGHT_BG = 0xFFF5F7FA.toInt()
    }

    sealed class PdfResult {
        data class Success(val file: File, val uri: Uri) : PdfResult()
        data class Error(val message: String) : PdfResult()
    }

    fun generateStatementPdf(
        year: Int,
        month: Int,
        projects: List<Project>,
        userProfile: UserProfile?,
        clientName: String? = null,
        propsMap: Map<String, Long> = emptyMap(),
        propItems: Map<String, List<PropItem>> = emptyMap()
    ): PdfResult {
        return try {
            val document = PdfDocument()

            val totalLabor = projects.sumOf { it.totalLabor }
            val totalTax = projects.sumOf { it.tax }
            val netIncome = projects.sumOf { it.netIncome }
            val totalProps = propsMap.values.sum()
            val finalAmount = netIncome + totalProps

            // === 페이지 1: 정산서 본문 ===
            val page1Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page1 = document.startPage(page1Info)
            val canvas1 = page1.canvas

            var y = drawHeader(canvas1, year, month)
            y = drawProfileInfo(canvas1, y, userProfile)
            y = drawSummary(canvas1, y, totalLabor, totalTax, netIncome, totalProps, finalAmount)
            y = drawProjectList(canvas1, y, projects, propsMap)
            drawFooter(canvas1, userProfile)

            document.finishPage(page1)

            // === 페이지 2+: 영수증 이미지 첨부 ===
            val allReceipts = mutableListOf<Pair<String, String>>() // (프로젝트명/소품명, receiptUri)
            propItems.forEach { (projectId, items) ->
                val projectName = projects.find { it.id == projectId }?.brand ?: "프로젝트"
                items.forEach { item ->
                    item.receiptUri?.let { uri ->
                        allReceipts.add("$projectName - ${item.name}" to uri)
                    }
                }
            }

            if (allReceipts.isNotEmpty()) {
                var receiptPageNum = 2
                var receiptY = MARGIN_TOP

                var currentPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, receiptPageNum).create()
                var currentPage = document.startPage(currentPageInfo)
                var currentCanvas = currentPage.canvas

                // 영수증 페이지 제목
                val titlePaint = Paint().apply { isAntiAlias = true; color = COLOR_PRIMARY; textSize = 18f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
                currentCanvas.drawText("영수증 첨부", MARGIN_LEFT, receiptY + 18f, titlePaint)
                receiptY += 40f

                allReceipts.forEachIndexed { index, (label, uriString) ->
                    val bitmap = loadReceiptBitmap(uriString)

                    // 라벨 높이 + 이미지 높이 계산
                    val labelHeight = 25f
                    val imageHeight = if (bitmap != null) {
                        val scale = (PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT) / bitmap.width.toFloat()
                        (bitmap.height * scale).coerceAtMost(350f)
                    } else 0f
                    val totalBlockHeight = labelHeight + imageHeight + 20f

                    // 페이지 넘침 체크
                    if (receiptY + totalBlockHeight > PAGE_HEIGHT - 60f) {
                        document.finishPage(currentPage)
                        receiptPageNum++
                        currentPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, receiptPageNum).create()
                        currentPage = document.startPage(currentPageInfo)
                        currentCanvas = currentPage.canvas
                        receiptY = MARGIN_TOP
                    }

                    // 라벨
                    val labelPaint = Paint().apply { isAntiAlias = true; color = COLOR_TEXT; textSize = 12f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
                    currentCanvas.drawText("${index + 1}. $label", MARGIN_LEFT, receiptY + 12f, labelPaint)
                    receiptY += labelHeight

                    // 이미지
                    if (bitmap != null) {
                        val maxWidth = (PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT).toInt()
                        val scale = maxWidth.toFloat() / bitmap.width.toFloat()
                        val drawHeight = (bitmap.height * scale).coerceAtMost(350f).toInt()
                        val drawWidth = if (bitmap.height * scale > 350f) {
                            (bitmap.width * (350f / bitmap.height)).toInt()
                        } else {
                            maxWidth
                        }

                        val destRect = Rect(
                            MARGIN_LEFT.toInt(),
                            receiptY.toInt(),
                            MARGIN_LEFT.toInt() + drawWidth,
                            receiptY.toInt() + drawHeight
                        )
                        currentCanvas.drawBitmap(bitmap, null, destRect, Paint().apply { isAntiAlias = true })
                        receiptY += drawHeight + 15f

                        bitmap.recycle()
                    } else {
                        val noPaint = Paint().apply { isAntiAlias = true; color = COLOR_TEXT_SECONDARY; textSize = 11f }
                        currentCanvas.drawText("(이미지를 불러올 수 없습니다)", MARGIN_LEFT, receiptY + 12f, noPaint)
                        receiptY += 25f
                    }

                    // 구분선
                    val linePaint = Paint().apply { color = COLOR_LINE; strokeWidth = 0.5f }
                    currentCanvas.drawLine(MARGIN_LEFT, receiptY, PAGE_WIDTH - MARGIN_RIGHT, receiptY, linePaint)
                    receiptY += 10f
                }

                document.finishPage(currentPage)
            }

            // 파일 저장
            val clientSuffix = clientName?.let { "_${it}" } ?: ""
            val fileName = "정산서_${year}년_${month}월${clientSuffix}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            Log.d(TAG, "PDF created with ${allReceipts.size} receipts: ${file.absolutePath}")
            PdfResult.Success(file, uri)
        } catch (e: Exception) {
            Log.e(TAG, "PDF creation failed: ${e.message}", e)
            PdfResult.Error("PDF 생성 실패: ${e.message}")
        }
    }

    /**
     * 영수증 이미지 로드
     */
    private fun loadReceiptBitmap(uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            if (uriString.startsWith("file://")) {
                val file = File(uri.path ?: return null)
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
            } else {
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load receipt image: $uriString, ${e.message}")
            null
        }
    }

    // =====================================================
    // 기존 그리기 함수들
    // =====================================================

    private fun drawHeader(canvas: Canvas, year: Int, month: Int): Float {
        val paint = Paint().apply { isAntiAlias = true }
        var y = MARGIN_TOP
        paint.apply { color = COLOR_PRIMARY; textSize = 28f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText("정 산 서", MARGIN_LEFT, y + 28f, paint)
        paint.apply { color = COLOR_TEXT_SECONDARY; textSize = 14f; typeface = Typeface.DEFAULT }
        val dateText = "${year}년 ${month}월"
        canvas.drawText(dateText, PAGE_WIDTH - MARGIN_RIGHT - paint.measureText(dateText), y + 28f, paint)
        y += 50f
        paint.apply { color = COLOR_PRIMARY; strokeWidth = 2f }
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, paint)
        return y + 30f
    }

    private fun drawProfileInfo(canvas: Canvas, startY: Float, profile: UserProfile?): Float {
        if (profile == null || (profile.name.isBlank() && profile.bankName.isBlank())) return startY
        val paint = Paint().apply { isAntiAlias = true }
        var y = startY
        paint.apply { color = COLOR_PRIMARY; textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText("발행자 정보", MARGIN_LEFT, y, paint)
        y += 25f
        paint.apply { color = COLOR_TEXT; textSize = 12f; typeface = Typeface.DEFAULT }
        if (profile.name.isNotBlank()) { canvas.drawText("성명: ${profile.name}", MARGIN_LEFT, y, paint); y += 18f }
        if (profile.phone.isNotBlank()) { canvas.drawText("연락처: ${profile.phone}", MARGIN_LEFT, y, paint); y += 18f }
        if (profile.email.isNotBlank()) { canvas.drawText("이메일: ${profile.email}", MARGIN_LEFT, y, paint); y += 18f }
        if (profile.bankName.isNotBlank() && profile.accountNumber.isNotBlank()) { canvas.drawText("입금계좌: ${profile.bankName} ${profile.accountNumber} (${profile.accountHolder})", MARGIN_LEFT, y, paint); y += 18f }
        if (profile.businessNumber.isNotBlank()) { canvas.drawText("사업자번호: ${profile.businessNumber}", MARGIN_LEFT, y, paint); y += 18f }
        y += 15f
        paint.apply { color = COLOR_LINE; strokeWidth = 1f }
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, paint)
        return y + 25f
    }

    private fun drawSummary(canvas: Canvas, startY: Float, totalLabor: Long, totalTax: Long, netIncome: Long, totalProps: Long, finalAmount: Long): Float {
        val paint = Paint().apply { isAntiAlias = true }
        var y = startY
        paint.apply { color = COLOR_PRIMARY; textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText("정산 요약", MARGIN_LEFT, y, paint)
        y += 25f
        paint.apply { color = COLOR_HIGHLIGHT_BG; style = Paint.Style.FILL }
        canvas.drawRect(MARGIN_LEFT, y - 5f, PAGE_WIDTH - MARGIN_RIGHT, y + 125f, paint)
        paint.apply { color = COLOR_TEXT; textSize = 12f; typeface = Typeface.DEFAULT; style = Paint.Style.FILL }
        val rightX = PAGE_WIDTH - MARGIN_RIGHT - 10f
        y += 15f
        canvas.drawText("총 인건비", MARGIN_LEFT + 10f, y, paint)
        val laborText = formatCurrency(totalLabor); canvas.drawText(laborText, rightX - paint.measureText(laborText), y, paint)
        y += 20f
        canvas.drawText("세금 (3.3%)", MARGIN_LEFT + 10f, y, paint)
        paint.color = 0xFFE53935.toInt(); val taxText = "-${formatCurrency(totalTax)}"; canvas.drawText(taxText, rightX - paint.measureText(taxText), y, paint)
        y += 20f; paint.color = COLOR_TEXT
        canvas.drawText("실수령 인건비", MARGIN_LEFT + 10f, y, paint)
        val netText = formatCurrency(netIncome); canvas.drawText(netText, rightX - paint.measureText(netText), y, paint)
        y += 15f; paint.apply { color = COLOR_LINE; strokeWidth = 1f }; canvas.drawLine(MARGIN_LEFT + 10f, y, rightX, y, paint)
        y += 18f; paint.apply { color = COLOR_TEXT; textSize = 12f; typeface = Typeface.DEFAULT }
        canvas.drawText("소품비 (클라이언트 청구)", MARGIN_LEFT + 10f, y, paint)
        val propsText = formatCurrency(totalProps); canvas.drawText(propsText, rightX - paint.measureText(propsText), y, paint)
        y += 15f; paint.apply { color = COLOR_LINE; strokeWidth = 1f }; canvas.drawLine(MARGIN_LEFT + 10f, y, rightX, y, paint)
        y += 22f; paint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 14f; color = COLOR_PRIMARY }
        canvas.drawText("최종 정산 금액", MARGIN_LEFT + 10f, y, paint)
        val finalText = formatCurrency(finalAmount); canvas.drawText(finalText, rightX - paint.measureText(finalText), y, paint)
        return y + 40f
    }

    private fun drawProjectList(canvas: Canvas, startY: Float, projects: List<Project>, propsMap: Map<String, Long>): Float {
        val paint = Paint().apply { isAntiAlias = true }
        val dateFormat = SimpleDateFormat("M/d", Locale.KOREA)
        var y = startY
        paint.apply { color = COLOR_PRIMARY; textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText("상세 내역 (${projects.size}건)", MARGIN_LEFT, y, paint); y += 25f

        projects.forEachIndexed { index, project ->
            if (y > PAGE_HEIGHT - 120f) return y
            val propAmount = propsMap[project.id] ?: 0L; val totalAmount = project.netIncome + propAmount
            val cardHeight = if (propAmount > 0) 75f else 60f
            paint.apply { color = if (index % 2 == 0) COLOR_HIGHLIGHT_BG else Color.WHITE; style = Paint.Style.FILL }
            canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + cardHeight, paint)
            paint.apply { color = COLOR_LINE; style = Paint.Style.STROKE; strokeWidth = 0.5f }
            canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + cardHeight, paint)
            var contentY = y + 16f; val leftCol = MARGIN_LEFT + 10f; val rightCol = PAGE_WIDTH - MARGIN_RIGHT - 10f
            paint.apply { color = COLOR_TEXT; textSize = 12f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); style = Paint.Style.FILL }
            canvas.drawText(project.brand, leftCol, contentY, paint)
            paint.color = COLOR_PRIMARY; val totalText = formatCurrency(totalAmount); canvas.drawText(totalText, rightCol - paint.measureText(totalText), contentY, paint)
            contentY += 18f; paint.apply { color = COLOR_TEXT_SECONDARY; textSize = 10f; typeface = Typeface.DEFAULT }
            canvas.drawText("${dateFormat.format(Date(project.startDate))} ~ ${dateFormat.format(Date(project.endDate))}  |  ${project.workType.displayName}  |  ${project.cuts}컷", leftCol, contentY, paint)
            contentY += 16f
            if (propAmount > 0) {
                paint.color = COLOR_TEXT_SECONDARY; canvas.drawText("인건비: ${formatCurrency(project.netIncome)}", leftCol, contentY, paint)
                paint.color = 0xFF1976D2.toInt(); canvas.drawText("소품비: ${formatCurrency(propAmount)}", leftCol + 150f, contentY, paint)
            } else { paint.color = COLOR_TEXT_SECONDARY; canvas.drawText("인건비: ${formatCurrency(project.netIncome)}", leftCol, contentY, paint) }
            y += cardHeight + 5f
        }
        return y + 10f
    }

    private fun drawFooter(canvas: Canvas, profile: UserProfile?) {
        val paint = Paint().apply { isAntiAlias = true; color = COLOR_TEXT_SECONDARY; textSize = 10f }
        val today = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(Date())
        canvas.drawText("발행일: $today", MARGIN_LEFT, PAGE_HEIGHT - 40f, paint)
        if (profile?.name?.isNotBlank() == true) { val t = "발행자: ${profile.name}"; canvas.drawText(t, PAGE_WIDTH - MARGIN_RIGHT - paint.measureText(t), PAGE_HEIGHT - 40f, paint) }
        val appText = "프리랜서 정산 앱"; canvas.drawText(appText, (PAGE_WIDTH - paint.measureText(appText)) / 2, PAGE_HEIGHT - 25f, paint)
    }

    fun createShareIntent(uri: Uri, fileName: String) = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); putExtra(Intent.EXTRA_SUBJECT, fileName); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    fun createViewIntent(uri: Uri) = Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
}