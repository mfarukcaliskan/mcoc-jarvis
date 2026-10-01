package com.example.myapplication.data

import android.content.Context
import org.json.JSONObject

data class AwChampionRef(val id: String, val highlighted: Boolean)

data class AwNode(
    val node: Int,
    val effects: List<String>,
    val defenders: List<AwChampionRef>,
    val attackers: List<AwChampionRef>
)

/** Bir AW yolu (1-9) ya da bölümü (SUBS 1-3, Boss Island): düğüm listesi. */
data class AwGroup(val id: String, val title: String, val lane: String?, val nodes: List<AwNode>)

object GuiaAwRepository {
    var season: Int = 0
        private set
    var groups: List<AwGroup> = emptyList()
        private set

    fun initialize(context: Context) {
        if (groups.isNotEmpty()) return
        load(context)
    }

    fun reload(context: Context) {
        load(context)
    }

    private fun refs(arr: org.json.JSONArray): List<AwChampionRef> =
        (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            AwChampionRef(o.getString("id"), o.optBoolean("highlighted", false))
        }

    private fun nodes(arr: org.json.JSONArray): List<AwNode> =
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val eff = o.getJSONArray("effects")
            AwNode(
                o.getInt("node"),
                (0 until eff.length()).map { eff.getString(it) },
                refs(o.getJSONArray("defenders")),
                refs(o.getJSONArray("attackers"))
            )
        }

    private fun load(context: Context) {
        try {
            val root = JSONObject(DataSource.openText(context, "guia_aw_season69.json"))
            season = root.getInt("season")
            val list = mutableListOf<AwGroup>()
            val paths = root.getJSONArray("paths")
            for (i in 0 until paths.length()) {
                val p = paths.getJSONObject(i)
                list.add(AwGroup("path${p.getInt("path")}", "Yol ${p.getInt("path")} - ${p.getString("title")}", p.optString("lane"), nodes(p.getJSONArray("nodes"))))
            }
            val secs = root.getJSONArray("sections")
            for (i in 0 until secs.length()) {
                val s = secs.getJSONObject(i)
                list.add(AwGroup(s.getString("id"), s.getString("title"), null, nodes(s.getJSONArray("nodes"))))
            }
            groups = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
