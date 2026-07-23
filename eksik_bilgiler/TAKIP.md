# Eksik / Hatalı Bilgiler Takip Listesi

Bu klasör, quest veri düzeltme sürecinde karşılaşılan ama o an çözülemeyen (eksik şampiyon, belirsiz eşleştirme, doğrulanamayan sayı vb.) noktaları toplar. Tüm sahneler bitince buraya dönüp tek tek çözeceğiz.

---

## 1. champions_db.json'da hiç olmayan şampiyon/varyantlar

Rehber veritabanlarında adı geçen ama uygulamanın 325 şampiyonluk `champions_db.json` dosyasında karşılığı bulunamayan isimler. Şu an en yakın gerçek id'ye düşürülmüş durumdalar (yanlış/eksik sayılır):

| Rehberdeki isim | Geçtiği yer | Şu an kullanılan (yanlış/eksik) fallback | Not |
|---|---|---|---|
| Red Magneto / White Magneto | Act 6 (6.1.2, 6.1.3, 6.2.5, 6.3.2, 6.3.4, 6.3.5, 6.3.6, 6.4.3), Act 7 ajan raporlarında da bahsedildi | `magneto` veya `magnetomarvelnow` | DB'de bu iki özel varyant hiç yok, sadece klasik `magneto` ve `magnetomarvelnow` var |
| Hydra Adaptoid | Act 6.4.5 boss | `adaptoid` | DB'de `adaptoid` veya `hydraadaptoid` hiç yok — boss id'si sahipsiz |
| Grandmaster | Act 6.4.6 boss | `grandmaster` | DB'de bu id hiç yok — boss id'si sahipsiz |
| Cosmic Spider-Man | Act 6.2.5 yol A | `spidermansupreme` | Tam karşılığı yok, tahmini fallback |
| Gwenmaster | Act 7.2.6 boss | `gwenmaster` | Boss-only NPC, roster'da olmaması normal olabilir ama teyit edilmeli |
| Night Carnage / Ice Phoenix | Act 7.1.5, 7.1.6 boss | `nightcarnage`, `icephoenix` | Mash-up bosslar, roster'da olmaması normal olabilir ama teyit edilmeli |
| Massacre | Act 6.3 ve Act 8 (8.2.4, 8.3.4) | id yok, atlandı | `masacre` yazımıyla da geçiyor, doğru id kontrol edilmeli |
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
