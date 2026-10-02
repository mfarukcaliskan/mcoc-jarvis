package com.example.myapplication.data

import android.content.Context
import org.json.JSONObject

data class SynergyEntry(val id: Int, val name: String, val unique: Boolean, val partners: List<String>, val effects: List<String>, val coPartners: List<String> = emptyList())

/** synergies.json (tools/sync_mcoc.py synergies, mcoc.gg): şampiyon başına temiz sinerji listesi. Tembel yüklenir (~550 KB). */
data class SynergyText(val name: String, val effects: List<String>)

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
            val root = JSONObject(DataSource.openText(ctx, "synergies.json")).getJSONObject("champions")
            val m = mutableMapOf<String, List<SynergyEntry>>()
            for (id in root.keys()) {
                val arr = root.getJSONArray(id)
                m[id] = (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    fun strs(k: String) = o.getJSONArray(k).let { a -> (0 until a.length()).map { a.getString(it) } }
                    SynergyEntry(o.getInt("id"), o.getString("name"), o.optBoolean("unique"), strs("partners"), strs("effects"),
                        o.optJSONArray("coPartners")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList())
                }
            }
            val ref = JSONObject(DataSource.openText(ctx, "synergies.json")).optJSONObject("referenced")
            referenced = ref?.keys()?.asSequence()?.associateWith { k ->
                val o = ref.getJSONObject(k)
                SynergyText(o.getString("name"), o.getJSONArray("effects").let { a -> (0 until a.length()).map { a.getString(it) } })
            } ?: emptyMap()
            map = m
            loaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun textOf(synergyId: Int): SynergyText? { ensureLoaded(); return referenced[synergyId.toString()] }
    fun forChampion(id: String): List<SynergyEntry> { ensureLoaded(); return map[id].orEmpty() }
}
