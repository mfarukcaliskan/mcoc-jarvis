# JARVIS Veri Rehberi

Bu dosya uygulamanın veritabanını (`app/src/main/assets/`) sade anlatır: **hangi dosya ne, nereden geliyor, nasıl güncelleniyor, ne kadar güvenilir.**
Tek tek karşılaştırma sonuçları için: `DOGRULAMA_RAPORU.md`. Eski, tarihsel notlar: `eksik_bilgiler/` (arşiv, güncel durum için bu rehbere bakın).

## Güvenilirlik seviyeleri

| Seviye | Anlamı |
|---|---|
| **A** | mcoc.gg'den otomatik üretilir **ve** canlı siteyle şampiyon şampiyon karşılaştırıldı (bkz. DOGRULAMA_RAPORU.md) |
| **B** | mcoc.gg'den otomatik üretilir, tek tek siteyle karşılaştırılmadı (aynı kaynaktan, yapısal testlerden geçer) |
| **C** | GuiaMTC'den (Portekizce, yazarın görüşü / görselden okunan liste). Oyun verisi değil, yorumdur |
| **D** | Elle girilmiş ya da eski, kaynağı doğrulanmamış. Güvenmeyin |

## Şampiyon verisi (en önemli kısım)

| Dosya | İçerik | Kaynak / güncelleme | Seviye |
|---|---|---|---|
| `champions_db.json` | 334 oynanabilir şampiyon: sınıf, prestij, saldırı/can/krit/zırh/blok, 7 sıralama, bağışıklıklar, yetenek adları, focus, çıkış tarihi, Strong Matchups/Counters, reacts, relic önerisi, etiketler. (Ayrıca 73 oynanamaz boss/NPC kaydı: `isPlayable=false`, eski veri) | mcoc.gg, haftalık (`sync_mcoc.py apply`) | A (oynanabilir), D (oynanamaz 73) |
| `details/<id>.json` | Şampiyon başına yetenek metinleri (`abilitySections`, İngilizce, mcoc.gg'den olduğu gibi) + hangi satırın hangi yeteneğe/bağışıklığa/counter'a ait olduğu (`abilityRefs`, `immunityRefs`, `counterRefs`) + ilgili sinerji kimlikleri | mcoc.gg, haftalık | A |
| `champion_extra.json` | Takma ad, yıldız aralığı, ascend, Raid rolü, ham direnç/delme, görünür etiketler, alternatif relic'ler | mcoc.gg, haftalık (`extras`) | A (etiketler), B (diğerleri) |
| `prestige.json` | 7★ R4/R5 × 11 sig noktası gerçek prestij tabloları (A1/A2 sitenin formülüyle) | mcoc.gg, haftalık | A |
| `synergies.json` | `texts`: sinerji metinleri (her metin tek kez). `champions`: şampiyon başına sinerji kimliği + ortaklar (karşı tarafın listelediği ters sinerjiler dahil) | mcoc.gg, haftalık (`synergies`) | A (ortak listesi), B (metin) |
| `capabilities.json` | Yetenek sözlüğü ("?" açıklamaları), hangi şampiyon hangi yeteneği/bağışıklığı/counter'ı taşıyor | mcoc.gg, haftalık | B |

## Etkinlik / oyun içi

| Dosya | İçerik | Kaynak | Seviye |
|---|---|---|---|
| `events.json` | AW taktikleri ve kadroları, Raid rolleri, havuzlar, çıkış grupları, afiliasyon/özellik grupları | mcoc.gg etiketleri, haftalık | B |
| `meta.json` | AW sezon notları ve Battlegrounds sezonları | elle (GuiaMTC + mcoc.gg'den) | D (BG sezon numarası mcoc.gg ile çelişiyor: mcoc.gg S.35, bizde S40/S41) |
| `guia_aw_season<N>.json`, `guia_aw<N>_helpers.json`, `guia_aw_bigthing.json` | AW sezon rehberleri (yalnızca **en yeni 2 sezon** tutulur: `tools/aw_retention.py`) | GuiaMTC (görselden okunmuş), mcoc.gg ile tamamlanmış | C |
| `guia_aw_globals.json`, `guia_raids.json` | AW genel kuralları / Raid rolleri; şampiyon listeleri mcoc.gg etiketlerinden | GuiaMTC metni + mcoc.gg listesi | C (metin), B (liste) |

## Relic verisi

| Dosya | İçerik | Kaynak | Seviye |
|---|---|---|---|
| `relics.json` | 145 andaç: tür, sınıf, doğal yetenek, rune'lar, prestij, önerilen şampiyonlar | mcoc.gg, haftalık | B |
| `relic_statcast.json` | Statcast kademe tablosu | mcoc.gg | B |
| `guia_relics.json` | 24 Battlecast puanı (x/10) ve Portekizce kullanım yorumu | GuiaMTC | C |

## GuiaMTC (yorum katmanı, Portekizce)

`guia_counters.json` (en iyi savunmacılar ve counter listeleri), `guia_tiers.json` (saldırı/savunma puanı 1–10), `guia_champions.json` (şampiyon profilleri), `guia_rank7.json` (7★ prestij sıralaması), `guia_guides.json` (Necropolis, Manopla, Coliseum, AQ Rampant Evolution rehber metinleri). Hepsi seviye **C**: yazarın görüşü; oyundaki gerçekle birebir olması beklenmez. `guia_rank7.json` içindeki sayılar mcoc.gg ile çapraz doğrulandı.

## Diğer

| Dosya | İçerik | Seviye |
|---|---|---|
| `quests/*.json` (168) | Eski görev/harita araştırma verisi | D (bu çalışmada doğrulanmadı) |
| `data_manifest.json` | Dosya özetleri ve `dataVersion`; uygulama bunu okuyup yalnızca değişen dosyaları indirir | — |

## Nasıl güncel kalır?

1. Her pazartesi GitHub Action (`.github/workflows/sync-data.yml`) mcoc.gg ve GuiaMTC'yi çeker, verileri üretir, AW sezon kuralını uygular, manifest'i günceller ve değişiklik varsa commit eder.
2. Uygulama açılışında (günde en fazla 1) manifest'i kontrol eder, değişen dosyaları indirir.
3. Yeni şampiyon oyuna eklendiğinde (örn. 2026-10-08'de Carina, Colossus (Age of Apocalypse), Ghost Rider (Robbie Reyes)) `sync_mcoc.py apply` otomatik ekler. Henüz yayınlanmamış şampiyonlar da mcoc.gg'de görünür ve çıkış tarihleri ileridedir.

**Bilinen sınırlar:** GuiaMTC verisi görsel ağırlıklıdır; yeni sezon düğümleri elle çıkarılır. Bazı sitedeki bölümler yalnızca ikondur (vuruş türleri, relic ikonları) ve metne çevrilemedi. `champions_db.json` içindeki eski `tier` (S/A/B/C) alanı kaynaksızdır ve arayüzde gösterilmez.
