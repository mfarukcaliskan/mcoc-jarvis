# GuiaMTC (guiamtc.com) Tarama Raporu

Tarama tarihi: **2026-10-01**. Site bir Google Sites sayfasıdır; 66 sayfa ve ~3.800 görsel girdisi tarandı, hiçbir sayfa/görsel atlanmadı.
İçerik büyük oranda **görsel** (tablo, tier listesi, portre ızgaraları); metin çoğunlukla başlık ve ipuçlarıdır.

## Okuma yöntemi ve doğruluk güvenceleri

1. **Etiketli ızgaralar:** her portrenin altındaki ad etiketi Windows yerleşik OCR'ıyla okundu **ve** portre mcoc.gg portreleriyle görüntü benzerliğiyle eşlendi. İki sinyal uyuşunca kabul edildi.
2. **Etiketsiz ızgaralar:** yalnızca görüntü eşleşmesi; eşleşmelerin tamamı gözle (portre + referans yan yana) denetlendi.
3. **Belirsiz/çelişkili hücreler** elle incelendi. Emin olunamayanlar **listeye alınmadı** (`unidentified` sayacı).
4. Hiçbir şampiyon tahmin edilmedi. Üç sinyalli doğrulama: Raids'te her amplifikatör şampiyonu kendi rolünün listesinde de çıkıyor (0 eksik).
5. **Mekanikler** mcoc.gg `content.json` taktik metinleriyle karşılaştırıldı.

## Güncellik bulguları (oyunla paralellik)

| Konu | Bulgu |
|---|---|
| Site genel | Güncel: Eylül 2026 tier listeleri, AW Sezon 69, Ekim 2026 BIG THING, Eylül 2026 Raids |
| Global rehberlerinin **şampiyon listeleri** | Görsel başlıklarındaki sezon damgasına göre eski sezonlara ait: Genesis/Coda 56-57, Wrath 61-62, Dinosaur 64-65, Stone/Water 66-67, Ricochet 68-69, Frightful 60. Mekanikler geçerli olabilir ama listeler o sezonun kadrosudur (sonradan çıkan şampiyonlar yok). |
| Sayısal değerler | **Eskimiş:** Adam Warlock prestiji GuiaMTC'de 33.367, mcoc.gg'de 33.370 (oyun yamasıyla yeniden ayar). Sayısal veride mcoc.gg esas alınır. |
| Magic Thief | **Çelişki:** GuiaMTC "her 10 sn, en fazla 5 yığın", mcoc.gg "her 20 sn, en fazla 3 yığın". Derece/sürüm farkı olabilir. |
| Ricochet / Stabilize (S68-69) | Savunma tarafı iki kaynakta da aynı. **Saldırı pasifinde çelişki:** GuiaMTC %30 Direnç (olumsuz etki süresi -%30), mcoc.gg %20 Dayanıklılık (yalnız Dengesizlik -%10/yığın). meta.json'da iki sürüm de belirtildi. |
| BG meta (mcoc.gg) | mcoc.gg'nin son BG meta kaydı "S.35"; bizdeki S40/S41 karo verisinin kaynağı doğrulanamadı. |

## Sekme sekme durum

| Sekme | İçerik | Durum |
|---|---|---|
| Best Defenders (6 sınıf) | Savunmacı başına Portekizce ipucu + counter portreleri | **Alındı** → `guia_counters.json` (152 savunmacı, 1.154 counter, haftalık otomatik güncellenir) |
| Globais AW (15 sayfa) + Ricochet | Taktik mekaniği (PT) + savunmacı/saldırgan listeleri | **Alındı** → `guia_aw_globals.json` (16 taktik). Etiketsiz yardımcı şerit listeleri kısmi (aşağıya bakın) |
| Raids [September 2026] | 3 rol × 4 amplifikatör, şampiyon listeleri | **Alındı** → `guia_raids.json` |
| Campeões & Rotação (6 sınıf) | Şampiyon profili: ad, 1-10 yıldız, yetenek/bağışıklık maddeleri (PT) | Metin okunabilir; ayrıştırma sırada |
| Tier Lists (Offense/Defense, Eylül 2026) | 3 görsel, sınıf sütunlu tier tablosu + imza seviyesi etiketleri | Özel ayrıştırıcı gerekiyor; sırada |
| AW - Season 69 | 44 görsel: yol/düğüm/savunmacı/saldırgan tabloları | Tablo ayrıştırıcı gerekiyor; sırada |
| AW - BIG THING (Ekim 2026) | Yeni AW modu, 94 görsel | Sırada |
| Immunities / Abilities / DOT | Bağışıklık, yetenek, DOT listeleri | mcoc.gg ile çapraz doğrulanacak; mcoc.gg birincil kaynak |
| Hazard Shift 2026 | 9 görsel, karışık yerleşim | İçerik mcoc.gg bağışıklıklarıyla örtüşüyor; ayrıntılı alınmadı |
| 7 Star Prestige / Relics | 9 görsel | Sırada; sayısal değerler için mcoc.gg esas |
| Relíquias (6 sınıf + giriş) | Andaç önerileri (metin ağırlıklı) | Sırada |
| AQ Mapa 6 / Mapa 7 / Map 8 / Rampant Evolution | Alliance Quest haritaları (düğüm başına şampiyon portreleri, 300+ görsel) | Sırada |
| Necropolis, Manopla do Grão-Mestre, Coliseum - Mr. Negative | Etkinlik rehberleri | Sırada |
| Awakening Gems | 2 görsel (Haziran 2026) | Sırada |
| Duels (6 sınıf) | Şampiyon + **üçüncü kişi oyuncu takma adları** | **Alınmadı** (kişisel veri; şampiyon listesi mcoc.gg'de zaten var) |
| AWS69 Stats | Oyuncu sıralamaları | **Alınmadı** (kişisel veri) |
| Eventos / Contacts, Home | Hizmet/iletişim/duyuru | **Alınmadı** (uygulama verisi değil) |

## Bilinen eksikler (bu turda alınmayanlar)

- **Etiketsiz yardımcı şerit listeleri:** Wrath "Unstoppable Counters" (kısmi), Poder de Proeza "Remove Prowess Effects" (kısmi), Desviar "Evade & Autoblock Counters", "Atacantes necessários (Hazard Shift)". Küçük, etiketsiz ve farklı çizim sürümlü portrelerde güvenilir eşleşme alınamadı.
- **Disp-ERR-são Mística savunmacıları:** 25'ten 7'si tanımlanamadı (`unidentified: 7`).
- "Iron Doom" (Best Defenders) hangi şampiyon olduğu belirsiz olduğu için dışarıda.
- Görsel içi metinler (düğüm adları, tablo notları) yalnızca etiket ve başlık düzeyinde okundu; tam tablo transkripsiyonu sırada.

## Telif / kaynak notu

İçerik GuiaMTC yazarının emeğidir. Uygulamada kaynak belirtilir; veri **kişisel kullanım** amaçlı alınmıştır. Yayımlamadan önce site sahibine haber vermek veya izin almak önerilir. Üçüncü kişi oyuncu adları hiçbir yerde saklanmaz.
