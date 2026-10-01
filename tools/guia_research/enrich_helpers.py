"""guia_aw69_helpers.json'u mcoc.gg capabilities.json ile tamamlar.

GuiaMTC listesi bolutleme yuzunden eksik; mcoc.gg ise ilgili yetenek/bagisikligin TUM sahiplerini verir.
Her eslestirilebilen bolume `mcocgg` blogu eklenir:
  all            : oyun verisine gore tum sahipler, TUM turler (tam, kosullu, guc, sure, arindirma); idsByKind turlere ayirir
  guiaConfirmed  : GuiaMTC'den okunan ve oyun verisiyle de dogrulananlar
  guiaNotInGame  : GuiaMTC'den okunan ama oyun verisinde olmayanlar (kosullu/yanlis tanima olabilir)
  gameNotRead    : oyun verisinde olup GuiaMTC'den okunamayanlar (GuiaMTC listesinde olabilir de olmayabilir de)
Eslestirilemeyen listelere hicbir sey eklenmez (uydurma yok).
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
A = os.path.join(ROOT, "app", "src", "main", "assets")

# (liste id, bolum adi) -> (tur, mcoc.gg adi, kabul edilen kinds)
MAP = {
    ("aw69_004", "main"): ("ability", "Heal Block", None),
    ("aw69_005", "main"): ("ability", "Petrify", None),
    ("aw69_026", "main"): ("ability", "Neutralize", None),
    ("aw69_039", "Nullify Immune"): ("immunity", "Nullify", None),
    ("aw69_039", "Resistant or Purify"): ("immunity", "Nullify", None),
    ("aw69_008", "Shock Immune"): ("immunity", "Shock", None),
    ("aw69_008", "Resistant or Purify"): ("immunity", "Shock", None),
    ("aw69_012", "Reverse Controls Immune"): ("immunity", "Reversed Controls", None),
    ("aw69_007", "Immune to Buffs"): ("immunity", "Buffs", None),
    ("aw69_016", "Slow (satır başlığı: SLOW)"): ("ability", "Slow", None),
}


def main():
    cap = json.load(open(os.path.join(A, "capabilities.json"), encoding="utf-8"))
    abil = {x["name"]: x for x in cap["abilities"]}
    imm = {x["name"]: x for x in cap["immunities"]}
    path = os.path.join(A, "guia_aw69_helpers.json")
    d = json.load(open(path, encoding="utf-8"))
    n = 0
    for lst in d["lists"]:
        for sec in lst["sections"]:
            key = (lst["id"], sec["name"])
            if key not in MAP:
                continue
            kind, name, kinds = MAP[key]
            src = abil if kind == "ability" else imm
            champs = src[name]["champions"]
            allids = sorted({c["id"] for c in champs})   # tum turler: tam, kosullu, guc, sure, arindirma
            by_kind = {}
            if kind == "immunity":
                for c in champs:
                    for k in c.get("kinds", ["full"]):
                        by_kind.setdefault(k, set()).add(c["id"])
            guia = set(sec["confirmed"])
            sec["mcocgg"] = {
                "property": name,
                "kind": kind,
                "idsByKind": {k: sorted(v) for k, v in sorted(by_kind.items())} if by_kind else None,
                "count": len(allids),
                "all": allids,
                "guiaConfirmed": sorted(guia & set(allids)),
                "guiaNotInGame": sorted(guia - set(allids)),
                "gameNotRead": sorted(set(allids) - guia),
            }
            n += 1
    d["note"] += (" mcocgg bloğu olan bölümlerde oyun verisinden (mcoc.gg) TAM kadro verilmiştir; "
                  "bu, GuiaMTC listesinin tam hali değil, aynı özelliğe sahip tüm şampiyonlardır. "
                  "Bloksuz bölümler için mcoc.gg'de bire bir karşılık bulunamadı.")
    json.dump(d, open(path, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print("zenginlestirilen bolum:", n)


if __name__ == "__main__":
    main()
