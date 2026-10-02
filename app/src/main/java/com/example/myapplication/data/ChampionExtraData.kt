package com.example.myapplication.data

import android.content.Context
import org.json.JSONObject

data class ChampionExtra(
    val alias: String?,
    val stars: List<Int>,
    val ascendable: Boolean,
    val raidBoostRole: String?,
    val physicalResist: Int?,
    val energyResist: Int?,
    val tags: List<String>,
    val altRelics: List<String>
)

/** champion_extra.json (tools/sync_mcoc.py extras, mcoc.gg): takma ad, yıldız aralığı, Raid rolü, direnç, etiketler. */
object ChampionExtraRepository {
    private var appContext: Context? = null
    private var loaded = false
    private var map: Map<String, ChampionExtra> = emptyMap()

    fun initialize(context: Context) { appContext = context.applicationContext }
    fun reload(context: Context) { appContext = context.applicationContext; loaded = false }

    @Synchronized
    private fun ensureLoaded() {
        if (loaded) return
        val ctx = appContext ?: return
        try {
            val root = JSONObject(DataSource.openText(ctx, "champion_extra.json")).getJSONObject("champions")
            val m = mutableMapOf<String, ChampionExtra>()
            for (id in root.keys()) {
                val o = root.getJSONObject(id)
                fun strs(k: String) = o.optJSONArray(k)?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList()
                val raw = o.optJSONObject("rawStats")
                m[id] = ChampionExtra(
                    alias = if (o.isNull("alias")) null else o.getString("alias"),
                    stars = o.optJSONArray("stars")?.let { a -> (0 until a.length()).map { a.getInt(it) } } ?: emptyList(),
                    ascendable = o.optBoolean("ascendable"),
                    raidBoostRole = if (o.isNull("raidBoostRole")) null else o.getString("raidBoostRole"),
                    physicalResist = raw?.takeIf { it.has("physicalresist") }?.getInt("physicalresist"),
                    energyResist = raw?.takeIf { it.has("energyresist") }?.getInt("energyresist"),
                    tags = strs("tags"),
                    altRelics = strs("altRelics")
                )
            }
            map = m
            loaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun forChampion(id: String): ChampionExtra? { ensureLoaded(); return map[id] }
}
