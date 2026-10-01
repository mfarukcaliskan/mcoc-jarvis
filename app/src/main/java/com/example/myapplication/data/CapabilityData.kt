package com.example.myapplication.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** mcoc.gg verisinden: bir yeteneğe/bağışıklığa sahip (ya da onu etkisizleştiren) şampiyon. */
data class CapHolder(
    val id: String,
    val via: List<String>,          // iddianın dayandığı metin bölümleri (örn. "Special Attack 3")
    val synergy: Boolean,           // yalnızca bir sinerji ile kazanılır
    val signatureOnly: Boolean,     // yalnızca imza yeteneği (awakening) ile
    val kinds: List<String>         // bağışıklık türleri: full, conditional, potency, duration, purify
)

/**
 * capabilities.json (tools/sync_mcoc.py capabilities ile üretilir, mcoc.gg'den haftalık güncellenir).
 * Dosya ~500 KB olduğu için ilk sorguda tembel (lazy) okunur.
 */
object CapabilityRepository {
    private var appContext: Context? = null
    private var loaded = false
    private var abilities: Map<String, List<CapHolder>> = emptyMap()
    private var immunities: Map<String, List<CapHolder>> = emptyMap()
    private var counters: Map<String, List<CapHolder>> = emptyMap()
    private var glossary: Map<String, String> = emptyMap()

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    /** Yeni veri indirildiğinde çağrılır; bir sonraki sorguda baştan okunur. */
    fun reload(context: Context) {
        appContext = context.applicationContext
        loaded = false
    }

    private fun holders(arr: JSONArray?): List<CapHolder> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            CapHolder(
                id = o.getString("id"),
                via = strings(o.optJSONArray("via")),
                synergy = o.optBoolean("synergy", false),
                signatureOnly = o.optBoolean("signatureOnly", false),
                kinds = strings(o.optJSONArray("kinds"))
            )
        }
    }

    private fun strings(arr: JSONArray?): List<String> =
        if (arr == null) emptyList() else (0 until arr.length()).map { arr.getString(it) }

    private fun group(arr: JSONArray?, gloss: MutableMap<String, String>? = null): Map<String, List<CapHolder>> {
        if (arr == null) return emptyMap()
        val map = HashMap<String, List<CapHolder>>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val name = o.getString("name")
            map[name] = holders(o.optJSONArray("champions"))
            if (gloss != null && o.has("glossary")) gloss[name] = o.getString("glossary")
        }
        return map
    }

    @Synchronized
    private fun ensureLoaded() {
        if (loaded) return
        val ctx = appContext ?: return
        try {
            val root = JSONObject(DataSource.openText(ctx, "capabilities.json"))
            val gloss = HashMap<String, String>()
            abilities = group(root.optJSONArray("abilities"), gloss)
            immunities = group(root.optJSONArray("immunities"))
            counters = group(root.optJSONArray("counters"), gloss)
            glossary = gloss
            loaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Bu yeteneğe sahip şampiyonlar (mcoc.gg adıyla, örn. "Heal Block"). */
    fun championsWithAbility(name: String): List<CapHolder> { ensureLoaded(); return abilities[name].orEmpty() }

    /** Bu bağışıklığa sahip şampiyonlar; kinds ile tam/koşullu/güç/süre türü ayırt edilir. */
    fun championsImmuneTo(name: String): List<CapHolder> { ensureLoaded(); return immunities[name].orEmpty() }

    /** Savunmadaki bu yeteneği etkisizleştiren (karşılayan) şampiyonlar. */
    fun championsCountering(defenderAbility: String): List<CapHolder> { ensureLoaded(); return counters[defenderAbility].orEmpty() }

    /** mcoc.gg'nin yetenek tanımı (İngilizce) ya da null. */
    fun glossaryOf(ability: String): String? { ensureLoaded(); return glossary[ability] }
}
