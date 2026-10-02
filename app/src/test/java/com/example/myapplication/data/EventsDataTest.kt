package com.example.myapplication.data

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class EventsDataTest {

    private fun assets(name: String): File {
        val f = File("src/main/assets/$name")
        return if (f.isFile) f else File("app/src/main/assets/$name")
    }

    private fun championIds(): Set<String> {
        val arr = JSONArray(assets("champions_db.json").readText())
        return (0 until arr.length()).map { arr.getJSONObject(it).getString("id") }.toSet()
    }

    private fun ids(arr: JSONArray?): List<String> =
        if (arr == null) emptyList() else (0 until arr.length()).map { arr.getString(it) }

    @Test
    fun events_awTacticsHaveRolesAndKnownChampions() {
        val known = championIds()
        val root = JSONObject(assets("events.json").readText())
        val aw = root.getJSONArray("awTactics")
        assertTrue("AW taktik sayısı beklenenden az", aw.length() >= 25)
        var withRole = 0
        for (i in 0 until aw.length()) {
            val t = aw.getJSONObject(i)
            val champs = ids(t.optJSONArray("champions"))
            val unknown = champs.filter { it !in known }
            assertTrue("${t.getString("name")}: DB'de olmayan şampiyon $unknown", unknown.isEmpty())
            if (!t.isNull("role")) {
                withRole++
                assertTrue("rol yalnızca defense/attack olabilir", t.getString("role") in listOf("defense", "attack"))
                assertTrue("${t.getString("name")}: açıklamasız", t.optString("descriptionEn").isNotBlank())
            }
        }
        assertTrue("rol/açıklama eşleşmesi çok düşük: $withRole", withRole >= 15)
    }

    @Test
    fun events_defenseAndAttackTacticsComeInPairs() {
        val root = JSONObject(assets("events.json").readText())
        val aw = root.getJSONArray("awTactics")
        var defense = 0
        var attack = 0
        for (i in 0 until aw.length()) {
            when (aw.getJSONObject(i).optString("role")) {
                "defense" -> defense++
                "attack" -> attack++
            }
        }
        // Her savunma taktiğinin bir saldırı karşılığı vardır (Crush 2.0 gibi eski tekil taktikler rolsüzdür)
        assertEquals("savunma/saldırı dengesi bozuk", defense, attack)
    }

    @Test
    fun events_poolsRaidsAndExitsAreSane() {
        val known = championIds()
        val root = JSONObject(assets("events.json").readText())
        val pools = root.getJSONArray("pools")
        assertTrue(pools.length() >= 10)
        for (i in 0 until pools.length()) {
            val p = pools.getJSONObject(i)
            assertTrue("${p.getString("name")}: bilinmeyen şampiyon", ids(p.optJSONArray("champions")).all { it in known })
        }
        val raids = root.getJSONArray("raids")
        assertEquals(3, raids.length())
        for (i in 0 until raids.length()) {
            assertEquals("her rolde 20 şampiyon", 20, raids.getJSONObject(i).getJSONArray("champions").length())
        }
        assertEquals(3, root.getJSONArray("exits").length())
    }

    @Test
    fun capabilitiesJson_isConsistent() {
        val known = championIds()
        val root = JSONObject(assets("capabilities.json").readText())
        val abilities = root.getJSONArray("abilities")
        assertTrue(abilities.length() > 200)
        for (i in 0 until abilities.length()) {
            val a = abilities.getJSONObject(i)
            val champs = a.getJSONArray("champions")
            for (k in 0 until champs.length()) {
                assertTrue("${a.getString("name")}: bilinmeyen ${champs.getJSONObject(k).getString("id")}",
                    champs.getJSONObject(k).getString("id") in known)
            }
        }
    }

    @Test
    fun abilityRefs_pointToExistingSectionLines() {
        val dir = assets("details")
        var checked = 0
        dir.listFiles { f -> f.name.endsWith(".json") }!!.forEach { f ->
            val o = JSONObject(f.readText())
            val refs = o.optJSONObject("abilityRefs") ?: return@forEach
            val secs = o.getJSONArray("abilitySections")
            for (name in refs.keys()) {
                val arr = refs.getJSONArray(name)
                for (i in 0 until arr.length()) {
                    val pr = arr.getJSONArray(i)
                    val content = secs.getJSONObject(pr.getInt(0)).getJSONArray("content")
                    assertTrue("${f.name}: $name satır dışı", pr.getInt(1) < content.length())
                    checked++
                }
            }
        }
        assertTrue("abilityRefs bulunamadı", checked > 1000)
    }
}
