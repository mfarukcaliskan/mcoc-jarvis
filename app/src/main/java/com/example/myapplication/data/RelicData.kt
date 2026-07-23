package com.example.myapplication.data

data class Relic(
    val id: String,
    val name: String,
    val relicClass: ChampionClass,
    val relicType: String,  // "Battlecast" or "Statcast"
    val innateAbilities: List<String>,
    val abilityRunes: List<String>,
    val attributeRunes: List<String>,
    val recommendedChampions: List<String>,
    val description: String
)

object RelicRepository {
    val relics = listOf(
        Relic(
            id = "thor_relic", name = "Thor Andacı", relicClass = ChampionClass.COSMIC,
            relicType = "Battlecast",
            innateAbilities = listOf("Yıldırım Hasarı +15%", "Sersemletme Süresi +0.5s"),
            abilityRunes = listOf("Mjolnir Çağrısı: SP2 sonrası Shock hasarı", "Gökyüzü Öfkesi: Kritik vuruşlarda Stun şansı"),
            attributeRunes = listOf("Saldırı +350", "Enerji Direnci +200"),
            recommendedChampions = listOf("Herkül", "Thor (Jane Foster)", "Silver Surfer"),
            description = "Kozmik şampiyonlar için ideal bir saldırı andacı. Yıldırım hasarı ve sersemletme ekler."
        ),
        Relic(
            id = "wolverine_relic", name = "Wolverine Andacı", relicClass = ChampionClass.MUTANT,
            relicType = "Battlecast",
            innateAbilities = listOf("Kanama Hasarı +20%", "Regen Hızı +10%"),
            abilityRunes = listOf("Adamantium Pençeler: Kanama süresi uzar", "İyileşme Faktörü: Kanama tetiklendiğinde küçük Regen"),
            attributeRunes = listOf("Kritik Oranı +5%", "Can +1500"),
            recommendedChampions = listOf("Başmelek", "Kitty Pryde", "Omega Red", "Sabretooth"),
            description = "Kanama hasarı veren mutant şampiyonlar için mükemmel. İyileşme bonusu ekler."
        ),
        Relic(
            id = "ultron_relic", name = "Ultron Andacı", relicClass = ChampionClass.TECH,
            relicType = "Statcast",
            innateAbilities = listOf("Zırh +500", "Enerji Direnci +300"),
            abilityRunes = listOf("Vibranium Kalkan: Blok sırasında hasar yansıtma", "Kendini Tamir: SP3 sonrası küçük Regen"),
            attributeRunes = listOf("Zırh +400", "Blok Becerisi +8%"),
            recommendedChampions = listOf("Nimrod", "Warlock", "Ghost", "Sentinel"),
            description = "Savunma odaklı teknoloji andacı. Zırh ve enerji direnci artışı sağlar."
        ),
        Relic(
            id = "ant_man_relic", name = "Ant-Man Andacı", relicClass = ChampionClass.SCIENCE,
            relicType = "Battlecast",
            innateAbilities = listOf("Glancing +15%", "Fury Süresi +1s"),
            abilityRunes = listOf("Küçülme: Glancing tetiklendiğinde Fury kazanma", "Büyüme: SP2 sonrası Unstoppable"),
            attributeRunes = listOf("Saldırı +300", "Kritik Hasar +8%"),
            recommendedChampions = listOf("Ghost", "Wasp", "Yellowjacket"),
            description = "Ghost ile birlikte kullanıldığında muazzam sinerji. Glancing ve Fury bonusları."
        ),
        Relic(
            id = "doctor_strange_relic", name = "Doctor Strange Andacı", relicClass = ChampionClass.MYSTIC,
            relicType = "Battlecast",
            innateAbilities = listOf("Nullify Gücü +25%", "Güç Kazanımı +10%"),
            abilityRunes = listOf("Agamotto'nun Gözü: Nullify sonrası Prowess", "Sonsuzluk Taşı: SP1'de Power Drain ekle"),
            attributeRunes = listOf("Saldırı +280", "Enerji Direnci +250"),
            recommendedChampions = listOf("Doktor Doom", "Doctor Strange", "Scarlet Witch", "Magik"),
            description = "Buff silme ve güç kontrolü yapan mistik şampiyonlar için biçilmiş kaftan."
        ),
        Relic(
            id = "daredevil_relic", name = "Daredevil Andacı", relicClass = ChampionClass.SKILL,
            relicType = "Statcast",
            innateAbilities = listOf("Yetenek Doğruluğu +10%", "Kaçış Hassasiyeti +15%"),
            abilityRunes = listOf("Radar Algısı: Kaçışı iptal et", "Sopa Ustalığı: Stun süresi uzar"),
            attributeRunes = listOf("Kritik Oranı +6%", "Saldırı +250"),
            recommendedChampions = listOf("Kingpin", "Nick Fury", "Black Widow"),
            description = "Beceri sınıfı şampiyonlar için Yetenek Doğruluğu ve Kaçış iptali sağlar."
        ),
        Relic(
            id = "ghost_rider_relic", name = "Ghost Rider Andacı", relicClass = ChampionClass.MYSTIC,
            relicType = "Battlecast",
            innateAbilities = listOf("Judgment Hasarı +20%", "Fate Seal Süresi +1s"),
            abilityRunes = listOf("Ceza Zinciri: Judgment sonrası ek Bleed", "Cehennem Alevi: SP3'te Incinerate"),
            attributeRunes = listOf("Saldırı +320", "Can +1200"),
            recommendedChampions = listOf("Blade (Stellar-Forged)", "Ghost Rider", "Mephisto"),
            description = "Blade-Ghost Rider sinerjisi ile beraber kullanıldığında Danger Sense'i güçlendirir."
        ),
        Relic(
            id = "thanos_relic", name = "Thanos Andacı", relicClass = ChampionClass.COSMIC,
            relicType = "Statcast",
            innateAbilities = listOf("Tüm İstatistikler +5%", "Güç Kazanımı +8%"),
            abilityRunes = listOf("Sonsuzluk Eldiveni: SP3 sonrası tüm buff'ları sil", "Titan Gücü: Fury yığınları artar"),
            attributeRunes = listOf("Saldırı +400", "Can +2000"),
            recommendedChampions = listOf("Corvus Glaive", "Proxima Midnight", "Thanos", "America Chavez"),
            description = "Black Order şampiyonlarıyla birlikte kullanıldığında çok güçlü genel istatistik andacı."
        ),
        Relic(
            id = "captain_america_relic", name = "Captain America Andacı", relicClass = ChampionClass.SCIENCE,
            relicType = "Statcast",
            innateAbilities = listOf("Blok Becerisi +12%", "Petrify +10%"),
            abilityRunes = listOf("Vibranium Kalkan: Blok sırasında güç kazanma", "Avengers Çağrısı: Avengers tag'ı olanlara +10% saldırı"),
            attributeRunes = listOf("Zırh +350", "Blok Becerisi +10%"),
            recommendedChampions = listOf("Captain America (IW)", "Nick Fury", "Iron Man"),
            description = "Savunma odaklı bilim andacı. Blok becerisi ve Petrify bonusları."
        ),
        Relic(
            id = "spider_man_relic", name = "Spider-Man Andacı", relicClass = ChampionClass.MYSTIC,
            relicType = "Battlecast",
            innateAbilities = listOf("Kaçış Şansı +10%", "True Strike Süresi +0.5s"),
            abilityRunes = listOf("Örümcek Ağı: SP1 sonrası Slow debuff", "Evade Mastery: Kaçış sonrası Precision"),
            attributeRunes = listOf("Kritik Oranı +5%", "Kritik Hasar +10%"),
            recommendedChampions = listOf("Spider-Man (Pavitr)", "Shathra", "Spider-Gwen"),
            description = "Spider-Verse şampiyonlarıyla güçlü sinerji. Kaçış ve True Strike bonusları."
        ),
        Relic(
            id = "venom_relic", name = "Venom Andacı", relicClass = ChampionClass.COSMIC,
            relicType = "Battlecast",
            innateAbilities = listOf("True Strike Süresi +1s", "Armor Break Gücü +15%"),
            abilityRunes = listOf("Simbiyot Bağı: True Strike sırasında ek hasar", "Genetik Hafıza: Spider-Verse düşmanlara +20% hasar"),
            attributeRunes = listOf("Saldırı +350", "Kritik Hasar +8%"),
            recommendedChampions = listOf("Venom", "Carnage", "Knull"),
            description = "Simbiyot ve Kozmik şampiyonlar için saldırı odaklı andaç."
        ),
        Relic(
            id = "phoenix_relic", name = "Phoenix Andacı", relicClass = ChampionClass.COSMIC,
            relicType = "Battlecast",
            innateAbilities = listOf("Incinerate Hasarı +20%", "Prowess +15%"),
            abilityRunes = listOf("Kozmik Alev: SP2 sonrası Incinerate", "Yeniden Doğuş: Ölüm anında %30 can ile diriliş"),
            attributeRunes = listOf("Enerji Saldırısı +300", "Enerji Direnci +200"),
            recommendedChampions = listOf("Madelyne Pryor", "Phoenix", "Cyclops"),
            description = "Enerji hasarı ve Yakma odaklı kozmik andaç. Madelyne Pryor ile mükemmel uyum."
        )
    )
}
