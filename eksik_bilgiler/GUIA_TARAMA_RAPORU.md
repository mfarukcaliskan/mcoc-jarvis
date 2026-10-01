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
| Campeões & Rotação (6 sınıf) | Şampiyon profili: ad, 1-10 yıldız, yetenek/bağışıklık maddeleri (PT) | **Alındı** → `guia_champions.json` (327 profil, 0 sınıf uyuşmazlığı). Derece = yazarın görüşü. 6 profilde yıldız sayısı ile yazılı derece çelişiyor (`ratingConflict`), 21 profilde bir bağışıklık maddesi mcoc.gg verisinde yok (`immunityNotInMcocgg`) |
| Tier Lists (Offense/Defense, Eylül 2026) | 3 görsel, sınıf sütunlu tier tablosu + imza seviyesi etiketleri | **Alındı** → `guia_tiers.json` (ofansif 330, defansif 167 şampiyon; 497 karonun hepsi gözle doğrulandı). Savunma listesi sitede yalnızca 10-8 derece satırlarını içeriyor |
| AW - Season 69 | 44 görsel: yol/düğüm/savunmacı/saldırgan tabloları | **Ana tablolar alındı** → `guia_aw_season69.json`: 9 yol × 4 düğüm + SUBS 1-3 + Boss Island = 50 düğüm, 800 karo (hepsi gözle doğrulandı), düğüm etkileri OCR. Yardımcı tablolar (Heal Block, Petrify, bağışıklık/counter listeleri, Hazard Shift listeleri) **alınmadı**: güvenilir karo ayrıştırması yapılamadı, tahminle veri üretilmedi |
| AW - BIG THING (Ekim 2026) | Yeni AW modu (Sezon 70, 7 Ekim 2026), 95 görsel | **Alındı** → `guia_aw_bigthing.json`: 10 düğüm × (resmi harita adı + zorluk, Portekizce açıklama, Güç Yükü kazanma kuralları ve sayıları, 6 en iyi savunmacı [60/60 gözle doğrulandı]) + oyun kuralları (yasak yok, 2 saldırgan, 10 savunmacı 9-15 milyon can, +%25 saldırı/yük). Yardımcı listelerin 17'si `mcoc.gg` oyun verisiyle tam kadro olarak eklendi. Meta ekranında gösteriliyor |
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

## Etkinlik verisi (events.json) ve GuiaMTC okuma doğruluğu

`tools/sync_mcoc.py events` mcoc.gg'nin şampiyon **etiketlerinden** oyun içi etkinlik üyeliğini çıkarır: AW taktikleri (AW1-AW25, rol + açıklama), Raid rolleri ve amplifikatörleri, AQ Ramp, Titan havuz çıkışları (Jun '27 / Mar '27 / Dec '26), 16 kristal havuzu, yükselme (ascension) havuzları, çıkış yılı/evren/özellik grupları, Battlegrounds meta kaydı. Haftalık otomatik akışa dahil.
- **GuiaMTC okuma doğruluğu bu yetkili veriyle ölçüldü:** AW taktik listeleri 550/563 = %97,7 doğru. Raid **rol** listeleri 60/60 doğru, ama amplifikatör **alt listeleri** 99/116 (%85): satır→amplifikatör eşlemem kaymıştı. Bu yüzden `guia_aw_globals.json` ve `guia_raids.json` listeleri yetkili etiket verisiyle değiştirildi, eski okuma `guiaReadIds` olarak saklandı.
- **mcoc.gg Battlegrounds meta kaydı "S.35 (Hafta 3-4)"** ve 20 saldırgan + 22 savunmacı içerir. Bizdeki `meta.json` S40/S41 karo verisinin kaynağı hâlâ doğrulanamadı (çelişki sürüyor).
- Anlamı sitede açıklanmayan etiketler ham saklandı: `Single/Double/Triple - Day N`, `Squad Builder`/`Grade`/`Faction` etiketleri.

## mcoc.gg ile tamamlama (capabilities.json)

