# Eksik / Hatalı Bilgiler Takip Listesi

Bu klasör, quest veri düzeltme sürecinde karşılaşılan ama o an çözülemeyen (eksik şampiyon, belirsiz eşleştirme, doğrulanamayan sayı vb.) noktaları toplar. Tüm sahneler bitince buraya dönüp tek tek çözeceğiz.

---

## 1. champions_db.json'da hiç olmayan şampiyon/varyantlar

### 1a. ÇÖZÜLDÜ (2026-07-24, web araştırmasıyla doğrulandı)

- **Red Magneto / White Magneto**: Bunlar ayrı şampiyon DEĞİL — MCOC topluluğunun kostüm rengine göre kullandığı takma adlar. "Red/Kırmızı Magneto" = klasik `magneto` (kırmızı pelerinli), "White Magneto" = `magnetomarvelnow` (House of X, beyaz kostüm — id tarihsel "Marvel Now" isminden geliyor, Kabam sonradan "House of X" olarak yeniden markaladı, aynı champion). Kaynak: [forums.playcontestofchampions.com/.../magneto-hox-white-mags](https://forums.playcontestofchampions.com/en/discussion/350766/magneto-hox-white-mags-question-about-abilities), House of X kostümünün beyaz olduğu YouTube/eBay referanslarıyla teyit edildi. Bu netleşince dosyalar tek tek kontrol edildi ve id'lerin **Red/White ile ters düştüğü 5 gerçek hata** bulunup düzeltildi: `6_1_3.json` (defender, marvelnow olmalıydı), `6_2_5.json` (idealCounter, marvelnow olmalıydı), `6_3_4.json`, `6_3_5.json`, `6_3_6.json` (idealCounters, üçü de klasik `magneto` olmalıydı ama `magnetomarvelnow` yazılmıştı).
- **Hydra Adaptoid** (Act 6.4.5 boss) ve **Grandmaster** (Act 6.4.6 boss): Web araştırmasıyla doğrulandı — ikisi de MCOC'ta **resmi olarak oynanamaz (unplayable) hikaye bossu**, roster'da hiçbir zaman yer almıyor. DB'de id olmaması hata değil, doğru davranış. Kaynak: [forums.playcontestofchampions.com/.../act-6-4-5-hydra-adaptoid](https://forums.playcontestofchampions.com/en/discussion/306415/act-6-4-5-hydra-adaptoid), [forums.playcontestofchampions.com/.../grandmaster-act-6-4-6](https://forums.playcontestofchampions.com/en/discussion/359198/grandmaster-act-6-4-6).
- **Gwenmaster** (Act 7.2.6), **Night Carnage** (7.1.5), **Ice Phoenix** (7.1.6): Web araştırmasıyla doğrulandı — üçü de M.O.D.O.K. laboratuvarı temalı, resmi olarak oynanamaz "Mashup/Combined Champion" bosslar, roster'da olmaması normal ve beklenen. Kaynak: [marvel-contestofchampions.fandom.com/wiki/Gwenmaster](https://marvel-contestofchampions.fandom.com/wiki/Gwenmaster), [frontlinemcoc.home.blog/.../act-7-1-6](https://frontlinemcoc.home.blog/tag/act-7-1-6/).
- **Massacre** (Act 6.3, Act 8.2.4/8.3.4): DB'de gerçekten var, sadece `masacre` (tek s) yazımıyla kayıtlı. Yanlış yazılan `massacre` id'lerinin hepsi (`7_3_1.json`, `8_2_4.json`, `8_3_4.json`) `masacre`'ye düzeltildi.

### 1b. Hâlâ belirsiz

| Rehberdeki isim | Geçtiği yer | Şu an kullanılan fallback | Not |
|---|---|---|---|
| Cosmic Spider-Man | Act 6.2.5 yol A | `spidermansupreme` | Araştırma net sonuç vermedi: "Spider-Man (Supreme)" (Mystic sınıf) mi yoksa "Spider-Man (Symbiote)" (Cosmic sınıf, adı gibi kozmik kökenli siyah kostüm) mi kastediliyor belirsiz kaldı — id değiştirilmedi, teyit gerekiyor. |
| Bahamut | Act 8.2.6 boss | id yok, atlandı | Boss-only NPC, roster'da olmaması normal olabilir ama teyit edilmeli |
| Black Dwarf | Act 8.2.6, 8.4.6 | id yok, atlandı | DB'de bu id/isim hiç yok |
| Cerastes | Act 8.3.6 boss ("Cerastes Sınavı") | id yok, atlandı | Boss-only NPC, quest adıyla aynı |
| Chronoserpent | Act 9.4.6 boss ("Chronoserpent (Carina) Hesaplaşması") | id yok, atlandı | Boss-only NPC, quest adıyla aynı |
| Collector | Act 8.4.6 | id yok, atlandı | Act 6.4.6'daki "Büyük Koleksiyoncu" (Grandmaster, hâlâ çözülmedi) ile karıştırılmamalı — farklı bir giriş |
| Dread Emperor Doom | Act 9.3.6 boss ("Dread Emperor Doom") | id yok, atlandı | Normal `doctordoom`'dan güçlendirilmiş özel bir versiyon olabilir, düz Doom'a düşürmedim çünkü counter önerileri yanıltıcı olurdu |
| Glykhan | Act 8.4.6 boss ("Glykhan Hesaplaşması") | id yok, atlandı | Boss-only NPC, quest adıyla aynı |
| Jane Foster (Lotan) | Act 9.2.6 boss ("Lotan Yüzleşmesi") | id yok, atlandı | Boss-only NPC, muhtemelen benzersiz bir amalgam |
| Morgan le Fay | Act 9.3.6 | id yok, atlandı | DB'de bu id/isim hiç yok |
| Mystique | Act 9.1.6, 9.3.3, 9.4.2 | id yok, atlandı | DB'de bu id/isim hiç yok |
| Nightmare | Act 9.3.1, 9.3.6 | id yok, atlandı | DB'de bu id/isim hiç yok |
| Quasar | Act 9.4.2, 9.4.3, 9.4.5 | id yok, atlandı | DB'de bu id/isim hiç yok (Photon/Monica Rambeau ile karıştırılmamalı, o da yok) |
| Scourge | Act 9.1.3 | id yok, atlandı | DB'de bu id/isim hiç yok |
| Scytalis | Act 8.1.6 boss ("Scytalis") | id yok, atlandı | Boss-only NPC, quest adıyla aynı |
| Thena | Act 8.4.2 | id yok, atlandı | DB'de bu id/isim hiç yok |
| US Agent | Act 8.4.5 | id yok, atlandı | DB'de bu id/isim hiç yok |
| X-23 (Orochi) | Act 9.1.6 boss | id yok, atlandı | Normal yol savunmacısı olan X-23, `wolverinex23`'e eşlendi (bkz. aşağı) ama bu benzersiz Orochi boss formu değil |

**Act 8/9'da onaylanmış ama yaklaşık (approximate) eşleştirmeler** — id'ler geçerli, teyit edilmesi iyi olur:
- `x_23` → `wolverinex23` ("Wolverine (X-23)") — DB'de düz "X-23" yok, en yakın karşılık bu.
- `spider_man_stark` → `spidermanstarkenhanced` ("Spider-Man (Stark Enhanced)") — tam isim eşleşmesi değil, en yakın karşılık.
- `punk` → `spiderpunk` ("Spider-Punk") — bağlamdan (Arnim Zola boss counter listesi: guardian, photon, punk, deathless_thanos, beta_ray_bill) çıkarıldı, orijinal rehberde muhtemelen kısaltılmış yazılmış.

## 2. Act 8 / Act 9 — id normalizasyonu tamamlandı (2026-07-23)

Act 6 ve 7 zaten doğru formattaydı. **Act 8 ve Act 9'daki snake_case id'ler (`winter_soldier`, `captain_america` vb.) `champions_db.json` formatına çevrildi** — 202 benzersiz id'den 184'ü otomatik/manuel eşleştirmeyle düzeltildi, 18'i (yukarıdaki tablo) gerçekten DB'de karşılığı olmadığı için olduğu gibi bırakıldı. "Yoldaki Şampiyonlar" ve "isOwn" rozet özellikleri artık bu iki sahnede de doğru çalışıyor.

## 3. Act 7 araştırma ajanlarının belirttiği belirsizlikler (web'den doğrulanamadı)

- **7.1.1, 7.1.2, 7.1.3, 7.1.6**: Yol A/B (kolay/zor) ataması kaynak rehberlerde açıkça etiketlenmemiş, ajan tarafından çıkarım yapıldı — teyit edilmeli.
- **7.2.6 (Gwenmaster)**: "Black Widow (DO)" hangi varyanta karşılık geliyor belirsiz, `blackwidow` kullanıldı; "Ultron (Prime)" için de Prime'a özel id olmadığından düz `ultron` kullanıldı.
- **7.3.6 (Kang)**: "Power Loop" güç kazanma yüzdesi kaynaklarda tutarsız (bir kaynak "%140" gibi mantıksız bir rakam veriyordu) — sayısal değer olmadan niteliksel anlatıldı, doğrulanmalı.
- **7.3.1, 7.3.3, 7.3.4**: "Quantum" alt-varyant adı diğer bölümler kadar net doğrulanamadı, genel Paradox/Entropic Prowess sistemiyle anlatıldı.
- **7.4.5 (İntikam)**: Boss hesaba göre Killmonger / Mister Negative / Namor arasında değişebilir; Killmonger ana boss olarak seçildi ama kesin değil.
- **7.4.3 (Mangog)**: Hatred yığını başına Attack/Crit bonusu yüzdeleri rank'e göre değiştiğinden yaklaşık verildi, kesin sayı yok.

---

*Güncelleme: 2026-07-10 — Act 6 karşılaştırması ve düzeltmesi tamamlandıktan sonra oluşturuldu.*
*Güncelleme: 2026-07-23 — Act 8/9 id normalizasyonu tamamlandı, yeni tespit edilen 18 roster-dışı isim listeye eklendi.*
