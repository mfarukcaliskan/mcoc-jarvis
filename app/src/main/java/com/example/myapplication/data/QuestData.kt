package com.example.myapplication.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class QuestMap(
    val id: String,                 // Örn: "6_1_1"
    val name: String,               // Örn: "Kara Düzen"
    val globalNodes: List<String> = emptyList(),
    val paths: List<QuestPath> = emptyList(),
    val bosses: List<QuestBoss> = emptyList()
)

data class QuestPath(
    val pathLetter: String,         // Örn: "A", "B"
    val isEasiest: Boolean,
    val pathNodes: List<String> = emptyList(),
    val defenders: List<String> = emptyList(), // Şampiyon ID'leri
    val leadsToBoss: String? = null // Bu yolun çıktığı boss'un championId'si
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
        Act(id = 1, name = "Sahne 1: The Contest (Yarışma)", chapters = listOf(
            Chapter(id = 1, name = "Bölüm 1", quests = listOf(
                QuestItem(id = "1_1_1", name = "1.1.1 - Surrender (Teslim Ol)"),
                QuestItem(id = "1_1_2", name = "1.1.2 - The Prize (Ödül)"),
                QuestItem(id = "1_1_3", name = "1.1.3 - Pursuit (Kovalamaca)"),
                QuestItem(id = "1_1_4", name = "1.1.4 - Sneak Attack (Ani Baskın)"),
                QuestItem(id = "1_1_5", name = "1.1.5 - Pathways (Yol Ayrımları)"),
                QuestItem(id = "1_1_6", name = "1.1.6 - Malice (Hınç)")
            ))
        )),
        Act(id = 2, name = "Sahne 2: Escalation (Tırmanış)", chapters = listOf(
            Chapter(id = 1, name = "Bölüm 1", quests = listOf(
                QuestItem(id = "2_1_1", name = "2.1.1 - New Recruits (Yeni Askerler)"),
                QuestItem(id = "2_1_2", name = "2.1.2 - The Mad Titan (Çılgın Titan)"),
                QuestItem(id = "2_1_3", name = "2.1.3 - Evolution (Evrim)"),
                QuestItem(id = "2_1_4", name = "2.1.4 - Adaptation (Adaptasyon)"),
                QuestItem(id = "2_1_5", name = "2.1.5 - Unstoppable (Durdurulamaz)"),
                QuestItem(id = "2_1_6", name = "2.1.6 - Conquered (Boyun Eğdirildi)")
            ))
        )),
        Act(id = 3, name = "Sahne 3: End Game (Son Oyun)", chapters = listOf(
            Chapter(id = 1, name = "Bölüm 1", quests = listOf(
                QuestItem(id = "3_1_1", name = "3.1.1 - A New Foe (Yeni Bir Düşman)"),
                QuestItem(id = "3_1_2", name = "3.1.2 - Introduction (Tanıtım)"),
                QuestItem(id = "3_1_3", name = "3.1.3 - Power Source (Güç Kaynağı)"),
                QuestItem(id = "3_1_4", name = "3.1.4 - Transcended (Aşkınlaşmış)"),
                QuestItem(id = "3_1_5", name = "3.1.5 - Manipulator (Manipülatör)"),
                QuestItem(id = "3_1_6", name = "3.1.6 - The Automaton (Otomat)")
            )),
            Chapter(id = 2, name = "Bölüm 2", quests = listOf(
                QuestItem(id = "3_2_1", name = "3.2.1 - The Doctor (Doktor)"),
                QuestItem(id = "3_2_2", name = "3.2.2 - The Captain (Kaptan)"),
                QuestItem(id = "3_2_3", name = "3.2.3 - The Rocket (Roket)"),
                QuestItem(id = "3_2_4", name = "3.2.4 - The Creator (Yaratıcı)"),
                QuestItem(id = "3_2_5", name = "3.2.5 - The Original (Orijinal)"),
                QuestItem(id = "3_2_6", name = "3.2.6 - Thanos Enters (Thanos Sahneye Çıkıyor)")
            ))
        )),
        Act(id = 4, name = "Sahne 4: Rebellion (İsyan)", chapters = listOf(
            Chapter(id = 1, name = "Bölüm 1", quests = listOf(
                QuestItem(id = "4_1_1", name = "4.1.1 - Realm Reborn (Diyar Yeniden Doğuyor)"),
                QuestItem(id = "4_1_2", name = "4.1.2 - A Little Help (Küçük Bir Yardım)"),
                QuestItem(id = "4_1_3", name = "4.1.3 - Incognito (Gizli)"),
                QuestItem(id = "4_1_4", name = "4.1.4 - Symbioids (Symbioidler)"),
                QuestItem(id = "4_1_5", name = "4.1.5 - Dark Times (Karanlık Zamanlar)"),
                QuestItem(id = "4_1_6", name = "4.1.6 - Trust (Güven)")
            )),
            Chapter(id = 2, name = "Bölüm 2", quests = listOf(
                QuestItem(id = "4_2_1", name = "4.2.1 - Linked Might (Bağlı Güç)"),
                QuestItem(id = "4_2_2", name = "4.2.2 - The Guillotine Drops (Giyotin Düşüyor)"),
                QuestItem(id = "4_2_3", name = "4.2.3 - Straight Outta Queens (Doğrudan Queens'ten)"),
                QuestItem(id = "4_2_4", name = "4.2.4 - Asgard's Champion (Asgard'ın Şampiyonu)"),
                QuestItem(id = "4_2_5", name = "4.2.5 - Dark Presence (Karanlık Varlık)"),
                QuestItem(id = "4_2_6", name = "4.2.6 - Defiance (Meydan Okuma)")
            )),
            Chapter(id = 3, name = "Bölüm 3", quests = listOf(
                QuestItem(id = "4_3_1", name = "4.3.1 - Triumphant Return (Muzaffer Dönüş)"),
                QuestItem(id = "4_3_2", name = "4.3.2 - Dark Omen (Karanlık Alamet)"),
                QuestItem(id = "4_3_3", name = "4.3.3 - Threats (Tehditler)"),
                QuestItem(id = "4_3_4", name = "4.3.4 - Inscrutable (Anlaşılmaz)"),
                QuestItem(id = "4_3_5", name = "4.3.5 - Ace in the Hole (Son Koz)"),
                QuestItem(id = "4_3_6", name = "4.3.6 - Stand Resolute (Kararlı Duruş)")
            )),
            Chapter(id = 4, name = "Bölüm 4", quests = listOf(
                QuestItem(id = "4_4_1", name = "4.4.1 - Allegiances (Bağlılıklar)"),
                QuestItem(id = "4_4_2", name = "4.4.2 - Plan in Action (Harekete Geçen Plan)"),
                QuestItem(id = "4_4_3", name = "4.4.3 - Dauntless (Yılmaz)"),
                QuestItem(id = "4_4_4", name = "4.4.4 - Known Unknowns (Bilinen Bilinmeyenler)"),
                QuestItem(id = "4_4_5", name = "4.4.5 - Surge of Power (Güç Dalgası)"),
                QuestItem(id = "4_4_6", name = "4.4.6 - Culmination (Doruk Nokta)")
            ))
        )),
        Act(id = 5, name = "Sahne 5: Elder's War (Yaşlının Savaşı)", chapters = listOf(
            Chapter(id = 1, name = "Bölüm 1", quests = listOf(
                QuestItem(id = "5_1_1", name = "5.1.1 - Fair Play (Adil Oyun)"),
                QuestItem(id = "5_1_2", name = "5.1.2 - Weaponized (Silahlandırıldı)"),
                QuestItem(id = "5_1_3", name = "5.1.3 - Back and Forth (Karşılıklı)"),
                QuestItem(id = "5_1_4", name = "5.1.4 - Taunted (Kışkırtıldı)"),
                QuestItem(id = "5_1_5", name = "5.1.5 - Overloaded (Aşırı Yüklendi)"),
                QuestItem(id = "5_1_6", name = "5.1.6 - Dark Angel (Kara Melek)")
            )),
            Chapter(id = 2, name = "Bölüm 2", quests = listOf(
                QuestItem(id = "5_2_1", name = "5.2.1 - Weird Science (Tuhaf Bilim)"),
                QuestItem(id = "5_2_2", name = "5.2.2 - Strange Bedfellows (Garip Müttefikler)"),
                QuestItem(id = "5_2_3", name = "5.2.3 - The Don (Patron)"),
                QuestItem(id = "5_2_4", name = "5.2.4 - Lines in the Sand (Sınır Çizgileri)"),
                QuestItem(id = "5_2_5", name = "5.2.5 - Insurrection (Ayaklanma)"),
                QuestItem(id = "5_2_6", name = "5.2.6 - Abrogation (Feshetme)")
            )),
            Chapter(id = 3, name = "Bölüm 3", quests = listOf(
                QuestItem(id = "5_3_1", name = "5.3.1 - Game Begins Anew (Oyun Yeniden Başlıyor)"),
                QuestItem(id = "5_3_2", name = "5.3.2 - Light in the Tunnel (Tünelin Ucundaki Işık)"),
                QuestItem(id = "5_3_3", name = "5.3.3 - Dire Warning (Ciddi Uyarı)"),
                QuestItem(id = "5_3_4", name = "5.3.4 - Foul Conclusion (Kirli Sonuç)"),
                QuestItem(id = "5_3_5", name = "5.3.5 - Force of Will (İrade Gücü)"),
                QuestItem(id = "5_3_6", name = "5.3.6 - Fitting Punishment (Layık Ceza)")
            )),
            Chapter(id = 4, name = "Bölüm 4", quests = listOf(
                QuestItem(id = "5_4_1", name = "5.4.1 - Contact (Temas)"),
                QuestItem(id = "5_4_2", name = "5.4.2 - Friends and Foes (Dostlar ve Düşmanlar)"),
                QuestItem(id = "5_4_3", name = "5.4.3 - Calculations (Hesaplamalar)"),
                QuestItem(id = "5_4_4", name = "5.4.4 - Do the Impossible (İmkansızı Yap)"),
                QuestItem(id = "5_4_5", name = "5.4.5 - Break the Unbreakable (Kırılmazı Kır)"),
                QuestItem(id = "5_4_6", name = "5.4.6 - Fight the Power (Güce Karşı Savaş)")
            ))
        )),
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
            val jsonString = DataSource.openText(context, "quests/$questId.json")
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
                        defenders = defenders,
                        leadsToBoss = if (pathObj.has("leadsToBoss") && !pathObj.isNull("leadsToBoss")) pathObj.getString("leadsToBoss") else null
                    )
                )
            }

            val bossesArr = obj.getJSONArray("bosses")
            val bosses = mutableListOf<QuestBoss>()
            for (i in 0 until bossesArr.length()) {
                val bossObj = bossesArr.getJSONObject(i)

                val bossNodesArr = bossObj.optJSONArray("bossNodes")
                val bossNodes = mutableListOf<String>()
                if (bossNodesArr != null) {
                    for (j in 0 until bossNodesArr.length()) {
                        bossNodes.add(bossNodesArr.getString(j))
                    }
                }

                val idealCountersArr = bossObj.optJSONArray("idealCounters")
                val idealCounters = mutableListOf<String>()
                if (idealCountersArr != null) {
                    for (j in 0 until idealCountersArr.length()) {
                        idealCounters.add(idealCountersArr.getString(j))
                    }
                }

                bosses.add(
                    QuestBoss(
                        championId = bossObj.getString("championId"),
                        bossNodes = bossNodes,
                        idealCounters = idealCounters
                    )
                )
            }

            QuestMap(
                id = questId,
                name = obj.getString("name"),
                globalNodes = globalNodes,
                paths = paths,
                bosses = bosses
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
