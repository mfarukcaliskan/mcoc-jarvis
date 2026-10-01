package com.example.myapplication.data

import android.content.Context
import org.json.JSONObject

/** GuiaMTC tier listesi satırı: yazarın kişisel değerlendirmesi, oyun verisi değildir. */
data class GuiaTierEntry(
    val id: String,
    val tier: String,        // "10", "9.5", "9", "8.5", "8", "7-7.5", "5-6.5", "1-4"
    val signature: String?   // "x200", "x20", "undup" ... ya da null
)

data class GuiaProfile(
    val id: String,
    val rating: Double,
    val abilitiesPt: List<String>,
    val immunitiesPt: List<String>
)

object GuiaTierRepository {
    private var offense: Map<String, GuiaTierEntry> = emptyMap()
    private var defense: Map<String, GuiaTierEntry> = emptyMap()
    private var profiles: Map<String, GuiaProfile> = emptyMap()

    fun offenseTier(championId: String): GuiaTierEntry? = offense[championId]
    fun defenseTier(championId: String): GuiaTierEntry? = defense[championId]
    fun profile(championId: String): GuiaProfile? = profiles[championId]

    fun initialize(context: Context) {
        if (offense.isNotEmpty()) return
        load(context)
    }

    fun reload(context: Context) {
        load(context)
    }

    private fun strings(arr: org.json.JSONArray?): List<String> =
        if (arr == null) emptyList() else (0 until arr.length()).map { arr.getString(it) }

    private fun entries(arr: org.json.JSONArray): Map<String, GuiaTierEntry> {
        val map = mutableMapOf<String, GuiaTierEntry>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            map[o.getString("id")] = GuiaTierEntry(
                o.getString("id"), o.getString("tier"),
                if (o.isNull("signature")) null else o.getString("signature")
            )
        }
        return map
    }

    private fun load(context: Context) {
        try {
            val tiers = JSONObject(DataSource.openText(context, "guia_tiers.json"))
            offense = entries(tiers.getJSONArray("offense"))
            defense = entries(tiers.getJSONArray("defense"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            val root = JSONObject(DataSource.openText(context, "guia_champions.json"))
            val arr = root.getJSONArray("champions")
            val map = mutableMapOf<String, GuiaProfile>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                map[o.getString("id")] = GuiaProfile(
                    o.getString("id"), o.getDouble("rating"),
                    strings(o.optJSONArray("abilitiesPt")), strings(o.optJSONArray("immunitiesPt"))
                )
            }
            profiles = map
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
