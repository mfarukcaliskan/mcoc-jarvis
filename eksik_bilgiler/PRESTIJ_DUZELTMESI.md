# Prestij Verisi Düzeltmesi (2026-10-01)

## Sorun
`champions_db.json` içindeki `progressions` tablosu **sentetikti**: her şampiyonda aynı sabit çarpanlar vardı
(6★ R1–R5 için tavanın 0,40 / 0,55 / 0,70 / 0,85 / 1,00'i, taban = tavanın %80'i; "7★" değerleri 6★'ın ×1,7'si).
Üstelik "6★ R5" değeri mcoc.gg'nin gerçek **7★ R5 sig 200** değerine eşitti (yanlış etiket). Prestij hesaplayıcı ve detay
ekranı bu tablodan, ayrıca `(sig/200)^0.8` ile uydurma bir sig eğrisinden hesap yapıyordu: kullanıcıya yanlış değer gösteriliyordu.

## Çözüm
- `tools/sync_mcoc.py prestige` → `prestige.json`: mcoc.gg'nin gerçek tabloları. 268 şampiyon × (7★ R4, R5; Punisher ayrıca R6) × 11 sig noktası
  (0, 20, …, 200). 537 kademe tablosu ham veriyle bağımsız karşılaştırıldı: **0 fark**.
- Sentetik `progressions` alanı `champions_db.json`'dan ve koddan tamamen kaldırıldı (404 kayıt).
- Detay ekranı ve hesaplayıcı: kademe seçimi veriden, sig kaydırıcısı 20'şer adım (yalnızca tablosu olanlarda).
- Haftalık otomatik akışa `prestige` adımı eklendi; `PrestigeDataTest` veri bozulursa (sig artarken prestij düşmesi, eksik R5, geri gelen `progressions`) CI'da yakalar.

## Kaynağın gerçek sınırları (uydurma yok)
- **Üst düzey `pi/attack/health` = R5 + sig 200** noktasıdır (Punisher'da R6 tablosu 46.370, `pi` 39.610 = R5).
- **Saldırı/Can yalnızca R5 sig 200 için** kaynakta var; R4/R6'da gösterilmez ("—").
- **En fazla 6★ olan 63 şampiyon** (ve 7★ olmayan) için tablo yok: tek giriş "en yüksek kademe" (maxPrestige/attack/health); hangi kademe olduğu kaynakta belirtilmemiş.
- **İşaretli tutarsızlıklar (mcoc.gg'nin kendi alanları arasında):** Red Skull, Mister Sinister, Phoenix, Sentry'de `pi_74` ile R4 tablosu >100 fark (Red Skull: 31.810 vs 32.870); Mbaku tablosunda bilinmeyen ("???") değer var. Uygulama tabloyu (sitenin gösterdiği değeri) kullanır.
- **Ascension (A1/A2):** 89 şampiyon `ascendable`. A1/A2 değerleri mcoc.gg'de **kaynak veri değil**, sitenin kendi yuvarlama formülüyle türetiliyor; `prestige.json`'a formül yazıldı ama uygulamada gösterilmiyor.

## Hâlâ doğrulanmamış (bu turda değiştirilmedi)
Ustalık (Mastery) çarpanları — Liquid Courage / Double Edge **+%30 saldırı, +%5 PI**, Sınıf Ustalıkları **+%5 PI** — hiçbir kaynakla doğrulanmadı.
Arayüzde "yaklaşık değerler, doğrulanmadı" olarak işaretlendi; doğru değerler bulunana kadar bilgi amaçlıdır.
