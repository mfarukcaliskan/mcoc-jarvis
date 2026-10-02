"""AW (Ittifak Savasi) sezon tutma kurali: veritabaninda yalnizca EN YENI 2 sezon kalir
(guncel + bir onceki); daha eski sezonlarin dosyalari ve meta.json kayitlari silinir.

Sezon kaynaklari (sezon numarasi icerigin `season`/`seasonNumber` alanindan okunur):
  - meta.json  -> mode == "Alliance War" kayitlari (seasonNumber)
  - guia_aw_season<N>.json, guia_aw<N>_helpers.json (season)
  - guia_aw_bigthing.json (season)
Yeni sezon eklenirken (ornegin S71) dosyalari ayni adlandirmayla ekleyip bu scripti calistirmak yeterli:
    python tools/aw_retention.py          # uygular
    python tools/aw_retention.py --check  # yalnizca raporlar, degistirmez (cikis kodu 1: fazla sezon var)
Haftalik is akisi (sync-data.yml) bunu manifest'ten once calistirir.
"""
import glob
import json
import os
import sys

KEEP = 2
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "app", "src", "main", "assets")


def season_files():
    out = []
    for pat in ("guia_aw_season*.json", "guia_aw*_helpers.json", "guia_aw_bigthing.json"):
        for p in glob.glob(os.path.join(A, pat)):
            try:
                n = int(json.load(open(p, encoding="utf-8")).get("season"))
            except Exception:
                continue
            out.append((n, p))
    return out


def main(check=False):
    meta_path = os.path.join(A, "meta.json")
    meta = json.load(open(meta_path, encoding="utf-8"))
    files = season_files()
    meta_aw = [(int(s["seasonNumber"]), s) for s in meta["seasons"] if s.get("mode") == "Alliance War"]
    seasons = sorted({n for n, _ in files} | {n for n, _ in meta_aw}, reverse=True)
    keep = set(seasons[:KEEP])
    drop = [n for n in seasons if n not in keep]
    print(f"AW sezonlari: {seasons}  | tutulan: {sorted(keep)} | silinecek: {sorted(drop)}")
    if check:
        return 1 if drop else 0
    for n, p in files:
        if n not in keep:
            os.remove(p)
            print("  silindi:", os.path.basename(p))
    kept = [s for s in meta["seasons"] if s.get("mode") != "Alliance War" or int(s["seasonNumber"]) in keep]
    if len(kept) != len(meta["seasons"]):
        meta["seasons"] = kept
        json.dump(meta, open(meta_path, "w", encoding="utf-8"), ensure_ascii=False, indent=2)
        print("  meta.json: eski AW sezon kayitlari cikarildi")
    return 0


if __name__ == "__main__":
    sys.exit(main("--check" in sys.argv))
