package com.example.myapplication.data

import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AwRetentionTest {
    private fun assetsDir(): File {
        val d = File("src/main/assets")
        return if (d.isDirectory) d else File("app/src/main/assets")
    }

    @Test
    fun allianceWar_keepsAtMostNewestTwoSeasons() {
        val dir = assetsDir()
        val seasons = mutableSetOf<Int>()
        dir.listFiles { f -> Regex("""guia_aw(_season\d+|\d+_helpers|_bigthing)\.json""").matches(f.name) }!!.forEach {
            seasons += JSONObject(it.readText()).get("season").toString().toInt()
        }
        val meta = JSONObject(File(dir, "meta.json").readText()).getJSONArray("seasons")
        for (i in 0 until meta.length()) {
            val o = meta.getJSONObject(i)
            if (o.getString("mode") == "Alliance War") seasons += o.get("seasonNumber").toString().toInt()
        }
        assertTrue("AW'de en fazla 2 sezon tutulmalı (tools/aw_retention.py): $seasons", seasons.size <= 2)
    }
}
