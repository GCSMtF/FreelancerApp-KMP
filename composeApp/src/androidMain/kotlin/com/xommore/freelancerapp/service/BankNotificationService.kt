package com.xommore.freelancerapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.xommore.freelancerapp.MainActivity

class BankNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "BankNotificationService"
        private const val SHINHAN_PACKAGE = "com.shinhan.sbanking"
        private const val SHINHAN_SOL_PACKAGE = "com.shinhan.smartcaremgr"
        const val CHANNEL_ID = "deposit_notification"
    }

    private lateinit var depositAlertManager: DepositAlertManager

    override fun onCreate() {
        super.onCreate()
        depositAlertManager = DepositAlertManager(this)
        createNotificationChannel()
        Log.d(TAG, "BankNotificationService started")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        if (sbn.packageName == SHINHAN_PACKAGE || sbn.packageName == SHINHAN_SOL_PACKAGE) {
            val extras = sbn.notification.extras
            val title = extras.getString("android.title") ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            Log.d(TAG, "Shinhan notification - Title: $title, Text: $text")
            parseDepositNotification(title, text)
        }
    }

    private fun parseDepositNotification(title: String, text: String) {
        val fullText = "$title $text"
        if (!fullText.contains("입금")) return

        val amountRegex = """(\d{1,3}(,\d{3})*)\s*원""".toRegex()
        val amount = amountRegex.find(fullText)?.groupValues?.get(1)?.replace(",", "")?.toLongOrNull() ?: 0L
        if (amount <= 0) return

        val registeredClients = depositAlertManager.getRegisteredClients()
        for (clientName in registeredClients) {
            if (fullText.contains(clientName)) {
                Log.d(TAG, "Matched client: $clientName, Amount: $amount")
                showDepositNotification(clientName, amount)
                depositAlertManager.saveDepositRecord(clientName, amount)
                return
            }
        }
    }

    private fun showDepositNotification(clientName: String, amount: Long) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_deposit", true)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val formattedAmount = String.format("%,d", amount)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💰 입금 확인")
            .setContentText("${clientName}님으로부터 ${formattedAmount}원이 입금되었습니다")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "입금 알림", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "클라이언트 입금 알림"; enableVibration(true); vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {}
}
