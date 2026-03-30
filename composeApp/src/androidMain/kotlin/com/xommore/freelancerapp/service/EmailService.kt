package com.xommore.freelancerapp.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailService(private val context: Context) {

    private val settingsManager = EmailSettingsManager(context)

    companion object {
        private const val SMTP_HOST = "smtp.gmail.com"
        private const val SMTP_PORT = "587"
    }

    sealed class EmailResult {
        object Success : EmailResult()
        data class Error(val message: String) : EmailResult()
    }

    suspend fun sendEmail(toEmail: String, subject: String, body: String): EmailResult = withContext(Dispatchers.IO) {
        try {
            val senderEmail = settingsManager.getSenderEmail()
            val appPassword = settingsManager.getAppPassword()

            if (senderEmail.isBlank() || appPassword.isBlank()) {
                return@withContext EmailResult.Error("이메일 설정이 필요합니다.\n설정 > 이메일 발송 설정에서 설정해주세요.")
            }

            Log.d("EmailService", "Sending email from: $senderEmail to: $toEmail")

            val properties = Properties().apply {
                put("mail.smtp.host", SMTP_HOST)
                put("mail.smtp.port", SMTP_PORT)
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.ssl.trust", SMTP_HOST)
            }

            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication() = PasswordAuthentication(senderEmail, appPassword)
            })

            val senderName = settingsManager.getSenderName()
            val fromAddress = if (senderName.isNotBlank()) InternetAddress(senderEmail, senderName, "UTF-8") else InternetAddress(senderEmail)

            val message = MimeMessage(session).apply {
                setFrom(fromAddress)
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                setSubject(subject, "UTF-8")
                setText(body, "UTF-8")
            }

            Transport.send(message)
            Log.d("EmailService", "Email sent successfully to: $toEmail")
            EmailResult.Success
        } catch (e: AuthenticationFailedException) {
            Log.e("EmailService", "Authentication failed: ${e.message}")
            EmailResult.Error("인증 실패: 이메일 또는 앱 비밀번호를 확인해주세요")
        } catch (e: MessagingException) {
            Log.e("EmailService", "Messaging error: ${e.message}")
            EmailResult.Error("발송 실패: ${e.message}")
        } catch (e: Exception) {
            Log.e("EmailService", "Error: ${e.message}")
            EmailResult.Error("오류 발생: ${e.message}")
        }
    }

    suspend fun sendTestEmail(): EmailResult {
        val senderEmail = settingsManager.getSenderEmail()
        return sendEmail(
            toEmail = senderEmail,
            subject = "[테스트] 프리랜서 정산 앱 이메일 설정 확인",
            body = "안녕하세요!\n\n프리랜서 정산 앱의 이메일 발송 설정이 완료되었습니다.\n이 메일이 정상적으로 수신되었다면, 정산서 이메일 발송 기능을 사용할 수 있습니다.\n\n---\n발신: $senderEmail"
        )
    }

    /**
     * PDF 첨부 이메일 발송
     */
    suspend fun sendEmailWithPdf(toEmail: String, subject: String, body: String, pdfUri: android.net.Uri, pdfFileName: String): EmailResult = withContext(Dispatchers.IO) {
        try {
            val senderEmail = settingsManager.getSenderEmail()
            val appPassword = settingsManager.getAppPassword()

            if (senderEmail.isBlank() || appPassword.isBlank()) {
                return@withContext EmailResult.Error("이메일 설정이 필요합니다.\n설정 > 이메일 발송 설정에서 설정해주세요.")
            }

            val properties = Properties().apply {
                put("mail.smtp.host", SMTP_HOST)
                put("mail.smtp.port", SMTP_PORT)
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.ssl.trust", SMTP_HOST)
            }

            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication() = PasswordAuthentication(senderEmail, appPassword)
            })

            val senderName = settingsManager.getSenderName()
            val fromAddress = if (senderName.isNotBlank()) InternetAddress(senderEmail, senderName, "UTF-8") else InternetAddress(senderEmail)

            // 멀티파트 메시지 (본문 + PDF 첨부)
            val multipart = MimeMultipart().apply {
                // 본문
                addBodyPart(MimeBodyPart().apply {
                    setText(body, "UTF-8")
                })
                // PDF 첨부
                addBodyPart(MimeBodyPart().apply {
                    val inputStream = context.contentResolver.openInputStream(pdfUri)
                    if (inputStream != null) {
                        val tempFile = java.io.File(context.cacheDir, pdfFileName)
                        tempFile.outputStream().use { out -> inputStream.copyTo(out) }
                        inputStream.close()
                        attachFile(tempFile)
                        fileName = pdfFileName
                    }
                })
            }

            val message = MimeMessage(session).apply {
                setFrom(fromAddress)
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                setSubject(subject, "UTF-8")
                setContent(multipart)
            }

            Transport.send(message)
            Log.d("EmailService", "Email with PDF sent to: $toEmail")
            EmailResult.Success
        } catch (e: AuthenticationFailedException) {
            EmailResult.Error("인증 실패: 이메일 또는 앱 비밀번호를 확인해주세요")
        } catch (e: MessagingException) {
            EmailResult.Error("발송 실패: ${e.message}")
        } catch (e: Exception) {
            EmailResult.Error("오류 발생: ${e.message}")
        }
    }

    fun isConfigured(): Boolean = settingsManager.isConfigured()
}