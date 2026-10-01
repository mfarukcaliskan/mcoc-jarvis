package com.example.myapplication.data

import android.content.Context
import org.json.JSONArray

/** Andaç yeteneği/niteliği: ad + mcoc.gg açıklaması (sayı aralıkları [min - max] olarak). */
data class RelicText(val name: String, val desc: String)

data class RelicPrestige(val star: Int, val rank: Int, val value: Int)

data class Relic(
    val id: String,
    val name: String,
    val relicClass: ChampionClass,
    val relicType: String,  // "Battlecast" or "Statcast"
    val image: String = "",
    val innateAbilities: List<String> = emptyList(),
    val abilityRunes: List<String> = emptyList(),
    val attributeRunes: List<String> = emptyList(),
    val recommendedChampions: List<String> = emptyList(),
    val description: String = "",
    val releaseDate: String = "",
    val rarity: List<Int> = emptyList(),
    // --- mcoc.gg ayrıntıları ---
    val innate: RelicText? = null,
    val abilities: List<RelicText> = emptyList(),
    val attributes: List<RelicText> = emptyList(),
    val recommendedChampionIds: List<String> = emptyList(),
    val maxLevel: String = "",
    val maxPrestige: Int? = null,
    val prestigeRank: Int? = null,
    val prestigeByLevel: List<RelicPrestige> = emptyList()
)

object RelicRepository {
    var relics: List<Relic> = emptyList()
        private set

    private fun parseStringArray(obj: org.json.JSONObject, key: String): List<String> {
        if (!obj.has(key)) return emptyList()
        return try {
            val arr = obj.getJSONArray(key)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) { emptyList() }
    }

    private fun parseIntArray(obj: org.json.JSONObject, key: String): List<Int> {
        if (!obj.has(key)) return emptyList()
        return try {
            val arr = obj.getJSONArray(key)
            (0 until arr.length()).map { arr.getInt(it) }
        } catch (e: Exception) { emptyList() }
    }

    private fun parseStringSafe(obj: org.json.JSONObject, key: String, default: String = ""): String {
        return try { obj.getString(key) } catch (e: Exception) { default }
    }

    fun initialize(context: Context) {
        if (relics.isNotEmpty()) return
        loadFromDataSource(context)
    }

    /** RemoteDataUpdater yeni veri indirdiğinde çağrılır; listeyi baştan okur. */
    fun reload(context: Context) {
        loadFromDataSource(context)
    }

    private fun parseTexts(obj: org.json.JSONObject, key: String): List<RelicText> {
        val arr = obj.optJSONArray(key) ?: return emptyList()
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            RelicText(o.getString("name"), o.optString("desc"))
        }
    }

    private fun loadFromDataSource(context: Context) {
        try {
            val jsonString = DataSource.openText(context, "relics.json")
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<Relic>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                val clsStr = obj.getString("relicClass")
                val cls = try {
                    ChampionClass.valueOf(clsStr)
                } catch (e: Exception) {
                    ChampionClass.COSMIC
                }

                list.add(
                    Relic(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        relicClass = cls,
                        relicType = obj.getString("relicType"),
                        image = parseStringSafe(obj, "image"),
                        innateAbilities = parseStringArray(obj, "innateAbilities"),
                        abilityRunes = parseStringArray(obj, "abilityRunes"),
                        attributeRunes = parseStringArray(obj, "attributeRunes"),
                        recommendedChampions = parseStringArray(obj, "recommendedChampions"),
                        description = parseStringSafe(obj, "description"),
                        releaseDate = parseStringSafe(obj, "releaseDate"),
                        rarity = parseIntArray(obj, "rarity"),
                        innate = obj.optJSONObject("innate")?.let { RelicText(it.getString("name"), it.optString("desc")) },
                        abilities = parseTexts(obj, "abilities"),
                        attributes = parseTexts(obj, "attributes"),
                        recommendedChampionIds = parseStringArray(obj, "recommendedChampionIds"),
                        maxLevel = parseStringSafe(obj, "maxLevel"),
                        maxPrestige = if (obj.has("maxPrestige") && !obj.isNull("maxPrestige")) obj.getInt("maxPrestige") else null,
                        prestigeRank = if (obj.has("prestigeRank") && !obj.isNull("prestigeRank")) obj.getInt("prestigeRank") else null,
                        prestigeByLevel = obj.optJSONArray("prestigeByLevel")?.let { a ->
                            (0 until a.length()).map { k ->
                                val o = a.getJSONObject(k)
                                RelicPrestige(o.getInt("star"), o.getInt("rank"), o.getInt("value"))
                            }
                        } ?: emptyList()
                    )
                )
            }
            relics = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
