#!/usr/bin/env python3
"""mcoc.gg -> JARVIS veri senkronizasyonu.

Kullanim (proje kokunden):
  python tools/sync_mcoc.py fetch    # mcoc.gg JSON'larini tools/.cache/mcoc altina indirir ve dogrular
  python tools/sync_mcoc.py report   # bizim veriyle mcoc.gg arasindaki farklari yazdirir (dosya degistirmez)
  python tools/sync_mcoc.py apply    # yetenek metinlerini ve yeni sampiyonlari assets'e yazar
Ardindan: python generate_manifest.py && python validate_quest_ids.py

Kaynak: https://mcoc.gg/json/*.json (sitenin kendi arayuzunun kullandigi statik dosyalar).
Istekler sirayla yapilir, aralarinda kisa bekleme vardir.
"""
import html
import json
import os
import re
import sys
import time
import urllib.error
import urllib.request

BASE = "https://mcoc.gg/json/"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CACHE = os.path.join(ROOT, "tools", ".cache", "mcoc")
ASSETS = os.path.join(ROOT, "app", "src", "main", "assets")
UA = {"User-Agent": "Mozilla/5.0 (JARVIS-sync)"}

GLOBAL_FILES = ["champions", "abilities", "immunities", "tags", "class", "focus", "ranks", "relics",
                "relics_abilities", "relics_attributes", "synergies", "roles", "content"]

# bizdeki id -> mcoc.gg 'image' (kimlikleri farkli olan sampiyonlar)
ID_ALIASES = {"agathaharkness": "agatha"}
CLASS_NAME = {1: "COSMIC", 2: "TECH", 3: "MUTANT", 4: "SKILL", 5: "SCIENCE", 6: "MYSTIC", 7: "SUPERIOR"}
# Oynanabilir olmayan etkinlik/NPC varyantlari: roster'a eklenmez
NON_ROSTER_PREFIXES = ("antivenomoid", "doombot", "henchpool", "sentinelbot", "symbioid")
FILLER = "Uyanış Yeteneği: İstatistikleri ve dövüş yeteneklerini güçlendirir."


