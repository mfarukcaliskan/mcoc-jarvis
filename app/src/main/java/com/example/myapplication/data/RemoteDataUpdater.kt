package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

sealed class UpdateResult {
    data object NoUpdate : UpdateResult()
    data class Updated(val newVersion: Int, val changedFiles: Int) : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

/**
 * champions_db.json, details ve quests klasörlerindeki json dosyaları ile roster.json'ı
 * GitHub'daki mcoc-jarvis reposundan (raw.githubusercontent.com) kontrol edip,
 * data_manifest.json'daki sürüm daha yeniyse indirir. Tüm dosyalar SHA-256 ile
 * doğrulanmadan hiçbiri diske yazılmaz (all-or-nothing) — yarım/bozuk güncelleme
 * bırakmamak için. Ağ hatalarında bundled/önceki veri korunur, asla crash etmez.
 */
object RemoteDataUpdater {
    private const val RAW_BASE =
        "https://raw.githubusercontent.com/mfarukcaliskan/mcoc-jarvis/main/app/src/main/assets/"
    private const val PREFS_NAME = "mcoc_data_prefs"
    private const val KEY_ACTIVE_VERSION = "activeDataVersion"
    private const val KEY_LAST_CHECK = "lastCheckTimestamp"
    private const val CHECK_COOLDOWN_MS = 24L * 60 * 60 * 1000
    private const val TIMEOUT_MS = 8000
    private const val TAG = "RemoteDataUpdater"

    suspend fun checkForUpdates(context: Context, force: Boolean = false): UpdateResult =
        withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()

            if (!force) {
                val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0L)
                if (now - lastCheck < CHECK_COOLDOWN_MS) {
                    return@withContext UpdateResult.NoUpdate
                }
            }

            if (!isNetworkAvailable(context)) {
                return@withContext UpdateResult.Error("İnternet bağlantısı yok")
            }

            try {
                val remoteManifestText = fetchText(RAW_BASE + "data_manifest.json")
                val remoteManifest = JSONObject(remoteManifestText)
                val remoteVersion = remoteManifest.getInt("dataVersion")
                val activeVersion = getActiveVersion(context, prefs)

                prefs.edit().putLong(KEY_LAST_CHECK, now).apply()

                if (remoteVersion <= activeVersion) {
                    return@withContext UpdateResult.NoUpdate
                }

                val filesObj = remoteManifest.getJSONObject("files")
                val keys = filesObj.keys()
                val downloaded = mutableListOf<Pair<String, ByteArray>>()

                while (keys.hasNext()) {
                    val relPath = keys.next()
                    val expectedHash = filesObj.getString(relPath)
                    val bytes = fetchBytes(RAW_BASE + relPath)
                    val actualHash = sha256(bytes)
                    if (!actualHash.equals(expectedHash, ignoreCase = true)) {
                        return@withContext UpdateResult.Error("Bozuk indirme (hash uyuşmadı): $relPath")
                    }
                    downloaded.add(relPath to bytes)
                }

                // Doğrulama tamamlandı, artık diske yazılabilir (all-or-nothing).
                val targetDir = File(context.filesDir, "mcoc_data")
                for ((relPath, bytes) in downloaded) {
                    val outFile = File(targetDir, relPath)
                    outFile.parentFile?.mkdirs()
                    outFile.writeBytes(bytes)
                }
                File(targetDir, "data_manifest.json").writeText(remoteManifestText)

                prefs.edit().putInt(KEY_ACTIVE_VERSION, remoteVersion).apply()

                UpdateResult.Updated(remoteVersion, downloaded.size)
            } catch (e: Exception) {
                Log.w(TAG, "Veri güncelleme kontrolü başarısız", e)
                UpdateResult.Error(e.message ?: "Bilinmeyen hata")
            }
        }

    fun currentVersion(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return getActiveVersion(context, prefs)
    }

    fun lastCheckTimestamp(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_CHECK, 0L)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            true // emin olamıyorsak denemeye izin ver, asıl HTTP isteği zaten hata verirse yakalanır
        }
    }

    private fun getActiveVersion(context: Context, prefs: SharedPreferences): Int {
        val stored = prefs.getInt(KEY_ACTIVE_VERSION, -1)
        if (stored >= 0) return stored
        val bundledVersion = try {
            val text = context.assets.open("data_manifest.json").bufferedReader().use { it.readText() }
            JSONObject(text).getInt("dataVersion")
        } catch (e: Exception) {
            0
        }
        prefs.edit().putInt(KEY_ACTIVE_VERSION, bundledVersion).apply()
        return bundledVersion
    }

    private fun fetchBytes(urlString: String): ByteArray {
        val connection = URL(urlString).openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.requestMethod = "GET"
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode}: $urlString")
            }
            return connection.inputStream.use { it.readBytes() }
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchText(urlString: String): String = fetchBytes(urlString).toString(Charsets.UTF_8)

    internal fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
