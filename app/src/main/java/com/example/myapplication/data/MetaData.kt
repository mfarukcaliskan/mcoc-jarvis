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
            id = "bg_s41", mode = "Battlegrounds", seasonNumber = 41,
            title = "Savaş Alanları Sezon 41",
            weekRange = "Hafta 1-4",
            dateRange = "5 Ağustos - 9 Eylül 2026",
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
            id = "bg_s40", mode = "Battlegrounds", seasonNumber = 40,
            title = "Savaş Alanları Sezon 40",
            weekRange = "Hafta 1-4",
            dateRange = "1 Temmuz - 5 Ağustos 2026",
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
            id = "aw_s69", mode = "Alliance War", seasonNumber = 69,
            title = "İttifak Savaşı Sezon 69",
            weekRange = "Hafta 1-4",
            // Bitiş tarihi resmi kaynaklarda henüz yayınlanmamıştı; başlangıç
            // (bir önceki sezonun bittiği gün) doğrulandı, bitiş S68'in 28
            // günlük döngüsünden çıkarıldı — kesin teyit edilirse güncellenmeli.
            dateRange = "12 Ağustos - 9 Eylül 2026 (tahmini)",
            nodes = listOf(
                MetaNode(
                    name = "Ricochet (Defans) / Stabilize (Saldırı)",
                    effect = "Savunmacı her 12 saniyede saldırgana %10 Şiddetli Dengesizlik (Unsteady) debuff'ı uygular (15sn, en fazla 3 yığın, saldırgan uzaktayken durur). Saldırgan her 15 komboda %20 Dayanıklılık (Endurance) pasifi kazanarak buna karşı koyar."
                )
            ),
            bannedChampions = listOf(),
            description = "Ricochet/Stabilize taktiği — bu taktiğin ikinci sezonu, güncellenmiş bir kara liste ile. Dengesizlik debuff'ını yönetebilen veya Dayanıklılık pasifinden faydalanan şampiyonlar öne çıkıyor."
        )
    )
}
