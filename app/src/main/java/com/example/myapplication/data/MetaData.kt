package com.example.myapplication.data

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
    val seasons = listOf(
        MetaSeason(
            id = "bg_s40", mode = "Battlegrounds", seasonNumber = 40,
            title = "Savaş Alanları Sezon 40",
            weekRange = "Hafta 1-4",
            dateRange = "24 Haziran - 21 Temmuz 2026",
            nodes = listOf(
                MetaNode(
                    name = "I Am Root!", 
                    effect = "Groot güçlendirilmiş: Tüm Fury buff'ları %50 daha güçlü",
                    bestAttackers = listOf("hercules", "hulkling", "shang_chi"),
                    bestDefenders = listOf("doctordoom", "onslaught", "rintrah")
                ),
                MetaNode(
                    name = "Daunting Doom", 
                    effect = "Doom güçlendirilmiş: Aura hasarı 2 katına çıkar",
                    bestAttackers = listOf("humantorch", "spiderman2099", "void"),
                    bestDefenders = listOf("doctordoom", "onslaught", "wong")
                ),
                MetaNode(
                    name = "Spite", 
                    effect = "Oyuncunun buff'ları sona erdiğinde hasar alır",
                    bestAttackers = listOf("void", "spiderman2099", "warlock"),
                    bestDefenders = listOf("doctordoom", "hercules", "sersi")
                ),
                MetaNode(
                    name = "Power Shield", 
                    effect = "Düşmanın güç barı doldukça savunması artar",
                    bestAttackers = listOf("magik", "doctordoom", "ghost"),
                    bestDefenders = listOf("onslaught", "rintrah", "void")
                ),
                MetaNode(
                    name = "Bane", 
                    effect = "Debuff vurduğunuzda iyileşirsiniz ama debuff bitince hasar alırsınız",
                    bestAttackers = listOf("warlock", "archangel", "omegared"),
                    bestDefenders = listOf("kingpin", "doctordoom", "onslaught")
                )
            ),
            bannedChampions = listOf("Herkül", "Ghost"),
            description = "Bu sezon Fury ve güç kontrolü mekanikleri ön planda. Doom ve Groot özel güçlendirilmiş."
        ),
        MetaSeason(
            id = "bg_s39", mode = "Battlegrounds", seasonNumber = 39,
            title = "Savaş Alanları Sezon 39",
            weekRange = "Hafta 1-4",
            dateRange = "27 Mayıs - 23 Haziran 2026",
            nodes = listOf(
                MetaNode(
                    name = "Buffet", 
                    effect = "Düşman buff aldıkça iyileşir",
                    bestAttackers = listOf("spiderman2099", "doctordoom", "void"),
                    bestDefenders = listOf("hercules", "hulkling", "sersi")
                ),
                MetaNode(
                    name = "Enhanced Fury", 
                    effect = "Tüm Fury buff'ları %100 daha güçlü",
                    bestAttackers = listOf("hercules", "shang_chi", "nickfury"),
                    bestDefenders = listOf("kingpin", "onslaught", "doctordoom")
                ),
                MetaNode(
                    name = "Kinetic Reactor", 
                    effect = "Düşman blok kırdığında güç kazanır",
                    bestAttackers = listOf("ghost", "quake", "spiderman2099"),
                    bestDefenders = listOf("onslaught", "doctordoom", "hercules")
                ),
                MetaNode(
                    name = "Aspect of Chaos", 
                    effect = "Her 7 saniyede rastgele buff/debuff tetiklenir",
                    bestAttackers = listOf("warlock", "void", "archangel"),
                    bestDefenders = listOf("doctordoom", "onslaught", "hulkling")
                )
            ),
            bannedChampions = listOf("Kitty Pryde", "Quake"),
            description = "Fury ve buff bazlı savaş mekanikleri. Buff silme yeteneği olan şampiyonlar kritik."
        ),
        MetaSeason(
            id = "aw_s28", mode = "Alliance War", seasonNumber = 28,
            title = "İttifak Savaşı Sezon 28",
            weekRange = "Hafta 1-8",
            dateRange = "1 Haziran - 28 Temmuz 2026",
            nodes = listOf(
                MetaNode(
                    name = "Flow", 
                    effect = "Düşman 10 vuruştan sonra Durdurulamaz olur",
                    bestAttackers = listOf("shang_chi", "void", "spiderman2099"),
                    bestDefenders = listOf("hercules", "doctordoom", "onslaught")
                ),
                MetaNode(
                    name = "Aggression: Fury", 
                    effect = "Düşmanın saldırısı zamanla artar",
                    bestAttackers = listOf("void", "spiderman2099", "ghost"),
                    bestDefenders = listOf("kingpin", "onslaught", "doctordoom")
                ),
                MetaNode(
                    name = "Unblockable Finale", 
                    effect = "SP3 engellenemez",
                    bestAttackers = listOf("magik", "doctordoom", "warlock"),
                    bestDefenders = listOf("onslaught", "rintrah", "hercules")
                ),
                MetaNode(
                    name = "Enhanced Armor Up", 
                    effect = "Zırh buff'ları %200 güçlenir",
                    bestAttackers = listOf("hulkling", "hercules", "void"),
                    bestDefenders = listOf("doctordoom", "onslaught", "warlock")
                ),
                MetaNode(
                    name = "Debuff Immune", 
                    effect = "Düşman tüm debuff'lara bağışık",
                    bestAttackers = listOf("hercules", "shang_chi", "ghost"),
                    bestDefenders = listOf("onslaught", "doctordoom", "kingpin")
                )
            ),
            bannedChampions = listOf(),
            description = "Savunma ağırlıklı sezon. Durdurulamaz ve Zırh mekanikleri kritik. Debuff immune yollar çok."
        )
    )

    fun fetchRemoteSeasons(onComplete: (List<MetaSeason>) -> Unit) {
        Thread {
            try {
                val url = java.net.URL("https://raw.githubusercontent.com/FarukMCOC/mcoc-meta/main/seasons.json")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                if (connection.responseCode == 200) {
                    val reader = java.io.BufferedReader(java.io.InputStreamReader(connection.inputStream))
                    val jsonString = reader.use { it.readText() }
                    
                    val parsedSeasons = mutableListOf<MetaSeason>()
                    val jsonArray = org.json.JSONArray(jsonString)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val id = obj.getString("id")
                        val mode = obj.getString("mode")
                        val seasonNumber = obj.getInt("seasonNumber")
                        val title = obj.getString("title")
                        val weekRange = obj.getString("weekRange")
                        val dateRange = obj.getString("dateRange")
                        val description = obj.getString("description")
                        
                        val nodesArray = obj.getJSONArray("nodes")
                        val nodes = mutableListOf<MetaNode>()
                        for (j in 0 until nodesArray.length()) {
                            val nodeObj = nodesArray.getJSONObject(j)
                            
                            val bestAttackers = mutableListOf<String>()
                            if (nodeObj.has("bestAttackers")) {
                                val attArr = nodeObj.getJSONArray("bestAttackers")
                                for (k in 0 until attArr.length()) {
                                    bestAttackers.add(attArr.getString(k))
                                }
                            }
                            
                            val bestDefenders = mutableListOf<String>()
                            if (nodeObj.has("bestDefenders")) {
                                val defArr = nodeObj.getJSONArray("bestDefenders")
                                for (k in 0 until defArr.length()) {
                                    bestDefenders.add(defArr.getString(k))
                                }
                            }
                            
                            nodes.add(
                                MetaNode(
                                    name = nodeObj.getString("name"),
                                    effect = nodeObj.getString("effect"),
                                    bestAttackers = bestAttackers,
                                    bestDefenders = bestDefenders
                                )
                            )
                        }
                        
                        val bannedArray = obj.getJSONArray("bannedChampions")
                        val banned = mutableListOf<String>()
                        for (j in 0 until bannedArray.length()) {
                            banned.add(bannedArray.getString(j))
                        }
                        
                        parsedSeasons.add(
                            MetaSeason(
                                id = id,
                                mode = mode,
                                seasonNumber = seasonNumber,
                                title = title,
                                weekRange = weekRange,
                                dateRange = dateRange,
                                nodes = nodes,
                                bannedChampions = banned,
                                description = description
                            )
                        )
                    }
                    onComplete(parsedSeasons)
                    return@Thread
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onComplete(seasons)
        }.start()
    }
}
