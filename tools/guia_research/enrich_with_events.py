"""GuiaMTC dosyalarindaki sampiyon listelerini mcoc.gg events.json (yetkili etiket verisi) ile dogrular ve duzeltir.

- guia_raids.json: amplifikator alt listeleri GuiaMTC gorselinden satir eslemesiyle okunmustu (%85 dogru);
  yetkili etiket listeleriyle degistirilir, eski okuma `guiaReadIds` olarak saklanir.
- guia_aw_globals.json: her taktigin savunma/saldirgan listesi yetkili etiket listesiyle degistirilir
  (okuma %97.7 dogruydu), eski okuma `guiaReadIds` olarak saklanir; fark sayilari rapora yazilir.
Calistirma: python tools/guia_research/enrich_with_events.py   (once: python tools/sync_mcoc.py events)
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
A = os.path.join(ROOT, "app", "src", "main", "assets")

TACTIC_TAGS = {  # guia taktik id -> {rol: mcoc.gg etiket adi}
    "stone_water": {"defenders": "AW: Stone", "attackers": "AW: Water"},
    "wrath_forbearance": {"defenders": "AW: Wraith", "attackers": "AW: Forbearance"},
    "dinosaur_meteor": {"defenders": "AW: Dinosaur", "attackers": "AW: Meteor"},
    "frightful_fantastic": {"defenders": "AW: Frightful", "attackers": "AW: Fantastic"},
    "genesis_coda": {"defenders": "AW: Genesis", "attackers": "AW: Coda"},
    "house_of_mirrors_clarity": {"defenders": "AW: House of Mirrors", "attackers": "AW: Clarity"},
    "provocateur_secutor": {"defenders": "AW: Provocateur", "attackers": "AW: Secutor"},
    "magic_thief_xmagica": {"defenders": "AW: Magic Thief", "attackers": "AW: X-Magica"},
    "ricochet_stabilize": {"defenders": "AW: Ricochet", "attackers": "AW: Stabilize"},
    "crush_2_0": {"defenders": "AW: Crush 2.0"},
}


def main():
    ev = json.load(open(os.path.join(A, "events.json"), encoding="utf-8"))
    tags = {t["name"]: t for t in ev["awTactics"]}

    # --- AW global listeleri
    path = os.path.join(A, "guia_aw_globals.json")
    g = json.load(open(path, encoding="utf-8"))
    read = ok = replaced = 0
    for tac in g["tactics"]:
        mapping = TACTIC_TAGS.get(tac["id"])
        if not mapping:
            continue
        for lst in tac["lists"]:
            name = mapping.get(lst["role"])
            if not name or len(lst["championIds"]) <= 5:
                continue
            auth = tags[name]
            got = set(lst["championIds"])
            read += len(got)
            ok += len(got & set(auth["champions"]))
            lst["guiaReadIds"] = lst["championIds"]
            lst["championIds"] = list(auth["champions"])
            lst["source"] = {"tagId": auth["tagId"], "tagName": auth["name"], "by": "mcoc.gg etiket üyeliği (yetkili)"}
            lst["diffVsGuiaRead"] = {"guiaOnly": sorted(got - set(auth["champions"])), "gameOnly": sorted(set(auth["champions"]) - got)}
            replaced += 1
    g["note"] += (" championIds listeleri artık mcoc.gg etiket üyeliğinden (yetkili); GuiaMTC görselinden okunan eski liste guiaReadIds'te "
                  f"(okuma doğruluğu: {ok}/{read} = {100 * ok / read:.1f}%). guiaOnly/gameOnly farkı sezon sonrası kadro değişikliği ya da okuma hatası olabilir.")
    json.dump(g, open(path, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(f"AW globals: {replaced} liste yetkili veriyle degistirildi; okuma dogrulugu {ok}/{read}")

    # --- Raids
    path = os.path.join(A, "guia_raids.json")
    r = json.load(open(path, encoding="utf-8"))
    auth_boost = {b["name"]: b for rr in ev["raids"] for b in rr["boosts"]}
    auth_role = {rr["role"]: rr for rr in ev["raids"]}
    read = ok = 0
    for role in r["roles"]:
        a = auth_role[role["name"]]
        role["guiaReadIds"] = role["recommendedChampionIds"]
        read += len(role["guiaReadIds"])
        ok += len(set(role["guiaReadIds"]) & set(a["champions"]))
        role["recommendedChampionIds"] = list(a["champions"])
        for b in role["boosts"]:
            ab = auth_boost[b["name"]]
            got = set(b["championIds"])
            read += len(got)
            ok += len(got & set(ab["champions"]))
            b["guiaReadIds"] = b["championIds"]
            b["championIds"] = list(ab["champions"])
            b["source"] = {"tagId": ab["tagId"], "by": "mcoc.gg etiket üyeliği (yetkili)"}
    r["note"] += (f" Şampiyon listeleri mcoc.gg etiket üyeliğinden (yetkili); GuiaMTC görselinden okunan eski listeler guiaReadIds'te "
                  f"(okuma doğruluğu {ok}/{read} = {100 * ok / read:.1f}%; amplifikatör alt listelerinde satır eşlemesi kaymış, bu yüzden değiştirildi).")
    json.dump(r, open(path, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(f"Raids: okuma dogrulugu {ok}/{read}")


if __name__ == "__main__":
    main()
