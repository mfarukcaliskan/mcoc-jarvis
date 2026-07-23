package com.example.myapplication.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class QuestMap(
    val id: String,                 // Örn: "6_1_1"
    val name: String,               // Örn: "Kara Düzen"
    val globalNodes: List<String> = emptyList(),
    val paths: List<QuestPath> = emptyList(),
    val boss: QuestBoss
)

data class QuestPath(
    val pathLetter: String,         // Örn: "A", "B"
    val isEasiest: Boolean,
    val pathNodes: List<String> = emptyList(),
    val defenders: List<String> = emptyList() // Şampiyon ID'leri
)

data class QuestBoss(
    val championId: String,         // Şampiyon ID
    val bossNodes: List<String> = emptyList(),
    val idealCounters: List<String> = emptyList() // En iyi şampiyon ID'leri
)

object QuestRepository {
    data class Act(val id: Int, val name: String, val chapters: List<Chapter>)
    data class Chapter(val id: Int, val name: String, val quests: List<QuestItem>)
    data class QuestItem(val id: String, val name: String)

    val acts = listOf(
        Act(id = 6, name = "Sahne 6: Yıkım", chapters = listOf(
            Chapter(id = 1, name = "Bölüm 1 - Cavalier Yolu", quests = listOf(
                QuestItem(id = "6_1_1", name = "6.1.1 - Kara Düzen"),
                QuestItem(id = "6_1_2", name = "6.1.2 - Aşırı Güç"),
                QuestItem(id = "6_1_3", name = "6.1.3 - Bir Babanın Kaygısı"),
                QuestItem(id = "6_1_4", name = "6.1.4 - Oyundaki Taşlar"),
                QuestItem(id = "6_1_5", name = "6.1.5 - Karşı Koyma"),
                QuestItem(id = "6_1_6", name = "6.1.6 - Çapraz Ateş")
            )),
            Chapter(id = 2, name = "Bölüm 2 - Güç Sınavı", quests = listOf(
                QuestItem(id = "6_2_1", name = "6.2.1 - Gücün Amacı"),
                QuestItem(id = "6_2_2", name = "6.2.2 - Koparılmış"),
                QuestItem(id = "6_2_3", name = "6.2.3 - Kafa Belası"),
                QuestItem(id = "6_2_4", name = "6.2.4 - Suç Unsurları"),
                QuestItem(id = "6_2_5", name = "6.2.5 - Güvensizlik"),
                QuestItem(id = "6_2_6", name = "6.2.6 - Şampiyonun Yükselişi")
            )),
            Chapter(id = 3, name = "Bölüm 3 - Üstat Sınavı", quests = listOf(
                QuestItem(id = "6_3_1", name = "6.3.1 - Tehlikeli Arayış"),
                QuestItem(id = "6_3_2", name = "6.3.2 - Gizli Operasyon"),
                QuestItem(id = "6_3_3", name = "6.3.3 - Plazma Tehlikesi"),
                QuestItem(id = "6_3_4", name = "6.3.4 - Kurnazlık"),
                QuestItem(id = "6_3_5", name = "6.3.5 - Zehirli Bataklık"),
                QuestItem(id = "6_3_6", name = "6.3.6 - Kalkan Savaşı")
            )),
            Chapter(id = 4, name = "Bölüm 4 - Taht Kırıcı", quests = listOf(
                QuestItem(id = "6_4_1", name = "6.4.1 - Buzlu Cehennem"),
                QuestItem(id = "6_4_2", name = "6.4.2 - Gök Gürültüsü"),
                QuestItem(id = "6_4_3", name = "6.4.3 - Karanlık Kanatlar"),
                QuestItem(id = "6_4_4", name = "6.4.4 - Yıldız Gücü"),
                QuestItem(id = "6_4_5", name = "6.4.5 - Hidra Kalesi"),
                QuestItem(id = "6_4_6", name = "6.4.6 - Büyük Koleksiyoncu")
            ))
        )),
        Act(id = 7, name = "Sahne 7: Yükseliş", chapters = listOf(
            Chapter(id = 1, name = "Bölüm 1 - Başlangıç", quests = listOf(
                QuestItem(id = "7_1_1", name = "7.1.1 - Avcının İzi"),
                QuestItem(id = "7_1_2", name = "7.1.2 - Yeşil Terör"),
                QuestItem(id = "7_1_3", name = "7.1.3 - Buzlu Vizyon"),
                QuestItem(id = "7_1_4", name = "7.1.4 - Ölüm Tanrıçası"),
                QuestItem(id = "7_1_5", name = "7.1.5 - Gece Karnajı"),
                QuestItem(id = "7_1_6", name = "7.1.6 - Buz Anka Kuşu")
            )),
            Chapter(id = 2, name = "Bölüm 2 - Karşılaşma", quests = listOf(
                QuestItem(id = "7_2_1", name = "7.2.1 - Geri Tepme"),
                QuestItem(id = "7_2_2", name = "7.2.2 - İkili Tehdit"),
                QuestItem(id = "7_2_3", name = "7.2.3 - Enerji Kabulu"),
                QuestItem(id = "7_2_4", name = "7.2.4 - Bataklik Canavarı"),
                QuestItem(id = "7_2_5", name = "7.2.5 - Kare Kare"),
                QuestItem(id = "7_2_6", name = "7.2.6 - Gwenmaster Sınavı")
            )),
            Chapter(id = 3, name = "Bölüm 3 - Kaotik Düzen", quests = listOf(
                QuestItem(id = "7_3_1", name = "7.3.1 - Özel Teslimat"),
                QuestItem(id = "7_3_2", name = "7.3.2 - Güçlendirilmiş"),
                QuestItem(id = "7_3_3", name = "7.3.3 - Gizli Örümcek"),
                QuestItem(id = "7_3_4", name = "7.3.4 - Karıştırıcı Usta"),
                QuestItem(id = "7_3_5", name = "7.3.5 - Teknoloji Savaşı"),
                QuestItem(id = "7_3_6", name = "7.3.6 - Fatih Kang")
            )),
            Chapter(id = 4, name = "Bölüm 4 - Büyük Yüzleşme", quests = listOf(
                QuestItem(id = "7_4_1", name = "7.4.1 - Mekanik Düşman"),
                QuestItem(id = "7_4_2", name = "7.4.2 - Çapraz Kemikler"),
                QuestItem(id = "7_4_3", name = "7.4.3 - Mangog'un Öfkesi"),
                QuestItem(id = "7_4_4", name = "7.4.4 - Mojo Şovu"),
                QuestItem(id = "7_4_5", name = "7.4.5 - İntikam"),
                QuestItem(id = "7_4_6", name = "7.4.6 - Kang'ın Dönüşü")
            ))
        )),
        Act(id = 8, name = "Sahne 8: Kozmos", chapters = listOf(
            Chapter(id = 1, name = "Bölüm 1 - Kozmik Güç", quests = listOf(
                QuestItem(id = "8_1_1", name = "8.1.1 - Kırmızı Kurukafa"),
                QuestItem(id = "8_1_2", name = "8.1.2 - Savaş Makinesi"),
                QuestItem(id = "8_1_3", name = "8.1.3 - Psiko-Man"),
                QuestItem(id = "8_1_4", name = "8.1.4 - Proxima'nın Mızrağı"),
                QuestItem(id = "8_1_5", name = "8.1.5 - Ölümsüz Savaşçı"),
                QuestItem(id = "8_1_6", name = "8.1.6 - Scytalis")
            )),
            Chapter(id = 2, name = "Bölüm 2 - Bahamut Tehdidi", quests = listOf(
                QuestItem(id = "8_2_1", name = "8.2.1 - Vizyonun Dönüşü"),
                QuestItem(id = "8_2_2", name = "8.2.2 - Ölümsüz Paralı Asker"),
                QuestItem(id = "8_2_3", name = "8.2.3 - Joe Fixit'in Kumar Masası"),
                QuestItem(id = "8_2_4", name = "8.2.4 - Akrep'in Zehri"),
                QuestItem(id = "8_2_5", name = "8.2.5 - Peni Parker'ın Robotu"),
                QuestItem(id = "8_2_6", name = "8.2.6 - Bahamut Savaşı")
            )),
            Chapter(id = 3, name = "Bölüm 3 - Kasap ve Kurban", quests = listOf(
                QuestItem(id = "8_3_1", name = "8.3.1 - Kasap ve Kurban"),
                QuestItem(id = "8_3_2", name = "8.3.2 - Dikenlerin Altında"),
                QuestItem(id = "8_3_3", name = "8.3.3 - Karanlık Hükümran"),
                QuestItem(id = "8_3_4", name = "8.3.4 - Kaya Gibi"),
                QuestItem(id = "8_3_5", name = "8.3.5 - Plazma Fırtınası"),
                QuestItem(id = "8_3_6", name = "8.3.6 - Cerastes Sınavı")
            )),
            Chapter(id = 4, name = "Bölüm 4 - Büyük Yüzleşme", quests = listOf(
                QuestItem(id = "8_4_1", name = "8.4.1 - Çapraz Ateş"),
                QuestItem(id = "8_4_2", name = "8.4.2 - Korku Taktikleri"),
                QuestItem(id = "8_4_3", name = "8.4.3 - Valkürlerin Dansı"),
                QuestItem(id = "8_4_4", name = "8.4.4 - Kaya ve Toz"),
                QuestItem(id = "8_4_5", name = "8.4.5 - Vatanseverin Yolu"),
                QuestItem(id = "8_4_6", name = "8.4.6 - Glykhan Hesaplaşması")
            ))
        )),
        Act(id = 9, name = "Sahne 9: Hesaplaşma", chapters = listOf(
            Chapter(id = 1, name = "Bölüm 1 - Orochi'nin Yükselişi", quests = listOf(
                QuestItem(id = "9_1_1", name = "9.1.1 - Öfke ve Kan"),
                QuestItem(id = "9_1_2", name = "9.1.2 - Fırtına Öncesi"),
                QuestItem(id = "9_1_3", name = "9.1.3 - Gözcü"),
                QuestItem(id = "9_1_4", name = "9.1.4 - Kara Dul'un Zehri"),
                QuestItem(id = "9_1_5", name = "9.1.5 - Şok Dalgası"),
                QuestItem(id = "9_1_6", name = "9.1.6 - Orochi Tapınağı")
            )),
            Chapter(id = 2, name = "Bölüm 2 - Lotan'ın Saldırısı", quests = listOf(
                QuestItem(id = "9_2_1", name = "9.2.1 - Kurbağa Bataklığı"),
                QuestItem(id = "9_2_2", name = "9.2.2 - Karınca Yuvası"),
                QuestItem(id = "9_2_3", name = "9.2.3 - Engellenemez Fırtına"),
                QuestItem(id = "9_2_4", name = "9.2.4 - Emici Kalkan"),
                QuestItem(id = "9_2_5", name = "9.2.5 - Odaklanma Alanı"),
                QuestItem(id = "9_2_6", name = "9.2.6 - Lotan Yüzleşmesi")
            )),
            Chapter(id = 3, name = "Bölüm 3 - İmparator Doom", quests = listOf(
                QuestItem(id = "9_3_1", name = "9.3.1 - İllüzyon Savaşları"),
                QuestItem(id = "9_3_2", name = "9.3.2 - Optik Patlama"),
                QuestItem(id = "9_3_3", name = "9.3.3 - Genetik Sapma"),
                QuestItem(id = "9_3_4", name = "9.3.4 - Negatif Enerji"),
                QuestItem(id = "9_3_5", name = "9.3.5 - Zihin Oyunları"),
                QuestItem(id = "9_3_6", name = "9.3.6 - Dread Emperor Doom")
            )),
            Chapter(id = 4, name = "Bölüm 4 - Carina'nın Dönüşü", quests = listOf(
                QuestItem(id = "9_4_1", name = "9.4.1 - Kozmik Parazit"),
                QuestItem(id = "9_4_2", name = "9.4.2 - Gen Laboratuvarı"),
                QuestItem(id = "9_4_3", name = "9.4.3 - Arcade Dünyası"),
                QuestItem(id = "9_4_4", name = "9.4.4 - Zümrüdüanka Gücü"),
                QuestItem(id = "9_4_5", name = "9.4.5 - Çelik Zırh"),
                QuestItem(id = "9_4_6", name = "9.4.6 - Chronoserpent (Carina) Hesaplaşması")
            ))
        ))
    )

