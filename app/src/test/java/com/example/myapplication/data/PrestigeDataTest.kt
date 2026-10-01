package com.example.myapplication.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PrestigeDataTest {

    private fun assets(name: String): File {
        // Gradle birim testlerinde çalışma dizini app modülüdür
        val f = File("src/main/assets/$name")
        return if (f.isFile) f else File("app/src/main/assets/$name")
    }

    private fun entry(prestige: List<Int?>?, max: Int? = null) =
        PrestigeEntry(star = 7, rank = 5, prestige = prestige, maxPrestige = max, attack = null, health = null)

    @Test
    fun prestigeAt_mapsIndexToSigStepsAndClamps() {
        val e = entry((0..10).map { 1000 + it * 100 })
        assertEquals(1000, e.prestigeAt(0))            // sig 0
        assertEquals(2000, e.prestigeAt(10))           // sig 200
        assertEquals(1500, e.prestigeAt(5))            // sig 100
        assertEquals(2000, e.prestigeAt(99))           // taşma: son noktaya sıkışır
        assertEquals(1000, e.prestigeAt(-3))           // negatif: ilk noktaya sıkışır
    }

    @Test
    fun prestigeAt_fallsBackToMaxWhenNoTableOrUnknownPoint() {
        assertEquals(17340, entry(null, 17340).prestigeAt(7))
        assertNull(entry(listOf(null, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), null).prestigeAt(0))
        assertEquals(5000, entry(listOf(null, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 5000).prestigeAt(0))
    }

    @Test
    fun defaultIndex_prefersSevenStarRankFive() {
        val info = PrestigeInfo(
            maxStar = 7, ascendable = false, flags = emptyList(),
            entries = listOf(
                PrestigeEntry(7, 4, emptyList(), null, null, null),
                PrestigeEntry(7, 5, emptyList(), null, 1, 1),
                PrestigeEntry(7, 6, emptyList(), null, null, null)
            )
        )
        assertEquals(1, info.defaultIndex)
        val single = PrestigeInfo(6, false, listOf(PrestigeEntry(6, null, null, 17340, 4465, 57737)), emptyList())
        assertEquals(0, single.defaultIndex)
    }

    @Test
    fun bundledPrestigeJson_isStructurallySound() {
        val root = JSONObject(assets("prestige.json").readText())
        val champs = root.getJSONObject("champions")
        assertTrue("beklenenden az şampiyon", champs.length() > 300)
        val ids = champs.keys()
        var sevenStar = 0
        while (ids.hasNext()) {
            val id = ids.next()
            val c = champs.getJSONObject(id)
            val entries = c.getJSONArray("entries")
            assertTrue("$id: giriş yok", entries.length() > 0)
            for (i in 0 until entries.length()) {
                val e = entries.getJSONObject(i)
                val table = e.optJSONArray("prestige")
                if (table != null) {
                    assertEquals("$id: sig noktası sayısı", PrestigeRepository.SIG_POINTS, table.length())
                    var prev = 0
                    for (k in 0 until table.length()) {
                        if (table.isNull(k)) continue
                        val v = table.getInt(k)
                        assertTrue("$id: prestij sig arttıkça düşemez ($prev -> $v)", v >= prev)
                        prev = v
                    }
                } else {
                    assertTrue("$id: tablosuz girişte maxPrestige olmalı", e.getInt("maxPrestige") > 0)
                }
            }
            if (c.getInt("maxStar") == 7) {
                sevenStar++
                var r5Found = false
                for (i in 0 until entries.length()) {
                    val e = entries.getJSONObject(i)
                    if (!e.isNull("rank") && e.getInt("rank") == 5) {
                        r5Found = true
                        assertTrue("$id: R5 saldırı/can eksik", e.getInt("attack") > 0 && e.getInt("health") > 0)
                    }
                }
                assertTrue("$id: 7★ için R5 tablosu yok", r5Found)
            }
        }
        assertTrue("7★ şampiyon sayısı beklenenden az", sevenStar > 200)
    }

    @Test
    fun bundledChampionsDb_hasNoSyntheticProgressions() {
        val text = assets("champions_db.json").readText()
        assertTrue("sentetik 'progressions' alanı geri gelmiş", !text.contains("\"progressions\""))
    }
}
