package com.example.myapplication.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * mcoc.gg'nin gerçek prestij tablosundan bir kademe (örn. 7★ R5).
 * prestige: sig 0,20,...,200 için 11 değer (null = kaynakta bilinmiyor); tablosu olmayan şampiyonlarda null.
 * attack/health yalnızca kaynakta verilen kademe için dolu (R5 sig 200), aksi halde null.
 */
data class PrestigeEntry(
    val star: Int,
    val rank: Int?,
    val prestige: List<Int?>?,
    val maxPrestige: Int?,
    val attack: Int?,
    val health: Int?
) {
    val label: String get() = if (rank != null) "$star★ R$rank" else "$star★ (en yüksek)"
    val hasSigTable: Boolean get() = prestige != null

    /** sigIndex: 0..10 (sig = index * 20). Tablo yoksa en yüksek kademe değeri döner. */
    fun prestigeAt(sigIndex: Int): Int? =
        prestige?.getOrNull(sigIndex.coerceIn(0, PrestigeRepository.SIG_POINTS - 1)) ?: maxPrestige
}

data class PrestigeInfo(
    val maxStar: Int,
    val ascendable: Boolean,
    val entries: List<PrestigeEntry>,
    val flags: List<String>
) {
    /** Varsayılan kademe: 7★ R5 (kaynağın üst düzey değerleri bu noktadır); yoksa son giriş. */
    val defaultIndex: Int
        get() {
            val i = entries.indexOfFirst { it.star == 7 && it.rank == 5 }
            return if (i >= 0) i else (entries.size - 1).coerceAtLeast(0)
        }
}

object PrestigeRepository {
    const val SIG_STEP = 20
    const val SIG_POINTS = 11   // 0,20,...,200

    private var appContext: Context? = null
    private var loaded = false
    private var byChampion: Map<String, PrestigeInfo> = emptyMap()

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun reload(context: Context) {
        appContext = context.applicationContext
        loaded = false
    }

    private fun intOrNull(o: JSONObject, key: String): Int? =
        if (o.has(key) && !o.isNull(key)) o.getInt(key) else null

    private fun series(arr: JSONArray?): List<Int?>? {
        if (arr == null) return null
        return (0 until arr.length()).map { if (arr.isNull(it)) null else arr.getInt(it) }
    }

    @Synchronized
    private fun ensureLoaded() {
        if (loaded) return
        val ctx = appContext ?: return
        try {
            val root = JSONObject(DataSource.openText(ctx, "prestige.json"))
            val champs = root.getJSONObject("champions")
            val map = HashMap<String, PrestigeInfo>()
            val keys = champs.keys()
            while (keys.hasNext()) {
                val id = keys.next()
                val o = champs.getJSONObject(id)
                val entriesArr = o.getJSONArray("entries")
                val entries = (0 until entriesArr.length()).map { i ->
                    val e = entriesArr.getJSONObject(i)
                    PrestigeEntry(
                        star = e.getInt("star"),
                        rank = intOrNull(e, "rank"),
                        prestige = series(e.optJSONArray("prestige")),
                        maxPrestige = intOrNull(e, "maxPrestige"),
                        attack = intOrNull(e, "attack"),
                        health = intOrNull(e, "health")
                    )
                }
                val flagsArr = o.optJSONArray("flags")
                val flags = if (flagsArr == null) emptyList() else (0 until flagsArr.length()).map { flagsArr.getString(it) }
                map[id] = PrestigeInfo(o.getInt("maxStar"), o.optBoolean("ascendable", false), entries, flags)
            }
            byChampion = map
            loaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun info(championId: String): PrestigeInfo? { ensureLoaded(); return byChampion[championId] }

    fun entries(championId: String): List<PrestigeEntry> = info(championId)?.entries.orEmpty()

    fun defaultIndex(championId: String): Int = info(championId)?.defaultIndex ?: 0

    /** Seçilen kademe + sig noktası için prestij; veri yoksa şampiyonun üst düzey değeri. */
    fun prestige(champion: Champion, entryIndex: Int, sigIndex: Int): Int {
        val entry = entries(champion.id).getOrNull(entryIndex)
        return entry?.prestigeAt(sigIndex) ?: champion.prestige
    }
}
