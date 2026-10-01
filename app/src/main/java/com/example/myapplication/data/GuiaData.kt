package com.example.myapplication.data

import android.content.Context
import org.json.JSONObject

/** GuiaMTC "Best Defenders" sayfasından savunmacıya göre counter listesi ve ipucu (Portekizce). */
data class GuiaDefender(
    val id: String,
    val tip: String,
    val counters: List<String>
)

object GuiaRepository {
    private var byDefender: Map<String, GuiaDefender> = emptyMap()

    fun forDefender(championId: String): GuiaDefender? = byDefender[championId]

    fun initialize(context: Context) {
        if (byDefender.isNotEmpty()) return
        load(context)
    }

    /** RemoteDataUpdater yeni veri indirdiğinde çağrılır. */
    fun reload(context: Context) {
        load(context)
    }

    private fun load(context: Context) {
        try {
            val root = JSONObject(DataSource.openText(context, "guia_counters.json"))
            val arr = root.getJSONArray("defenders")
            val map = mutableMapOf<String, GuiaDefender>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val countersArr = o.optJSONArray("counters")
                val counters = if (countersArr == null) emptyList() else (0 until countersArr.length()).map { countersArr.getString(it) }
                map[o.getString("id")] = GuiaDefender(o.getString("id"), o.optString("tip"), counters)
            }
            byDefender = map
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
