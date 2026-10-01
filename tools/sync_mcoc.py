#!/usr/bin/env python3
"""mcoc.gg -> JARVIS veri senkronizasyonu.

Kullanim (proje kokunden):
  python tools/sync_mcoc.py fetch    # mcoc.gg JSON'larini tools/.cache/mcoc altina indirir ve dogrular
  python tools/sync_mcoc.py report   # bizim veriyle mcoc.gg arasindaki farklari yazdirir (dosya degistirmez)
  python tools/sync_mcoc.py apply    # yetenek metinlerini ve yeni sampiyonlari assets'e yazar
  python tools/sync_mcoc.py capabilities  # capabilities.json (kim hangi yetenek/bagisikliga sahip) uretir
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
from datetime import datetime, timezone
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


def ensure_portrait(image):
    """Yeni sampiyonun portresini uygulamanin drawable klasorune indirir (yoksa)."""
    path = os.path.join(ROOT, "app", "src", "main", "res", "drawable", f"{image}.webp")
    if os.path.isfile(path):
        return
    req = urllib.request.Request(f"https://mcoc.gg/images/portraits/{image}.webp", headers=UA)
    with urllib.request.urlopen(req, timeout=30) as r:
        data = r.read()
    with open(path, "wb") as f:
        f.write(data)


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
        ensure_portrait(g["image"])
        added.append(champ["id"])
    if added:
        with open(os.path.join(ASSETS, "champions_db.json"), "w", encoding="utf-8") as f:
            json.dump(ours_list, f, ensure_ascii=False, indent=4)
    print("eklenen yeni sampiyonlar:", added or "yok")


IMM_KIND = {"": "full", "CONDITIONAL": "conditional", "POTENCY": "potency", "DURATION": "duration", "PURIFY": "purify"}


def _imm_kind(note):
    n = (note or "").upper()
    for key, kind in (("CONDITIONAL", "conditional"), ("POTENCY", "potency"), ("DURATION", "duration"), ("PURIFY", "purify")):
        if key in n:
            return kind
    return "full" if not n else n.lower()


def cmd_capabilities():
    """mcoc.gg verisinden 'hangi sampiyon hangi yetenege/bagisikliga sahip' dizinini uretir
    (capabilities.json). Her iddia icin kaynak metin bolumu (via) ve sinerji bilgisi tutulur."""
    gg = load("champions.json")["data"]
    ours = {c["id"] for c in load_ours()}
    ab = table(load("abilities.json")["data"])
    im = table(load("immunities.json")["data"])
    syn = {str(s["id"]): s for s in load("synergies.json")["data"]}

    def titles(image):
        path = os.path.join(CACHE, "champions", f"{image}.json")
        with open(path, encoding="utf-8") as f:
            return [clean(a["title"]) for a in json.load(f)["abilities"]]

    abilities, immunities, counters, reacts = {}, {}, {}, {}
    used_syn = {}
    dropped = []
    for g in gg:
        our_id = gg_to_ours_id(g["image"])
        if our_id not in ours:
            dropped.append(g["image"])
            continue
        tt = titles(g["image"])

        def via(maps, i):
            out = []
            if maps and i < len(maps) and maps[i]:
                for m in maps[i]:
                    if isinstance(m, dict) and 1 <= m.get("t", 0) <= len(tt):
                        out.append(tt[m["t"] - 1])
            return list(dict.fromkeys(out))

        def add(bucket, record, key, name, extra):
            e = bucket.setdefault(key, {"name": name, "champions": {}})
            c = e["champions"].setdefault(our_id, {"id": our_id})
            for k, v in extra.items():
                if k == "via":
                    c["via"] = list(dict.fromkeys(c.get("via", []) + v))
                elif k == "synergy":
                    c["synergy"] = c.get("synergy", True) and v
                elif k == "synergyIds":
                    c["synergyIds"] = sorted(set(c.get("synergyIds", [])) | set(v))
                elif k == "kinds":
                    c["kinds"] = sorted(set(c.get("kinds", [])) | set(v))
            return e

        def feed(bucket, table_, ids, maps, syn_ids, syn_maps, kind_fn=None):
            for i, aid in enumerate(g.get(ids, [])):
                rec = table_.get(aid)
                if not rec or rec.get("hidden"):
                    continue
                extra = {"via": via(g.get(maps), i), "synergy": False}
                if kind_fn:
                    extra["kinds"] = [kind_fn(rec)]
                add(bucket, rec, rec["name"], rec["name"], extra)
            for i, aid in enumerate(g.get(syn_ids, [])):
                rec = table_.get(aid)
                if not rec or rec.get("hidden"):
                    continue
                sm = g.get(syn_maps, [])
                sid = str(sm[i]) if i < len(sm) and sm[i] else None
                extra = {"synergy": True}
                if sid:
                    extra["synergyIds"] = [int(sid)] if sid.isdigit() else []
                    if sid in syn:
                        used_syn[sid] = {"name": clean(syn[sid]["name"]), "desc": clean(syn[sid]["desc"])}
                if kind_fn:
                    extra["kinds"] = [kind_fn(rec)]
                add(bucket, rec, rec["name"], rec["name"], extra)

        feed(abilities, ab, "ability", "ability_map", "synergy_ability", "synergy_ability_map")
        feed(immunities, im, "immune", "immune_map", "synergy_immune", "synergy_immune_map", lambda r: _imm_kind(r.get("NOTE")))
        feed(counters, ab, "xability", "xability_map", "synergy_xability", "synergy_xability_map")
        feed(reacts, im, "react", "react_map", None, None, lambda r: _imm_kind(r.get("NOTE")))

    gloss = {a["name"]: clean(a.get("glossary", "")) for a in load("abilities.json")["data"]}

    def finish(bucket, with_gloss=False):
        out = []
        for key in sorted(bucket):
            e = bucket[key]
            champs = sorted(e["champions"].values(), key=lambda c: c["id"])
            for c in champs:  # yalnizca imza yetenegi (awakened) ile kazanilan ozellik
                v = c.get("via")
                if v and all(t.startswith("Signature Ability") for t in v):
                    c["signatureOnly"] = True
            row = {"name": e["name"], "champions": champs}
            if with_gloss and gloss.get(e["name"]):
                row["glossary"] = gloss[e["name"]]
            out.append(row)
        return out

    data = {
        "source": "https://mcoc.gg/json (champions.json, abilities.json, immunities.json, synergies.json, champions/*.json)",
        "generatedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "note": "via: iddianin dayandigi sampiyon metin bolumu (mcoc.gg ability_map). synergy=true: yalnizca bir sinerji ile kazanilir. "
                "signatureOnly=true: yalnizca imza yetenegi (awakening) ile. kinds (bagisiklik/tepki): full tam bagisiklik, conditional kosullu, potency guc azaltma, duration sure kisaltma, purify arindirma.",
        "abilities": finish(abilities, True),
        "immunities": finish(immunities),
        "counters": finish(counters, True),
        "reacts": finish(reacts),
        "synergies": {k: used_syn[k] for k in sorted(used_syn, key=int)},
        "droppedNotInRoster": sorted(dropped),
    }
    target = os.path.join(ASSETS, "capabilities.json")
    if os.path.isfile(target):
        with open(target, encoding="utf-8") as f:
            prev = json.load(f)
        if {k: v for k, v in prev.items() if k != "generatedAt"} == {k: v for k, v in data.items() if k != "generatedAt"}:
            print("capabilities.json degismedi")
            return
    with open(target, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, separators=(",", ":"))
    print(f"capabilities.json: {len(data['abilities'])} yetenek, {len(data['immunities'])} bagisiklik, "
          f"{len(data['counters'])} karsi-yetenek, {len(data['reacts'])} tepki; roster disi atlanan {len(dropped)}")


if __name__ == "__main__":
    commands = {"fetch": cmd_fetch, "report": cmd_report, "apply": cmd_apply, "capabilities": cmd_capabilities}
    if len(sys.argv) != 2 or sys.argv[1] not in commands:
        sys.exit(__doc__)
    commands[sys.argv[1]]()
