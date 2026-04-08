package com.xommore.freelancerapp.service

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class DepositRecord(
    val clientName: String,
    val amount: Long,
    val timestamp: Long
)

class DepositAlertManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "deposit_alert_prefs"
        private const val KEY_CLIENTS = "registered_clients"
        private const val KEY_ACCOUNT_INFO = "account_info"
        private const val KEY_DEPOSIT_RECORDS = "deposit_records"
        private const val KEY_ENABLED = "alert_enabled"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, true)
    fun setEnabled(enabled: Boolean) { prefs.edit().putBoolean(KEY_ENABLED, enabled).apply() }

    fun getRegisteredClients(): List<String> {
        val json = prefs.getString(KEY_CLIENTS, null) ?: return emptyList()
        return try { val arr = JSONArray(json); (0 until arr.length()).map { arr.getString(it) } } catch (_: Exception) { emptyList() }
    }

    fun addClient(name: String) {
        val clients = getRegisteredClients().toMutableList()
        if (!clients.contains(name)) { clients.add(name); saveClients(clients) }
    }

    fun removeClient(name: String) {
        val clients = getRegisteredClients().toMutableList(); clients.remove(name); saveClients(clients)
    }

    private fun saveClients(clients: List<String>) {
        prefs.edit().putString(KEY_CLIENTS, JSONArray(clients).toString()).apply()
    }

    fun saveAccountInfo(bankName: String, accountNumber: String) {
        prefs.edit().putString(KEY_ACCOUNT_INFO, JSONObject().apply { put("bankName", bankName); put("accountNumber", accountNumber) }.toString()).apply()
    }

    fun getAccountInfo(): Pair<String, String>? {
        val json = prefs.getString(KEY_ACCOUNT_INFO, null) ?: return Pair("신한은행", "")
        return try { val obj = JSONObject(json); Pair(obj.optString("bankName", ""), obj.optString("accountNumber", "")) } catch (_: Exception) { null }
    }

    fun saveDepositRecord(clientName: String, amount: Long) {
        val records = getDepositRecords().toMutableList()
        records.add(DepositRecord(clientName, amount, System.currentTimeMillis()))
        val recentRecords = records.takeLast(100)
        val arr = JSONArray()
        recentRecords.forEach { r -> arr.put(JSONObject().apply { put("clientName", r.clientName); put("amount", r.amount); put("timestamp", r.timestamp) }) }
        prefs.edit().putString(KEY_DEPOSIT_RECORDS, arr.toString()).apply()
    }

    fun getDepositRecords(): List<DepositRecord> {
        val json = prefs.getString(KEY_DEPOSIT_RECORDS, null) ?: return emptyList()
        return try { val arr = JSONArray(json); (0 until arr.length()).map { val o = arr.getJSONObject(it); DepositRecord(o.getString("clientName"), o.getLong("amount"), o.getLong("timestamp")) } } catch (_: Exception) { emptyList() }
    }

    fun clearDepositRecords() { prefs.edit().remove(KEY_DEPOSIT_RECORDS).apply() }
}
