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
| Massacre | Act 7.3 (Kang yolu) | id yok, atlandı | Act 6'da da `masacre` yazımıyla geçiyor, doğru id kontrol edilmeli |

## 2. Act 8 / Act 9 — henüz id normalizasyonu yapılmadı

Act 6 tamamen düzeltildi (snake_case → champions_db.json format). Act 7 zaten doğru formatta yazıldı. **Act 8 ve Act 9 hâlâ eski snake_case id'leri kullanıyor** (örn. `winter_soldier`, `captain_america`) — bu yüzden o iki sahnede "Yoldaki Şampiyonlar" ve "isOwn" rozet özellikleri sessizce bozuk çalışıyor. Diğer sahnelerin .md karşılaştırması bitince Act 8/9 için de aynı id-normalizasyon geçişi yapılmalı.

## 3. Act 7 araştırma ajanlarının belirttiği belirsizlikler (web'den doğrulanamadı)

- **7.1.1, 7.1.2, 7.1.3, 7.1.6**: Yol A/B (kolay/zor) ataması kaynak rehberlerde açıkça etiketlenmemiş, ajan tarafından çıkarım yapıldı — teyit edilmeli.
- **7.2.6 (Gwenmaster)**: "Black Widow (DO)" hangi varyanta karşılık geliyor belirsiz, `blackwidow` kullanıldı; "Ultron (Prime)" için de Prime'a özel id olmadığından düz `ultron` kullanıldı.
- **7.3.6 (Kang)**: "Power Loop" güç kazanma yüzdesi kaynaklarda tutarsız (bir kaynak "%140" gibi mantıksız bir rakam veriyordu) — sayısal değer olmadan niteliksel anlatıldı, doğrulanmalı.
- **7.3.1, 7.3.3, 7.3.4**: "Quantum" alt-varyant adı diğer bölümler kadar net doğrulanamadı, genel Paradox/Entropic Prowess sistemiyle anlatıldı.
- **7.4.5 (İntikam)**: Boss hesaba göre Killmonger / Mister Negative / Namor arasında değişebilir; Killmonger ana boss olarak seçildi ama kesin değil.
- **7.4.3 (Mangog)**: Hatred yığını başına Attack/Crit bonusu yüzdeleri rank'e göre değiştiğinden yaklaşık verildi, kesin sayı yok.

---

*Güncelleme: 2026-07-10 — Act 6 karşılaştırması ve düzeltmesi tamamlandıktan sonra oluşturuldu.*