def fetch_json(rel):
    path = os.path.join(CACHE, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    req = urllib.request.Request(BASE + rel, headers=UA)
    with urllib.request.urlopen(req, timeout=30) as r:
        body = r.read()
    parsed = json.loads(body.decode("utf-8"))  # HTML hata sayfasi gelirse burada patlar
    if "data" not in parsed and "abilities" not in parsed:
        raise ValueError(f"{rel}: beklenmeyen yapi")
    with open(path, "wb") as f:
        f.write(body)


def load(rel):
    with open(os.path.join(CACHE, rel), encoding="utf-8") as f:
        return json.load(f)


def load_ours():
    with open(os.path.join(ASSETS, "champions_db.json"), encoding="utf-8") as f:
        return json.load(f)


def cmd_fetch():
    for name in GLOBAL_FILES:
        fetch_json(name + ".json")
        time.sleep(0.2)
    champs = load("champions.json")["data"]
    missing = []
    for c in champs:
        fetch_json(f"champions/{c['image']}.json")
        time.sleep(0.1)
        if 7 in c.get("rarity", []):
            try:
                fetch_json(f"prestige/{c['image']}.json")
            except urllib.error.HTTPError as e:
                missing.append((c["image"], e.code))
            time.sleep(0.1)
    print(f"{len(champs)} sampiyon indirildi; prestij dosyasi eksik: {missing or 'yok'}")


def clean(text):
    text = re.sub(r"<br\s*/?>", "\n", text)
    text = re.sub(r"<[^>]+>", "", text)
    return html.unescape(text).strip()


def table(rows):
    """id'leri hem int hem str olarak aranabilir yapar (mcoc.gg ikisini de kullaniyor)."""
    out = {}
    for r in rows:
        out[r["id"]] = r
        out[str(r["id"])] = r
        if str(r["id"]).isdigit():
            out[int(r["id"])] = r
    return out


def gg_to_ours_id(image):
    for ours, gg in ID_ALIASES.items():
        if gg == image:
            return ours
    return image


def ours_to_gg_image(our_id):
    return ID_ALIASES.get(our_id, our_id)


def cmd_report():
    gg = load("champions.json")["data"]
    ours = {c["id"]: c for c in load_ours()}
    gg_by_image = {g["image"]: g for g in gg}
    matched = [i for i in ours if ours_to_gg_image(i) in gg_by_image]
    new = [g for g in gg if gg_to_ours_id(g["image"]) not in ours]
    roster_new = [g for g in new if not g["image"].startswith(NON_ROSTER_PREFIXES)]
    print(f"mcoc.gg: {len(gg)} sampiyon | bizde: {len(ours)} | eslesen: {len(matched)}")
    print("bizde olmayan OYNANABILIR sampiyonlar:", [(g["image"], g["date"]) for g in roster_new])
    print("bizde olmayan etkinlik/NPC varyantlari:", len(new) - len(roster_new))
    stat_diff = sum(
        1 for i in matched
        if (ours[i]["attack"], ours[i]["health"], ours[i]["prestige"])
        != (gg_by_image[ours_to_gg_image(i)]["attack"], gg_by_image[ours_to_gg_image(i)]["health"], gg_by_image[ours_to_gg_image(i)]["pi"])
    )
    print("attack/health/prestige farkli olan sampiyon:", stat_diff)
    filler = 0
    for i in matched:
        p = os.path.join(ASSETS, "details", f"{i}.json")
        if os.path.isfile(p):
            with open(p, encoding="utf-8") as f:
                if FILLER in json.load(f).get("signatureAbility", ""):
                    filler += 1
    print("imza yetenegi jenerik dolgu metni olan detay dosyasi:", filler)
    with open(os.path.join(ASSETS, "relics.json"), encoding="utf-8") as f:
        rel_ours = json.load(f)
    rel_gg = load("relics.json")["data"]
    name_diff = sum(1 for a, b in zip(rel_ours, rel_gg) if a["name"] != b["name"])
    print(f"andac: bizde {len(rel_ours)} | mcoc.gg {len(rel_gg)} | isim farki {name_diff}")


def sections_for(image):
    with open(os.path.join(CACHE, "champions", f"{image}.json"), encoding="utf-8") as f:
        raw = json.load(f)["abilities"]
    out = []
    for a in raw:
        content = [clean(c) for c in a.get("content", []) if clean(c)]
        if content:
            out.append({"title": clean(a["title"]), "content": content})
    return out


def signature_text(sections):
    parts = []
    for s in sections:
        if s["title"].startswith("Signature Ability"):
            name = s["title"].split(" - ", 1)[1] if " - " in s["title"] else ""
            body = "\n".join(s["content"])
            parts.append(f"{name}\n{body}" if name else body)
    return "\n\n".join(parts)


def update_details(our_id, name, image):
    path = os.path.join(ASSETS, "details", f"{our_id}.json")
    if os.path.isfile(path):
        with open(path, encoding="utf-8") as f:
            d = json.load(f)
    else:
        d = {"id": our_id, "name": name, "abilityDetails": {}, "synergies": [],
             "howToPlay": "", "bestUse": "", "signatureAbility": ""}
    sections = sections_for(image)
    d["abilitySections"] = sections
    d["signatureAbility"] = signature_text(sections)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(d, f, ensure_ascii=False, indent=4)


def parse_rank(cell):
    digits = re.sub(r"\D", "", str(cell[0]))
    return int(digits) if digits else 0


def build_champion(g, lk, known_tags):
    def names(ids, tbl, key="name"):
        return list(dict.fromkeys(tbl[i][key] for i in ids if i in tbl))

    immunities = names(g.get("immune", []), lk["immune"])
    r = lk["ranks"].get(g["id"], {})
    month, day, year = g["date"].split("/")  # mcoc.gg tarihleri AY/GUN/YIL
    relic_ids = ([g["relic"]] if g.get("relic") else []) + g.get("alt_relics", [])
    rank = lambda key: parse_rank(r[key]) if key in r else 0
    return {
        "id": g["image"],
        "name": g["name"],
        "mcocClass": CLASS_NAME[g["class"]],
        "tier": "N/A",  # tier elle degerlendiriliyor; kaynakta yok
        "prestige": g["pi"],
        "prestigeRank": rank("pi"),
        "attack": g["attack"],
        "health": g["health"],
        "critRate": round(g.get("critrate", 0) / 100, 2),
        "critDamage": round(g.get("critdamage", 0) / 100, 2),
        "armor": round(g.get("armor", 0) / 100, 2),
        "blockProficiency": round(g.get("block", 0) / 100, 2),
        "immunities": immunities,
        "counters": [f"{n} Immunity" for n in immunities],
        "abilities": ", ".join(names(g.get("ability", []), lk["ability"])),
        "tags": [t for t in names(g["tags"], lk["tags"], "tag") if t in known_tags],
        "synergies": [],
        "recommendedRelics": [lk["relics"][x]["name"] for x in relic_ids if x in lk["relics"]],
        "focusAttack": lk["focus"].get(g.get("focus_attack"), {}).get("name", ""),
        "focusDefense": lk["focus"].get(g.get("focus_defense"), {}).get("name", ""),
        "releaseDate": f"{int(day):02d}-{int(month):02d}-{year}",
        "strongMatchups": [lk["champs"][x]["image"] for x in g.get("xchampion", []) if x in lk["champs"]],
        "strongCounters": [],
        "reactsTo": names(g.get("react", []), lk["immune"]),
        "counterAbilities": names(g.get("xability", []), lk["ability"]),
        "attackRank": rank("attack"),
        "healthRank": rank("health"),
        "critRateRank": rank("critrate"),
        "critDamageRank": rank("critdamage"),
        "armorRank": rank("armor"),
        "blockProficiencyRank": rank("block"),
        "progressions": [],
    }


def cmd_apply():
    gg = load("champions.json")["data"]
    gg_by_image = {g["image"]: g for g in gg}
    ours_list = load_ours()
    ours = {c["id"]: c for c in ours_list}

    # 1) mevcut sampiyonlar: jenerik dolgu yerine gercek yetenek metinleri
    updated = 0
    for our_id, c in ours.items():
        image = ours_to_gg_image(our_id)
        if image in gg_by_image:
            update_details(our_id, c["name"], image)
            updated += 1
    print(f"yetenek metni guncellenen detay dosyasi: {updated}")

    # 2) bizde olmayan oynanabilir sampiyonlar
    lk = {
        "ability": table(load("abilities.json")["data"]), "immune": table(load("immunities.json")["data"]),
        "tags": table(load("tags.json")["data"]), "focus": table(load("focus.json")["data"]),
        "relics": table(load("relics.json")["data"]), "ranks": table(load("ranks.json")["data"]),
        "champs": table(gg),
    }
    known_tags = {t for c in ours_list for t in c.get("tags", [])}
    added = []
    for g in gg:
        our_id = gg_to_ours_id(g["image"])
        if our_id in ours or g["image"].startswith(NON_ROSTER_PREFIXES):
            continue
        champ = build_champion(g, lk, known_tags)
        ours_list.append(champ)
        update_details(champ["id"], champ["name"], g["image"])
        added.append(champ["id"])
    if added:
        with open(os.path.join(ASSETS, "champions_db.json"), "w", encoding="utf-8") as f:
            json.dump(ours_list, f, ensure_ascii=False, indent=4)
    print("eklenen yeni sampiyonlar:", added or "yok")


if __name__ == "__main__":
    commands = {"fetch": cmd_fetch, "report": cmd_report, "apply": cmd_apply}
    if len(sys.argv) != 2 or sys.argv[1] not in commands:
        sys.exit(__doc__)
    commands[sys.argv[1]]()