    fun loadQuestMap(context: Context, questId: String): QuestMap? {
        return try {
            val jsonString = context.assets.open("quests/$questId.json").bufferedReader().use { it.readText() }
            val obj = JSONObject(jsonString)
            
            val globalNodesArr = obj.optJSONArray("globalNodes")
            val globalNodes = mutableListOf<String>()
            if (globalNodesArr != null) {
                for (i in 0 until globalNodesArr.length()) {
                    globalNodes.add(globalNodesArr.getString(i))
                }
            }
            
            val pathsArr = obj.getJSONArray("paths")
            val paths = mutableListOf<QuestPath>()
            for (i in 0 until pathsArr.length()) {
                val pathObj = pathsArr.getJSONObject(i)
                
                val pathNodesArr = pathObj.optJSONArray("pathNodes")
                val pathNodes = mutableListOf<String>()
                if (pathNodesArr != null) {
                    for (j in 0 until pathNodesArr.length()) {
                        pathNodes.add(pathNodesArr.getString(j))
                    }
                }
                
                val defendersArr = pathObj.optJSONArray("defenders")
                val defenders = mutableListOf<String>()
                if (defendersArr != null) {
                    for (j in 0 until defendersArr.length()) {
                        defenders.add(defendersArr.getString(j))
                    }
                }
                
                paths.add(
                    QuestPath(
                        pathLetter = pathObj.getString("pathLetter"),
                        isEasiest = pathObj.getBoolean("isEasiest"),
                        pathNodes = pathNodes,
                        defenders = defenders
                    )
                )
            }
            
            val bossObj = obj.getJSONObject("boss")
            val bossNodesArr = bossObj.optJSONArray("bossNodes")
            val bossNodes = mutableListOf<String>()
            if (bossNodesArr != null) {
                for (i in 0 until bossNodesArr.length()) {
                    bossNodes.add(bossNodesArr.getString(i))
                }
            }
            
            val idealCountersArr = bossObj.optJSONArray("idealCounters")
            val idealCounters = mutableListOf<String>()
            if (idealCountersArr != null) {
                for (i in 0 until idealCountersArr.length()) {
                    idealCounters.add(idealCountersArr.getString(i))
                }
            }
            
            QuestMap(
                id = questId,
                name = obj.getString("name"),
                globalNodes = globalNodes,
                paths = paths,
                boss = QuestBoss(
                    championId = bossObj.getString("championId"),
                    bossNodes = bossNodes,
                    idealCounters = idealCounters
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
