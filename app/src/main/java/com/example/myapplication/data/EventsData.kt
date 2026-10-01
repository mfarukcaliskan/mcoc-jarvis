package com.example.myapplication.data

import android.content.Context
import org.json.JSONObject

data class AwTactic(
    val tagId: String,
    val name: String,
    val role: String?,            // "defense" | "attack" | null (mcoc.gg'de açıklama kaydı yok)
    val description: String?,
    val champions: List<String>
)

data class PoolExit(val title: String, val champions: List<String>)

/**
 * events.json (tools/sync_mcoc.py events ile üretilir, mcoc.gg'den haftalık güncellenir):
 * AW taktikleri ve kadroları, Titan havuz çıkışları. Dosya ~80 KB; ilk sorguda tembel okunur.
 */
object EventsRepository {
    private var appContext: Context? = null
    private var loaded = false
    private var tactics: List<AwTactic> = emptyList()
    private var exits: List<PoolExit> = emptyList()

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun reload(context: Context) {
        appContext = context.applicationContext
        loaded = false
    }

    private fun strings(arr: org.json.JSONArray?): List<String> =
        if (arr == null) emptyList() else (0 until arr.length()).map { arr.getString(it) }

    @Synchronized
    private fun ensureLoaded() {
        if (loaded) return
        val ctx = appContext ?: return
        try {
            val root = JSONObject(DataSource.openText(ctx, "events.json"))
            val aw = root.getJSONArray("awTactics")
            tactics = (0 until aw.length()).map { i ->
                val o = aw.getJSONObject(i)
                AwTactic(
                    tagId = o.getString("tagId"),
                    name = o.getString("name"),
                    role = if (o.isNull("role")) null else o.getString("role"),
                    description = if (o.isNull("descriptionEn")) null else o.getString("descriptionEn"),
                    champions = strings(o.optJSONArray("champions"))
                )
            }
            val ex = root.getJSONArray("exits")
            exits = (0 until ex.length()).map { i ->
                val o = ex.getJSONObject(i)
                PoolExit(o.getString("title"), strings(o.optJSONArray("champions")))
            }
            loaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Rol/açıklaması olan (güncel) AW taktikleri, en yeniden eskiye. */
    fun awTactics(): List<AwTactic> { ensureLoaded(); return tactics.filter { it.role != null }.reversed() }

    fun poolExits(): List<PoolExit> { ensureLoaded(); return exits }
}
