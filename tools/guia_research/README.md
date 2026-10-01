# GuiaMTC araştırma betikleri (tek seferlik, yerel)

`guia_aw_globals.json` ve `guia_raids.json` bu betiklerle, 2026-10-01 tarihli site görüntüsünden üretildi.
Haftalık otomatik akışa **dahil değildir** (Windows yerleşik OCR gerektirir ve sonuçlar gözle doğrulandı).
Otomatik güncellenen tek GuiaMTC verisi `tools/sync_guia.py` ile üretilen `guia_counters.json`'dır.

- `cells.py`: bileşik görsellerde portre hücrelerini (siyah boşluk / periyodik kafes) bulur
- `labelocr.py` + `ocr_batch.ps1`: hücre altındaki ad etiketini Windows OCR ile okur ve şampiyon adına çözer
- `match3.py`: arka planı temizleyip mcoc.gg portreleriyle eşler
- `assemble.py`: doğrulanmış sonuçları + elle düzeltmeleri birleştirip `guia_aw_globals.json` üretir

Betikler yerel ara dosyalara (`grids_all.json`, `layout.json`, `pimgs/`...) ve mutlak yollara dayanır; çalıştırılabilir bir araç değil, yöntemin kaydıdır.
