package com.example.myapplication.data

import android.content.Context
import org.json.JSONObject

data class SynergyEntry(val id: Int, val name: String, val unique: Boolean, val partners: List<String>, val effects: List<String>, val coPartners: List<String> = emptyList())

/** synergies.json (tools/sync_mcoc.py synergies, mcoc.gg): şampiyon başına temiz sinerji listesi. Tembel yüklenir (~550 KB). */
data class SynergyText(val name: String, val effects: List<String>, val unique: Boolean = false)

object SynergyRepository {
    private var referenced: Map<String, SynergyText> = emptyMap()
    private var appContext: Context? = null
    private var loaded = false
    private var map: Map<String, List<SynergyEntry>> = emptyMap()

    fun initialize(context: Context) { appContext = context.applicationContext }
    fun reload(context: Context) { appContext = context.applicationContext; loaded = false }

    @Synchronized
    private fun ensureLoaded() {
        if (loaded) return
        val ctx = appContext ?: return
        try {
            val doc = JSONObject(DataSource.openText(ctx, "synergies.json"))
            val tx = doc.getJSONObject("texts")
            referenced = tx.keys().asSequence().associateWith { k ->
                val o = tx.getJSONObject(k)
                SynergyText(o.getString("name"), o.getJSONArray("effects").let { a -> (0 until a.length()).map { a.getString(it) } }, o.optBoolean("unique"))
            }
            val root = doc.getJSONObject("champions")
            val m = mutableMapOf<String, List<SynergyEntry>>()
            for (id in root.keys()) {
                val arr = root.getJSONArray(id)
                m[id] = (0 until arr.length()).mapNotNull { i ->
                    val o = arr.getJSONObject(i)
                    val t = referenced[o.getInt("id").toString()] ?: return@mapNotNull null
                    fun strs(k: String) = o.optJSONArray(k)?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList()
                    SynergyEntry(o.getInt("id"), t.name, t.unique, strs("partners"), t.effects, strs("coPartners"))
                }
            }
            map = m
            loaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun textOf(synergyId: Int): SynergyText? { ensureLoaded(); return referenced[synergyId.toString()] }
    fun forChampion(id: String): List<SynergyEntry> { ensureLoaded(); return map[id].orEmpty() }
}
