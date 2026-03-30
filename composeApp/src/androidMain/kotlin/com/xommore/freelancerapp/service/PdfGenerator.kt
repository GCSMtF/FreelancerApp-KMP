package com.xommore.freelancerapp.service

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.xommore.freelancerapp.data.model.Project
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
        propsMap: Map<String, Long> = emptyMap()
    ): PdfResult {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val totalLabor = projects.sumOf { it.totalLabor }
            val totalTax = projects.sumOf { it.tax }
            val netIncome = projects.sumOf { it.netIncome }
            val totalProps = propsMap.values.sum()
            val finalAmount = netIncome + totalProps

            var yPosition = drawHeader(canvas, year, month)
            yPosition = drawProfileInfo(canvas, yPosition, userProfile)
            yPosition = drawSummary(canvas, yPosition, totalLabor, totalTax, netIncome, totalProps, finalAmount)
            yPosition = drawProjectList(canvas, yPosition, projects, propsMap)
            drawFooter(canvas, userProfile)

            document.finishPage(page)

            val clientSuffix = clientName?.let { "_${it}" } ?: ""
            val fileName = "정산서_${year}년_${month}월${clientSuffix}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)

            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            Log.d(TAG, "PDF created: ${file.absolutePath}")
            PdfResult.Success(file, uri)
        } catch (e: Exception) {
            Log.e(TAG, "PDF creation failed: ${e.message}", e)
            PdfResult.Error("PDF 생성 실패: ${e.message}")
        }
    }

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
        if (profile.bankName.isNotBlank() && profile.accountNumber.isNotBlank()) {
            canvas.drawText("입금계좌: ${profile.bankName} ${profile.accountNumber} (${profile.accountHolder})", MARGIN_LEFT, y, paint); y += 18f
        }
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
        val laborText = formatCurrency(totalLabor)
        canvas.drawText(laborText, rightX - paint.measureText(laborText), y, paint)

        y += 20f
        canvas.drawText("세금 (3.3%)", MARGIN_LEFT + 10f, y, paint)
        paint.color = 0xFFE53935.toInt()
        val taxText = "-${formatCurrency(totalTax)}"
        canvas.drawText(taxText, rightX - paint.measureText(taxText), y, paint)

        y += 20f
        paint.color = COLOR_TEXT
        canvas.drawText("실수령 인건비", MARGIN_LEFT + 10f, y, paint)
        val netText = formatCurrency(netIncome)
        canvas.drawText(netText, rightX - paint.measureText(netText), y, paint)

        y += 15f
        paint.apply { color = COLOR_LINE; strokeWidth = 1f }
        canvas.drawLine(MARGIN_LEFT + 10f, y, rightX, y, paint)

        y += 18f
        paint.apply { color = COLOR_TEXT; textSize = 12f; typeface = Typeface.DEFAULT }
        canvas.drawText("소품비 (클라이언트 청구)", MARGIN_LEFT + 10f, y, paint)
        val propsText = formatCurrency(totalProps)
        canvas.drawText(propsText, rightX - paint.measureText(propsText), y, paint)

        y += 15f
        paint.apply { color = COLOR_LINE; strokeWidth = 1f }
        canvas.drawLine(MARGIN_LEFT + 10f, y, rightX, y, paint)

        y += 22f
        paint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 14f; color = COLOR_PRIMARY }
        canvas.drawText("최종 정산 금액", MARGIN_LEFT + 10f, y, paint)
        val finalText = formatCurrency(finalAmount)
        canvas.drawText(finalText, rightX - paint.measureText(finalText), y, paint)

        return y + 40f
    }

    private fun drawProjectList(canvas: Canvas, startY: Float, projects: List<Project>, propsMap: Map<String, Long>): Float {
        val paint = Paint().apply { isAntiAlias = true }
        val dateFormat = SimpleDateFormat("M/d", Locale.KOREA)
        var y = startY

        paint.apply { color = COLOR_PRIMARY; textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText("상세 내역 (${projects.size}건)", MARGIN_LEFT, y, paint)
        y += 25f

        projects.forEachIndexed { index, project ->
            if (y > PAGE_HEIGHT - 120f) return y

            val propAmount = propsMap[project.id] ?: 0L
            val totalAmount = project.netIncome + propAmount
            val cardHeight = if (propAmount > 0) 75f else 60f

            paint.apply { color = if (index % 2 == 0) COLOR_HIGHLIGHT_BG else Color.WHITE; style = Paint.Style.FILL }
            canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + cardHeight, paint)

            paint.apply { color = COLOR_LINE; style = Paint.Style.STROKE; strokeWidth = 0.5f }
            canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + cardHeight, paint)

            var contentY = y + 16f
            val leftCol = MARGIN_LEFT + 10f
            val rightCol = PAGE_WIDTH - MARGIN_RIGHT - 10f

            paint.apply { color = COLOR_TEXT; textSize = 12f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); style = Paint.Style.FILL }
            canvas.drawText(project.brand, leftCol, contentY, paint)

            paint.color = COLOR_PRIMARY
            val totalText = formatCurrency(totalAmount)
            canvas.drawText(totalText, rightCol - paint.measureText(totalText), contentY, paint)

            contentY += 18f
            paint.apply { color = COLOR_TEXT_SECONDARY; textSize = 10f; typeface = Typeface.DEFAULT }
            val periodText = "${dateFormat.format(Date(project.startDate))} ~ ${dateFormat.format(Date(project.endDate))}"
            canvas.drawText("$periodText  |  ${project.workType.displayName}  |  ${project.cuts}컷", leftCol, contentY, paint)

            contentY += 16f
            if (propAmount > 0) {
                paint.color = COLOR_TEXT_SECONDARY
                canvas.drawText("인건비: ${formatCurrency(project.netIncome)}", leftCol, contentY, paint)
                paint.color = 0xFF1976D2.toInt()
                canvas.drawText("소품비: ${formatCurrency(propAmount)}", leftCol + 150f, contentY, paint)
            } else {
                paint.color = COLOR_TEXT_SECONDARY
                canvas.drawText("인건비: ${formatCurrency(project.netIncome)}", leftCol, contentY, paint)
            }

            y += cardHeight + 5f
        }
        return y + 10f
    }

    private fun drawFooter(canvas: Canvas, profile: UserProfile?) {
        val paint = Paint().apply { isAntiAlias = true; color = COLOR_TEXT_SECONDARY; textSize = 10f }
        val today = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(Date())
        val y = PAGE_HEIGHT - 40f

        canvas.drawText("발행일: $today", MARGIN_LEFT, y, paint)
        if (profile?.name?.isNotBlank() == true) {
            val issuerText = "발행자: ${profile.name}"
            canvas.drawText(issuerText, PAGE_WIDTH - MARGIN_RIGHT - paint.measureText(issuerText), y, paint)
        }
        val appText = "프리랜서 정산 앱"
        canvas.drawText(appText, (PAGE_WIDTH - paint.measureText(appText)) / 2, PAGE_HEIGHT - 25f, paint)
    }

    fun createShareIntent(uri: Uri, fileName: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createViewIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}