`tools/sync_mcoc.py capabilities` mcoc.gg'den **kim hangi yeteneğe/bağışıklığa sahip** dizinini üretir (249 yetenek, 65 bağışıklık, 36 karşı-yetenek, 24 tepki). Her kayıtta dayandığı metin bölümü (`via`), sinerji ve yalnızca-imza (`signatureOnly`) bilgisi var. Doğrulama: 14 özellik için ham veriyle bağımsız sayım, 0 fark.
- GuiaMTC yardımcı listelerinden eşleştirilebilen 10 bölüm (Heal Block, Petrify, Neutralize, Slow, Shock, Nullify, Reverse Controls, Buffs) `guia_aw69_helpers.json` içinde `mcocgg.all` ile **tam kadroya** tamamlandı; GuiaMTC'den okunan ile oyun verisi yan yana (`guiaConfirmed`, `guiaNotInGame`, `gameNotRead`).
- **Birleştirilmeyenler:** GuiaMTC'nin "Counters" listeleri (Autoblock, Evade, Unstoppable, Invisibility) mcoc.gg'nin karşı-yetenek alanıyla yalnızca %60-75 örtüştü (tanımlar farklı: GuiaMTC Slow gibi mekanikle karşılayanları da sayar). Bu yüzden "tamamlama" olarak eklenmedi.
- Counter Bulucu artık savunmacının yeteneklerini etkisizleştiren şampiyonları doğrudan bu dizinden öneriyor.

## AW Big Thing bulguları

- **Kaynak çelişkisi:** GuiaMTC'nin 9. düğüm başlığı "Imunidade a Atordoamento" (Stun Immunity), ama aynı paragrafın açıklaması ve resmi harita "Power Efficiency" (özel saldırılar %50 daha az güç harcar) diyor. Başlık kopyala-yapıştır hatası görünüyor; resmi harita adı esas alındı, çelişki `sourceConflicts`'ta.
- **Çapraz kontrol (düğüm 5):** GuiaMTC'nin "Contra Ataque" listesindeki 5 şampiyondan 4'ü `mcoc.gg` etiketinde var; Spider-Punk yok ama oyun metni ("Counterculture Counter-Attack") mekaniği doğruluyor: etiket dizini eksik, GuiaMTC doğru.
- **Tarih:** Sayfa "Sezon 70 (7 Ekim 2026)" der; yani rehber oyunda başlamadan önce yazılmış, resmi değişiklik olabilir.
- Alınamayanlar: "Reversão de Cura", "Aumentam o Medidor de Combo" ve "Controlam o Poder" listelerinin `mcoc.gg`'de açık karşılığı yok; GuiaMTC'nin kendi görsel seçimleri (şampiyon bazlı not) okunmadı.

## Bilinen eksikler (bu turda alınmayanlar)

- **AW Sezon 69 yardımcı listeleri (17 liste, KISMEN alındı):** `guia_aw69_helpers.json` yalnızca iki bağımsız bölütleme yönteminin (sabit ızgara+altyazı ve satır-parçası) aynı karoyu aynı şampiyon olarak bulduğu **315 kesin eşleşmeyi** içerir. Listeler **eksiktir**: kaynak görsellerdeki karo genişlikleri düzensiz (55-63 px, kenarlar örtüşüyor), hiçbir tek bölütleme tüm karoları doğru kutulayamadı. Listede olmayan şampiyon "GuiaMTC'de yok" demek DEĞİLDİR. Çapraz kontrol: mcoc.gg oyun verisine göre Heal Block 27/28, Petrify 10/10, Reverse Controls 11/12, Neutralize 8/8, Nullify 7/7, Shock 17/18 uyumlu; uyumsuzlar (Ghost Rider, Hulk Immortal, Rhino) koşullu yetenek ya da yanlış tanıma olabilir. İkonla başlayan alt gruplar adlandırılamadı. Kalan 29 görselin tamamı için çözüm: karo adımını görsel başına elle kalibre eden bir bölütleme ya da kaynağın düzenli bir sürümü.
- (Eski not) AW Sezon 69 yardımcı listeleri ilk denemede: karolar bitişik ve arka planları farklı olduğundan sabit adımlı pencere yöntemi 620 pencerenin yalnızca 163'ünü güvenilir eşleştirdi, bazı görsellerde hiç karo bulamadı. Veri uydurmamak için dosyaya eklenmedi. Çözüm: bu görseller için ayrı bir bölütleme (renk bloğu + sinerji işareti ayrımı) gerekiyor.
- **Vurgulu (sarı) karolar:** GuiaMTC sayfası sarı zeminin anlamını açıklamıyor; yalnızca `highlighted` işareti olarak saklandı, yorum yapılmadı.

