package com.example.myapplication.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject


/** Sezon/karo verisi meta.json'dan okunur; sezon geçişi için APK güncellemesi gerekmez. */
data class MetaSeason(
    val id: String,
    val mode: String,            // "Battlegrounds" or "Alliance War"
    val seasonNumber: Int,
    val title: String,
    val weekRange: String,
    val dateRange: String,
    val nodes: List<MetaNode>,
    val bannedChampions: List<String>,
    val description: String
)

data class MetaNode(
    val name: String,
    val effect: String,
    val bestAttackers: List<String> = emptyList(),
    val bestDefenders: List<String> = emptyList()
)

object MetaRepository {
    var seasons: List<MetaSeason> = emptyList()
        private set

    fun initialize(context: Context) {
        if (seasons.isNotEmpty()) return
        loadFromDataSource(context)
    }

    /** RemoteDataUpdater yeni veri indirdiğinde çağrılır; sezonları baştan okur. */
    fun reload(context: Context) {
        loadFromDataSource(context)
    }

    private fun strings(arr: JSONArray?): List<String> =
        if (arr == null) emptyList() else (0 until arr.length()).map { arr.getString(it) }

    private fun loadFromDataSource(context: Context) {
        try {
            val root = JSONObject(DataSource.openText(context, "meta.json"))
            val arr = root.getJSONArray("seasons")
            seasons = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val nodesArr = o.getJSONArray("nodes")
                MetaSeason(
                    id = o.getString("id"),
                    mode = o.getString("mode"),
                    seasonNumber = o.getInt("seasonNumber"),
                    title = o.getString("title"),
                    weekRange = o.optString("weekRange"),
                    dateRange = o.optString("dateRange"),
                    nodes = (0 until nodesArr.length()).map { j ->
                        val n = nodesArr.getJSONObject(j)
                        MetaNode(
                            name = n.getString("name"),
                            effect = n.getString("effect"),
                            bestAttackers = strings(n.optJSONArray("bestAttackers")),
                            bestDefenders = strings(n.optJSONArray("bestDefenders"))
                        )
                    },
                    bannedChampions = strings(o.optJSONArray("bannedChampions")),
                    description = o.optString("description")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
