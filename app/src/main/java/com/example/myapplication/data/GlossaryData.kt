package com.example.myapplication.data

data class GlossaryTerm(
    val id: String,
    val term: String,
    val termTr: String,
    val category: String,  // "Buff", "Debuff", "Mekanik", "Durum"
    val description: String
)

object GlossaryRepository {
    val terms = listOf(
        // === BUFF'LAR ===
        GlossaryTerm("fury", "Fury", "Öfke", "Buff", "Saldırı gücünü belirli bir süre boyunca artırır. Yığınlanabilir."),
        GlossaryTerm("precision", "Precision", "Kesinlik", "Buff", "Kritik vuruş oranını artırır."),
        GlossaryTerm("cruelty", "Cruelty", "Acımasızlık", "Buff", "Kritik hasar oranını artırır."),
        GlossaryTerm("armor_up", "Armor Up", "Zırh Artışı", "Buff", "Zırhı artırarak alınan fiziksel hasarı azaltır."),
        GlossaryTerm("regen", "Regeneration", "İyileşme", "Buff", "Belirli bir süre boyunca can yeniler."),
        GlossaryTerm("power_gain", "Power Gain", "Güç Kazanımı", "Buff", "Zamanla güç barını doldurur."),
        GlossaryTerm("unstoppable", "Unstoppable", "Durdurulamaz", "Buff", "Saldırılar sizi durduramaz, sersemletemez ve geri itemez."),
        GlossaryTerm("indestructible", "Indestructible", "Yenilmez", "Buff", "Canınız 1'in altına düşemez. Sizi öldüren darbeyi siler."),
        GlossaryTerm("true_strike", "True Strike", "Gerçek Vuruş", "Buff", "Blok, Kaçış (Evade) ve Miss etkilerini yok sayar."),
        GlossaryTerm("prowess", "Prowess", "Hüner", "Buff", "Özel Saldırı hasarını artırır. Yığınlanabilir."),
        GlossaryTerm("true_accuracy", "True Accuracy", "Gerçek İsabet", "Buff", "Kaçış (Evade) ve Auto-Block'u yok sayar."),
        GlossaryTerm("unblockable", "Unblockable", "Engellenemez", "Buff", "Saldırılar bloklanamaz."),

        // === DEBUFF'LAR ===
        GlossaryTerm("bleed", "Bleed", "Kanama", "Debuff", "Zamanla fiziksel hasar verir. Yığınlanabilir. Kanama bağışıklığı olan robotlar etkilenmez."),
        GlossaryTerm("poison", "Poison", "Zehir", "Debuff", "Zamanla hasar verir ve iyileşme etkinliğini %30 azaltır."),
        GlossaryTerm("incinerate", "Incinerate", "Yakma", "Debuff", "Zamanla enerji hasarı verir ve zırhı azaltır."),
        GlossaryTerm("shock", "Shock", "Şok", "Debuff", "Zamanla enerji hasarı verir."),
        GlossaryTerm("coldsnap", "Coldsnap", "Ani Soğuk", "Debuff", "Zamanla enerji hasarı verir ve Kaçışı (Evade) devre dışı bırakır."),
        GlossaryTerm("stun", "Stun", "Sersemletme", "Debuff", "Rakibi kısa süreliğine hareketsiz bırakır."),
        GlossaryTerm("armor_break", "Armor Break", "Zırh Kırma", "Debuff", "Rakibin zırhını azaltır. Yığınlanabilir."),
        GlossaryTerm("heal_block", "Heal Block", "İyileşme Engeli", "Debuff", "Rakibin iyileşmesini tamamen engeller."),
        GlossaryTerm("petrify", "Petrify", "Taşlaştırma", "Debuff", "Rakibin güç kazanımını ve iyileşmesini azaltır."),
        GlossaryTerm("fate_seal", "Fate Seal", "Kader Mührü", "Debuff", "Rakibin tüm buff'larını bastırır, yeni buff kazanmasını engeller."),
        GlossaryTerm("power_drain", "Power Drain", "Güç Emme", "Debuff", "Rakibin güç barını azaltır."),
        GlossaryTerm("power_lock", "Power Lock", "Güç Kilidi", "Debuff", "Rakibin güç kazanmasını tamamen engeller."),
        GlossaryTerm("exhaustion", "Exhaustion", "Tükenme", "Debuff", "Rakibin kritik hasar oranını azaltır."),
        GlossaryTerm("weakness", "Weakness", "Zayıflık", "Debuff", "Rakibin saldırı gücünü azaltır."),
        GlossaryTerm("slow", "Slow", "Yavaşlatma", "Debuff", "Durdurulamaz ve Kaçış yeteneklerini devre dışı bırakır."),
        GlossaryTerm("concussion", "Concussion", "Sarsıntı", "Debuff", "Rakibin yetenek doğruluğunu (Ability Accuracy) azaltır."),
        GlossaryTerm("neurotoxin", "Neurotoxin", "Nörotoksin", "Debuff", "Kanama + Zehir birleşimi. Yetenek doğruluğunu %100 azaltır. Başmelek'e özeldir."),
        GlossaryTerm("degen", "Degeneration", "Dejenerasyon", "Debuff", "Zamanla hasar verir. İyileşme ile önlenemez."),

        // === MEKANİKLER ===
        GlossaryTerm("evade", "Evade", "Kaçış", "Mekanik", "Rakibin saldırısından otomatik olarak kaçınma. True Strike veya True Accuracy ile iptal edilir."),
        GlossaryTerm("auto_block", "Auto-Block", "Otomatik Blok", "Mekanik", "Kombo sırasında bile otomatik blok. True Accuracy veya yetenek doğruluğu azaltma ile iptal edilir."),
        GlossaryTerm("parry", "Parry", "Siper", "Mekanik", "Düşmanın saldırısını siperleyerek onu sersemletme. Ustalık ağacından açılır."),
        GlossaryTerm("intercept", "Intercept", "Önleme", "Mekanik", "Rakip ileri atıldığında sizin de ileri atılarak vuruşu karşılama. Zamanlama gerektirir."),
        GlossaryTerm("dex", "Dexterity", "Çeviklik", "Mekanik", "Rakibin saldırısından geriye kaçarak (Swipe Back) kaçınma. Ustalık ağacından açılır."),
        GlossaryTerm("nullify", "Nullify", "Silme", "Mekanik", "Rakibin aktif buff'larını kaldırma. Mistik şampiyonlarda yaygındır."),
        GlossaryTerm("stagger", "Stagger", "Sendeletme", "Mekanik", "Rakibin bir sonraki buff'ını tetiklenmeden önce siler. Doktor Doom'un imza yeteneği."),
        GlossaryTerm("purify", "Purify", "Arındırma", "Mekanik", "Kendi üzerinizdeki debuff'ları temizleme."),
        GlossaryTerm("power_sting", "Power Sting", "Güç İğnesi", "Mekanik", "Rakip Özel Saldırı atarsa hasar alır."),
        GlossaryTerm("phase", "Phase", "Faz (Hayalet Modu)", "Mekanik", "Tüm saldırılar vücudunuzdan geçer (Miss). Ghost ve Kitty Pryde'a özeldir."),
        GlossaryTerm("limbo", "Limbo", "Limbo", "Mekanik", "Alınan hasar iyileşmeye dönüşür ve düşmana yansıtılır. Magik'e özeldir."),

        // === DURUMLAR ===
        GlossaryTerm("miss", "Miss", "Iskalama", "Durum", "Saldırı hedefi ıskalar, hasar vermez. Phase veya bazı yetenekler tetikler."),
        GlossaryTerm("glancing", "Glancing", "Sıyırma", "Durum", "Vuruş tam isabet etmez, hasar %50 azalır ve kritik olmaz."),
        GlossaryTerm("passive", "Passive", "Pasif", "Durum", "Pasif yetenek: Buff/Debuff ikonuyla gösterilir ama Nullify/Purify ile silinemez."),
        GlossaryTerm("buff", "Buff", "Destek", "Durum", "Karaktere geçici güçlendirme sağlayan olumlu etki."),
        GlossaryTerm("debuff", "Debuff", "Zayıflatıcı", "Durum", "Karaktere zamanla zarar veren veya güç azaltan olumsuz etki.")
    )
}
