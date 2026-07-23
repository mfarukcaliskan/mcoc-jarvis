package com.example.myapplication.data

import android.content.Context
import org.json.JSONArray

// ==================== ENUMS ====================

enum class ChampionClass(val displayName: String, val color: Long) {
    COSMIC("Kozmik", 0xFF00BCD4),
    TECH("Teknoloji", 0xFF3F51B5),
    MUTANT("Mutant", 0xFFFFEB3B),
    SKILL("Beceri", 0xFFF44336),
    SCIENCE("Bilim", 0xFF4CAF50),
    MYSTIC("Mistik", 0xFF9C27B0)
}

enum class SortOption(val displayName: String) {
    PRESTIGE_DESC("Prestij (Yüksek)"),
    PRESTIGE_ASC("Prestij (Düşük)"),
    ATTACK_DESC("Saldırı (Yüksek)"),
    HEALTH_DESC("Can (Yüksek)"),
    NAME_ASC("İsim (A-Z)"),
    NAME_DESC("İsim (Z-A)")
}

// ==================== DATA CLASSES ====================

data class RankStats(
    val rank: Int,
    val basePrestige: Int,
    val maxPrestige: Int,
    val attack: Int,
    val health: Int
)

data class StarProgression(
    val starRating: Int, // 6 veya 7
    val ranks: List<RankStats>
)

data class Synergy(
    val partnerName: String,
    val bonus: String
)

data class ChampionDetails(
    val id: String,
    val name: String,
    val abilityDetails: Map<String, String> = emptyMap(),
    val synergies: List<Synergy> = emptyList(),
    val howToPlay: String = "",
    val bestUse: String = "",
    val signatureAbility: String = ""
)

data class Champion(
    val id: String,
    val name: String,
    val mcocClass: ChampionClass,
    val tier: String,
    val prestige: Int,
    val prestigeRank: Int,
    val attack: Int,
    val health: Int,
    val critRate: Double,
    val critDamage: Double,
    val armor: Double,
    val blockProficiency: Double,
    val immunities: List<String>,
    val counters: List<String>,
    val abilities: String,
    val signatureAbility: String = "",
    val synergies: List<Synergy> = emptyList(),
    val recommendedRelics: List<String>,
    val tags: List<String>,
    val focusAttack: String,
    val focusDefense: String,
    val howToPlay: String = "",
    val bestUse: String = "",
    val releaseYear: Int,
    // === New fields from MCOC.gg ===
    val strongMatchups: List<String> = emptyList(),
    val strongCounters: List<String> = emptyList(),
    val reactsTo: List<String> = emptyList(),
    val counterAbilities: List<String> = emptyList(),
    val releaseDate: String = "",
    val attackRank: Int = 0,
    val healthRank: Int = 0,
    val critRateRank: Int = 0,
    val critDamageRank: Int = 0,
    val armorRank: Int = 0,
    val blockProficiencyRank: Int = 0,
    // === Phase 2 dynamic fields ===
    val progressions: List<StarProgression> = emptyList(),
    val abilityDetails: Map<String, String> = emptyMap()
)

// ==================== REPOSITORY ====================

object ChampionRepository {
    var champions: List<Champion> = emptyList()
        private set

    private fun parseStringArray(obj: org.json.JSONObject, key: String): List<String> {
        if (!obj.has(key)) return emptyList()
        return try {
            val arr = obj.getJSONArray(key)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) { emptyList() }
    }

    private fun parseIntSafe(obj: org.json.JSONObject, key: String, default: Int = 0): Int {
        return try { obj.getInt(key) } catch (e: Exception) { default }
    }

    private fun parseStringSafe(obj: org.json.JSONObject, key: String, default: String = ""): String {
        return try { obj.getString(key) } catch (e: Exception) { default }
    }

