package com.example.myapplication.data

import android.content.Context
import org.json.JSONObject

data class GuiaRelicRating(
    val relicId: String,
    val rating: Int,
    val ratingMax: Int,
    val effectPt: String?,
    val recommendedUsePt: String?,
    val interactionsPt: String?,
    val mentionedChampionIds: List<String>
)

data class GuiaGuide(val id: String, val title: String, val paragraphs: List<String>)

data class Rank7Entry(val rank: Int, val championId: String, val r5: Int, val r5A1: Int, val r5A2: Int, val r4: Int)

/** guia_relics.json (Battlecast puanları, PT metin) ve guia_rank7.json (7★ prestij sıralaması); GuiaMTC. */
object GuiaRelicRepository {
    private var appContext: Context? = null
    private var loaded = false
    private var ratings: Map<String, GuiaRelicRating> = emptyMap()
    private var rank7: List<Rank7Entry> = emptyList()
    private var guides: List<GuiaGuide> = emptyList()

    fun initialize(context: Context) { appContext = context.applicationContext }
    fun reload(context: Context) { appContext = context.applicationContext; loaded = false }

    private fun opt(o: JSONObject, k: String): String? = if (o.isNull(k)) null else o.optString(k).ifBlank { null }

    @Synchronized
    private fun ensureLoaded() {
        if (loaded) return
        val ctx = appContext ?: return
        try {
            val rel = JSONObject(DataSource.openText(ctx, "guia_relics.json")).getJSONArray("relics")
            ratings = (0 until rel.length()).mapNotNull { i ->
                val o = rel.getJSONObject(i)
                if (o.isNull("relicId")) return@mapNotNull null
                val m = o.optJSONArray("mentionedChampionIds")
                GuiaRelicRating(
                    o.getString("relicId"), o.getInt("rating"), o.optInt("ratingMax", 10),
                    opt(o, "effectPt"), opt(o, "recommendedUsePt"), opt(o, "interactionsPt"),
                    if (m == null) emptyList() else (0 until m.length()).map { m.getString(it) }
                )
            }.associateBy { it.relicId }
            val rows = JSONObject(DataSource.openText(ctx, "guia_rank7.json")).getJSONArray("rows")
            rank7 = (0 until rows.length()).map { i ->
                val o = rows.getJSONObject(i)
                val r5 = o.getJSONObject("r5"); val r4 = o.getJSONObject("r4")
                Rank7Entry(o.getInt("rank"), o.getString("championId"), r5.getInt("base"), r5.getInt("a1"), r5.getInt("a2"), r4.getInt("base"))
            }
            val gs = JSONObject(DataSource.openText(ctx, "guia_guides.json")).getJSONArray("guides")
            guides = (0 until gs.length()).map { i ->
                val o = gs.getJSONObject(i)
                val ps = o.getJSONArray("paragraphs")
                GuiaGuide(o.getString("id"), o.getString("titlePt"), (0 until ps.length()).map { ps.getJSONObject(it).getString("text") })
            }
            loaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun ratingFor(relicId: String): GuiaRelicRating? { ensureLoaded(); return ratings[relicId] }
    fun guides(): List<GuiaGuide> { ensureLoaded(); return guides }
    fun rank7(): List<Rank7Entry> { ensureLoaded(); return rank7 }
    fun rank7For(championId: String): Rank7Entry? { ensureLoaded(); return rank7.firstOrNull { it.championId == championId } }
}
