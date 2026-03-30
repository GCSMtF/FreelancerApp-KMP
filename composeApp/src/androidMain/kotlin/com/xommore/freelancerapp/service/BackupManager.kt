package com.xommore.freelancerapp.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.xommore.freelancerapp.data.model.Client
import com.xommore.freelancerapp.data.model.Project
import com.xommore.freelancerapp.data.model.UserProfile
import com.xommore.freelancerapp.data.model.WorkType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BackupManager(private val context: Context) {

    companion object {
        private const val TAG = "BackupManager"
        private const val BACKUP_VERSION = 1
    }

    sealed class BackupResult {
        data class Success(val file: File, val uri: Uri) : BackupResult()
        data class Error(val message: String) : BackupResult()
    }

    sealed class RestoreResult {
        data class Success(val projects: List<Project>, val clients: List<Client>, val profile: UserProfile?) : RestoreResult()
        data class Error(val message: String) : RestoreResult()
    }

    suspend fun createBackup(projects: List<Project>, clients: List<Client>, profile: UserProfile?): BackupResult = withContext(Dispatchers.IO) {
        try {
            val jsonObject = JSONObject().apply {
                put("version", BACKUP_VERSION)
                put("createdAt", System.currentTimeMillis())
                put("appName", "FreelancerApp")
                put("projects", JSONArray().apply { projects.forEach { put(projectToJson(it)) } })
                put("clients", JSONArray().apply { clients.forEach { put(clientToJson(it)) } })
                profile?.let { put("profile", profileToJson(it)) }
            }

            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA)
            val fileName = "freelancer_backup_${dateFormat.format(Date())}.json"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { it.write(jsonObject.toString(2).toByteArray(Charsets.UTF_8)) }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            Log.d(TAG, "Backup created: ${file.absolutePath}")
            BackupResult.Success(file, uri)
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed: ${e.message}", e)
            BackupResult.Error("백업 실패: ${e.message}")
        }
    }

    suspend fun restoreFromUri(uri: Uri): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext RestoreResult.Error("파일을 열 수 없습니다")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            if (jsonObject.optInt("version", 0) == 0) return@withContext RestoreResult.Error("올바른 백업 파일이 아닙니다")

            val projects = mutableListOf<Project>()
            jsonObject.optJSONArray("projects")?.let { arr -> for (i in 0 until arr.length()) jsonToProject(arr.getJSONObject(i))?.let { projects.add(it) } }

            val clients = mutableListOf<Client>()
            jsonObject.optJSONArray("clients")?.let { arr -> for (i in 0 until arr.length()) jsonToClient(arr.getJSONObject(i))?.let { clients.add(it) } }

            val profile = jsonObject.optJSONObject("profile")?.let { jsonToProfile(it) }

            Log.d(TAG, "Restore success: ${projects.size} projects, ${clients.size} clients")
            RestoreResult.Success(projects, clients, profile)
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed: ${e.message}", e)
            RestoreResult.Error("복원 실패: ${e.message}")
        }
    }

    fun createShareIntent(uri: Uri, fileName: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun projectToJson(p: Project) = JSONObject().apply {
        put("id", p.id); put("userId", p.userId); put("brand", p.brand); put("workType", p.workType.name)
        put("cuts", p.cuts); put("basePrice", p.basePrice); put("startDate", p.startDate); put("endDate", p.endDate)
        put("memo", p.memo); put("createdAt", p.createdAt); put("clientId", p.clientId ?: "")
        put("clientName", p.clientName); put("clientCompany", p.clientCompany); put("clientEmail", p.clientEmail)
    }

    private fun jsonToProject(j: JSONObject): Project? = try {
        Project(id = j.getString("id"), userId = j.getString("userId"), brand = j.getString("brand"),
            workType = try { WorkType.valueOf(j.getString("workType")) } catch (_: Exception) { WorkType.entries.first() },
            cuts = j.getInt("cuts"), basePrice = j.getLong("basePrice"), startDate = j.getLong("startDate"), endDate = j.getLong("endDate"),
            memo = j.optString("memo", ""), createdAt = j.optLong("createdAt", System.currentTimeMillis()),
            clientId = j.optString("clientId", "").ifBlank { null }, clientName = j.optString("clientName", ""),
            clientCompany = j.optString("clientCompany", ""), clientEmail = j.optString("clientEmail", ""))
    } catch (e: Exception) { Log.e(TAG, "Failed to parse project: ${e.message}"); null }

    private fun clientToJson(c: Client) = JSONObject().apply {
        put("id", c.id); put("userId", c.userId); put("company", c.company); put("name", c.name)
        put("email", c.email); put("phone", c.phone); put("memo", c.memo); put("createdAt", c.createdAt)
    }

    private fun jsonToClient(j: JSONObject): Client? = try {
        Client(id = j.getString("id"), userId = j.getString("userId"), company = j.getString("company"),
            name = j.optString("name", ""), email = j.optString("email", ""), phone = j.optString("phone", ""),
            memo = j.optString("memo", ""), createdAt = j.optLong("createdAt", System.currentTimeMillis()))
    } catch (e: Exception) { Log.e(TAG, "Failed to parse client: ${e.message}"); null }

    private fun profileToJson(p: UserProfile) = JSONObject().apply {
        put("id", p.id); put("userId", p.userId); put("name", p.name); put("phone", p.phone); put("email", p.email)
        put("bankName", p.bankName); put("accountNumber", p.accountNumber); put("accountHolder", p.accountHolder)
        put("businessNumber", p.businessNumber); put("address", p.address); put("taxRate", p.taxRate)
    }

    private fun jsonToProfile(j: JSONObject): UserProfile? = try {
        UserProfile(id = j.optString("id", "default"), userId = j.optString("userId", ""),
            name = j.optString("name", ""), phone = j.optString("phone", ""), email = j.optString("email", ""),
            bankName = j.optString("bankName", ""), accountNumber = j.optString("accountNumber", ""),
            accountHolder = j.optString("accountHolder", ""), businessNumber = j.optString("businessNumber", ""),
            address = j.optString("address", ""), taxRate = j.optDouble("taxRate", 3.3))
    } catch (e: Exception) { Log.e(TAG, "Failed to parse profile: ${e.message}"); null }
}