    private fun parseProgressions(obj: org.json.JSONObject): List<StarProgression> {
        if (!obj.has("progressions")) return emptyList()
        return try {
            val list = mutableListOf<StarProgression>()
            val progArr = obj.getJSONArray("progressions")
            for (i in 0 until progArr.length()) {
                val progObj = progArr.getJSONObject(i)
                val starRating = progObj.getInt("starRating")
                val ranksList = mutableListOf<RankStats>()
                val ranksArr = progObj.getJSONArray("ranks")
                for (j in 0 until ranksArr.length()) {
                    val rankObj = ranksArr.getJSONObject(j)
                    ranksList.add(
                        RankStats(
                            rank = rankObj.getInt("rank"),
                            basePrestige = rankObj.getInt("basePrestige"),
                            maxPrestige = rankObj.getInt("maxPrestige"),
                            attack = rankObj.getInt("attack"),
                            health = rankObj.getInt("health")
                        )
                    )
                }
                list.add(StarProgression(starRating, ranksList))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseAbilityDetails(obj: org.json.JSONObject): Map<String, String> {
        if (!obj.has("abilityDetails")) return emptyMap()
        return try {
            val map = mutableMapOf<String, String>()
            val detailsObj = obj.getJSONObject("abilityDetails")
            val keys = detailsObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = detailsObj.getString(key)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun initialize(context: Context) {
        if (champions.isNotEmpty()) return
        loadFromDataSource(context)
    }

    /** RemoteDataUpdater yeni veri indirdiğinde çağrılır; listeyi baştan okur. */
    fun reload(context: Context) {
        loadFromDataSource(context)
    }

    private fun loadFromDataSource(context: Context) {
        try {
            val jsonString = DataSource.openText(context, "champions_db.json")
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<Champion>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val immunitiesList = parseStringArray(obj, "immunities")
                val countersList = parseStringArray(obj, "counters")
                val recommendedRelicsList = parseStringArray(obj, "recommendedRelics")
                val tagsList = parseStringArray(obj, "tags")

                val synergiesList = mutableListOf<Synergy>()
                if (obj.has("synergies")) {
                    val synergiesArr = obj.getJSONArray("synergies")
                    for (j in 0 until synergiesArr.length()) {
                        val synObj = synergiesArr.getJSONObject(j)
                        synergiesList.add(
                            Synergy(
                                synObj.getString("partnerName"),
                                synObj.getString("bonus")
                            )
                        )
                    }
                }

                val clsStr = obj.getString("mcocClass")
                val cls = try {
                    ChampionClass.valueOf(clsStr)
                } catch (e: Exception) {
                    ChampionClass.COSMIC
                }

                list.add(
                    Champion(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        mcocClass = cls,
                        tier = obj.getString("tier"),
                        prestige = obj.getInt("prestige"),
                        prestigeRank = obj.getInt("prestigeRank"),
                        attack = obj.getInt("attack"),
                        health = obj.getInt("health"),
                        critRate = obj.getDouble("critRate"),
                        critDamage = obj.getDouble("critDamage"),
                        armor = obj.getDouble("armor"),
                        blockProficiency = obj.getDouble("blockProficiency"),
                        immunities = immunitiesList,
                        counters = countersList,
                        abilities = obj.getString("abilities"),
                        signatureAbility = parseStringSafe(obj, "signatureAbility"),
                        synergies = synergiesList,
                        recommendedRelics = recommendedRelicsList,
                        tags = tagsList,
                        focusAttack = parseStringSafe(obj, "focusAttack"),
                        focusDefense = parseStringSafe(obj, "focusDefense"),
                        howToPlay = parseStringSafe(obj, "howToPlay"),
                        bestUse = parseStringSafe(obj, "bestUse"),
                        releaseYear = parseIntSafe(obj, "releaseYear"),
                        // New optional fields
                        strongMatchups = parseStringArray(obj, "strongMatchups"),
                        strongCounters = parseStringArray(obj, "strongCounters"),
                        reactsTo = parseStringArray(obj, "reactsTo"),
                        counterAbilities = parseStringArray(obj, "counterAbilities"),
                        releaseDate = parseStringSafe(obj, "releaseDate"),
                        attackRank = parseIntSafe(obj, "attackRank"),
                        healthRank = parseIntSafe(obj, "healthRank"),
                        critRateRank = parseIntSafe(obj, "critRateRank"),
                        critDamageRank = parseIntSafe(obj, "critDamageRank"),
                        armorRank = parseIntSafe(obj, "armorRank"),
                        blockProficiencyRank = parseIntSafe(obj, "blockProficiencyRank"),
                        // Phase 2 fields
                        progressions = parseProgressions(obj),
                        abilityDetails = parseAbilityDetails(obj)
                    )
                )
            }
            champions = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadChampionDetails(context: android.content.Context, champId: String): ChampionDetails? {
        return try {
            val jsonString = DataSource.openText(context, "details/$champId.json")
            val obj = org.json.JSONObject(jsonString)
            
            val synergiesList = mutableListOf<Synergy>()
            if (obj.has("synergies")) {
                val synergiesArr = obj.getJSONArray("synergies")
                for (j in 0 until synergiesArr.length()) {
                    val synObj = synergiesArr.getJSONObject(j)
                    synergiesList.add(
                        Synergy(
                            synObj.getString("partnerName"),
                            synObj.getString("bonus")
                        )
                    )
                }
            }
            
            ChampionDetails(
                id = champId,
                name = obj.getString("name"),
                abilityDetails = parseAbilityDetails(obj),
                synergies = synergiesList,
                howToPlay = parseStringSafe(obj, "howToPlay"),
                bestUse = parseStringSafe(obj, "bestUse"),
                signatureAbility = parseStringSafe(obj, "signatureAbility")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