- Tier listesinde Summoned Symbiote yok (sitede de yok); Wolverine (7-7.5) karosunun imza etiketi kırpılmış, `signature: null`.
- 5 oynanabilir şampiyon GuiaMTC profilinde yok: Weapon X, Summoned Symbiote, Gwenom, Spider-Man Noir, Green Goblin (Stellar Forged) (çoğu sitenin son güncellemesinden sonra çıktı).

- **Etiketsiz yardımcı şerit listeleri:** Wrath "Unstoppable Counters" (kısmi), Poder de Proeza "Remove Prowess Effects" (kısmi), Desviar "Evade & Autoblock Counters", "Atacantes necessários (Hazard Shift)". Küçük, etiketsiz ve farklı çizim sürümlü portrelerde güvenilir eşleşme alınamadı.
- **Disp-ERR-são Mística savunmacıları:** 25'ten 7'si tanımlanamadı (`unidentified: 7`).
- "Iron Doom" (Best Defenders) hangi şampiyon olduğu belirsiz olduğu için dışarıda.
- Görsel içi metinler (düğüm adları, tablo notları) yalnızca etiket ve başlık düzeyinde okundu; tam tablo transkripsiyonu sırada.

## Telif / kaynak notu

İçerik GuiaMTC yazarının emeğidir. Uygulamada kaynak belirtilir; veri **kişisel kullanım** amaçlı alınmıştır. Yayımlamadan önce site sahibine haber vermek veya izin almak önerilir. Üçüncü kişi oyuncu adları hiçbir yerde saklanmaz.

## Relics (Battlecast puanları) ve 7★ Prestij + Relics + Stat Focus (tamamlandı)
- `guia_relics.json`: 24 Battlecast relic için GuiaMTC puanı (x/10), Portekizce etki/kullanım/etkileşim metni; relics.json ile eşleşti (yalnızca The Cosmic Egg'in Guia puanı yok). Uygulamada RelicCard'da gösterilir (GuiaRelicRepository).
- `guia_rank7.json`: 268 şampiyon, 7★ sıralaması; R5/R4 taban ve A1/A2 değerleri. Taban değerler mcoc.gg prestijiyle çapraz doğrulandı (266/268 OCR'dan birebir; Heimdall R5 okunamadı, Archangel R4 OCR 32500 → mcoc.gg değeri kullanıldı, `correctedFromMcocgg` ile işaretli). Ad takma adları değer eşleşmesiyle çözüldü ("White Widow" → Black Widow (Deadly Origin): R5 değeri tekil eşleşti, yine de ad okuması belirsiz).
- Dahil EDİLMEYENLER: focus (saldırı/savunma) ve Stat/Battle Cast relic ikon sütunları (güvenilir ikon eşleştirmesi yapılmadı).

## Metin rehberleri: Necropolis, Manopla, Coliseu, AQ Rampant Evolution (alındı – yalnızca metin)
- `guia_guides.json`: Portekizce metin olduğu gibi (64/54/9/3 paragraf); Necropolis'te 15 rakip + önerilen cevaplar ayrıştırıldı. Meta ekranında "GuiaMTC Rehberleri" bölümü.
- Alınmayanlar (yalnızca görsel): Coliseum tier listesi, AQ Rampant Evolution tier listesi, Awakening Gems (sayfada metin yok), AQ Map 6/7/8 (yüzlerce harita görseli).
- Hazard Shift ve DOT sekmeleri: şampiyon listeleri mcoc.gg `capabilities.json` içinde zaten yetkili olarak var (abilities: Bleed/Poison/Incinerate/Shock/Degeneration/Coldsnap/Plasma/Rupture/Disintegration/Neuroshock/Corrosion; immunities). Guia görsel listeleri ayrıca okunmadı (mcoc.gg listesi esas alındı).

## Eski `tier` alanı (S/A/B/C) – kaynağı belirsiz
- champions_db.json `tier` alanı elle verilmiş, kaynağı yok (sync_mcoc "N/A" yazar). GuiaMTC offense/defense puanıyla zayıf korelasyon (ör. 'C' etiketli 6 şampiyon Guia'da 9'un üstünde). Bu yüzden şampiyon detay başlığı ve overlay artık bu etiketi göstermez; yerine GuiaMTC saldırı/savunma puanı (x/10) gösterilir. Filtre/sıralamadaki eski S/A/B/C hâlâ duruyor – karar bekliyor (kaldırma ya da Guia puanından türetme).
- `howToPlay`/`bestUse` şablon metinleri kaldırıldı (uydurmaydı); gerçek kaynak (mcoc.gg yetenek bölümleri) `abilitySections`'ta.
