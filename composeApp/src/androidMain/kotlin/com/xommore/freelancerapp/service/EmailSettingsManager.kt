package com.xommore.freelancerapp.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EmailSettingsManager(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context, "email_settings", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("EmailSettingsManager", "EncryptedSharedPreferences failed, using fallback: ${e.message}")
            context.getSharedPreferences("email_settings_fallback", Context.MODE_PRIVATE)
        }
    }

    companion object {
        private const val KEY_SENDER_EMAIL = "sender_email"
        private const val KEY_APP_PASSWORD = "app_password"
        private const val KEY_SENDER_NAME = "sender_name"
        private const val DEFAULT_EMAIL = ""
    }

    fun setSenderEmail(email: String) { sharedPreferences.edit().putString(KEY_SENDER_EMAIL, email).apply() }
    fun getSenderEmail(): String = sharedPreferences.getString(KEY_SENDER_EMAIL, DEFAULT_EMAIL) ?: DEFAULT_EMAIL

    fun setAppPassword(password: String) { sharedPreferences.edit().putString(KEY_APP_PASSWORD, password).apply() }
    fun getAppPassword(): String = sharedPreferences.getString(KEY_APP_PASSWORD, "") ?: ""

    fun setSenderName(name: String) { sharedPreferences.edit().putString(KEY_SENDER_NAME, name).apply() }
    fun getSenderName(): String = sharedPreferences.getString(KEY_SENDER_NAME, "") ?: ""

    fun isConfigured(): Boolean {
        val email = getSenderEmail()
        val password = getAppPassword()
        return email.isNotBlank() && password.isNotBlank() && password.length >= 16
    }

    fun clearSettings() { sharedPreferences.edit().clear().apply() }
}