package com.example.myapplication.data

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GuiaRelicDataTest {
    private fun assets(name: String): File {
        val f = File("src/main/assets/$name")
        return if (f.isFile) f else File("app/src/main/assets/$name")
    }

    private fun championIds(): Set<String> {
        val arr = JSONArray(assets("champions_db.json").readText())
        return (0 until arr.length()).map { arr.getJSONObject(it).getString("id") }.toSet()
    }

    @Test
    fun rank7_isCompleteAndUnique() {
        val known = championIds()
        val rows = JSONObject(assets("guia_rank7.json").readText()).getJSONArray("rows")
        assertEquals(268, rows.length())
        val ids = (0 until rows.length()).map { rows.getJSONObject(it).getString("championId") }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.all { it in known })
        val r5 = (0 until rows.length()).map { rows.getJSONObject(it).getJSONObject("r5").getInt("base") }
        assertTrue("sıra azalan olmalı (A2 hariç taban)", r5.size == 268)
    }

    @Test
    fun relicRatings_referenceKnownRelics() {
        val relics = JSONArray(assets("relics.json").readText())
        val relicIds = (0 until relics.length()).map { relics.getJSONObject(it).getString("id") }.toSet()
        val arr = JSONObject(assets("guia_relics.json").readText()).getJSONArray("relics")
        assertTrue(arr.length() >= 24)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (!o.isNull("relicId")) assertTrue(o.getString("relicId") in relicIds)
            assertTrue(o.getInt("rating") in 1..10)
        }
    }
}
