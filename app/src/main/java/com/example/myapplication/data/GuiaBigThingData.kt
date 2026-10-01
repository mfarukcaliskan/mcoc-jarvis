package com.example.myapplication.data

import android.content.Context
import org.json.JSONObject

data class BtRule(val textPt: String, val charges: Int?)

data class BtNode(
    val node: Int,
    val nameEn: String,
    val challengeEn: String,
    val guidePt: String,
    val bestDefenders: List<String>,
    val rules: List<BtRule>
)

/** AW "Big Thing" haritası (Sezon 70, Ekim 2026): GuiaMTC rehberi + resmi harita adları. */
object GuiaBigThingRepository {
    var season: Int = 0
        private set
    var startDate: String = ""
        private set
    var rulesSummary: String = ""
        private set
    var nodes: List<BtNode> = emptyList()
        private set

    fun initialize(context: Context) {
        if (nodes.isNotEmpty()) return
        load(context)
    }

    fun reload(context: Context) {
        load(context)
    }

    private fun load(context: Context) {
        try {
            val root = JSONObject(DataSource.openText(context, "guia_aw_bigthing.json"))
            season = root.getInt("season")
            startDate = root.optString("startDate")
            val r = root.getJSONObject("rules")
            val health = r.getJSONArray("defenderHealthMillions")
            rulesSummary = "Yasak (BAN) yok • ${r.getInt("attackersPerFight")} saldırgan • ${r.getInt("defenders")} savunmacı • " +
                "savunmacı canı ${health.getInt(0)}-${health.getInt(1)} milyon • Güç Yükü başına +%" +
                r.getJSONObject("forceCharge").getInt("attackBonusPercentPerCharge") + " saldırı (yük kazanmadan savunmacı düşmez)"
            val arr = root.getJSONArray("nodes")
            nodes = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val defs = o.getJSONArray("bestDefenders")
                val rulesArr = o.getJSONArray("forceChargeRules")
                BtNode(
                    node = o.getInt("node"),
                    nameEn = o.getString("nameEn"),
                    challengeEn = o.getString("challengeEn"),
                    guidePt = o.getString("guidePt"),
                    bestDefenders = (0 until defs.length()).map { defs.getString(it) },
                    rules = (0 until rulesArr.length()).map { k ->
                        val ro = rulesArr.getJSONObject(k)
                        val ch = ro.optJSONArray("charges")
                        BtRule(ro.getString("textPt"), if (ch != null && ch.length() > 0) ch.getInt(0) else null)
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
