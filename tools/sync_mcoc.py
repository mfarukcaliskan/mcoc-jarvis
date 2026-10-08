#!/usr/bin/env python3
"""mcoc.gg -> JARVIS veri senkronizasyonu.

Kullanim (proje kokunden):
  python tools/sync_mcoc.py fetch    # mcoc.gg JSON'larini tools/.cache/mcoc altina indirir ve dogrular
  python tools/sync_mcoc.py report   # bizim veriyle mcoc.gg arasindaki farklari yazdirir (dosya degistirmez)
  python tools/sync_mcoc.py apply    # yetenek metinlerini ve yeni sampiyonlari assets'e yazar
  python tools/sync_mcoc.py events        # events.json (AW taktikleri, roller, havuzlar, cikislar) uretir
  python tools/sync_mcoc.py relics        # relics.json (tam andac verisi) + relic_statcast.json uretir
  python tools/sync_mcoc.py prestige      # prestige.json (gercek 7 yildiz tablolari) uretir, sentetik progressions'i kaldirir
  python tools/sync_mcoc.py capabilities  # capabilities.json (kim hangi yetenek/bagisikliga sahip) uretir
Ardindan: python generate_manifest.py && python validate_quest_ids.py

Kaynak: https://mcoc.gg/json/*.json (sitenin kendi arayuzunun kullandigi statik dosyalar).
Istekler sirayla yapilir, aralarinda kisa bekleme vardir.
"""
import difflib
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
                "relics_abilities", "relics_attributes", "relics_ranks", "relics/statcast", "synergies", "roles", "content",
                "crystals", "custom"]

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
    # stat 0 ise site siralamada '-' gosterir (ranks.json yine de bir sayi tasir) -> 0
    RAW = {"critrate": "critrate", "critdamage": "critdamage", "armor": "armor", "block": "block"}
    rank = lambda key: (0 if key in RAW and not g.get(RAW[key]) else parse_rank(r[key])) if key in r else 0
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
        "critDamage": round(g.get("critdamage", 0) / 10, 1),  # ham 965 -> %96.5
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
        "strongMatchups": [gg_to_ours_id(lk["champs"][x]["image"]) for x in g.get("xchampion", []) if x in lk["champs"]],
        "strongCounters": lk.get("inv_x", {}).get(g["id"], []),
        "reactsTo": names(g.get("react", []), lk["immune"]),
        "counterAbilities": names(g.get("xability", []), lk["ability"]),
        "attackRank": rank("attack"),
        "healthRank": rank("health"),
        "critRateRank": rank("critrate"),
        "critDamageRank": rank("critdamage"),
        "armorRank": rank("armor"),
        "blockProficiencyRank": rank("block"),
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
    # "Strong Counters" = xchampion listesinin tersi (siteyle dogrulandi: iBom -> Dust, Peni Parker, ...)
    inv_x = {}
    for g in gg:
        for target in g.get("xchampion", []):
            inv_x.setdefault(target, []).append(gg_to_ours_id(g["image"]))
    lk["inv_x"] = inv_x
    known_tags = {t for c in ours_list for t in c.get("tags", [])}
    # 1b) mevcut sampiyonlarin kaynak kaynakli sayisal/liste alanlari mcoc.gg ile yenilenir (siralamalar, istatistikler,
    # bagisikliklar, yetenek adlari...). Kendi elle alanlarimiz (tier, synergies, strongCounters, isPlayable) korunur.
    # Liste alanlari yalnizca KUME olarak degistiyse yazilir (yalniz sira farki dosyayi oynatmasin).
    LIST_FIELDS = ("immunities", "counters", "reactsTo", "counterAbilities", "strongMatchups", "strongCounters", "tags", "recommendedRelics")
    SCALAR_FIELDS = ("prestige", "prestigeRank", "attack", "health", "critRate", "critDamage", "armor", "blockProficiency",
                     "attackRank", "healthRank", "critRateRank", "critDamageRank", "armorRank", "blockProficiencyRank",
                     "releaseDate", "mcocClass", "name")
    refreshed = {}
    for g in gg:
        oid = gg_to_ours_id(g["image"])
        c = ours.get(oid)
        if c is None:
            continue
        new = build_champion(g, lk, known_tags)
        for k in SCALAR_FIELDS:
            if k in new and c.get(k) != new[k] and not (new[k] in (0, "") and c.get(k) and not k.endswith("Rank")):
                refreshed[k] = refreshed.get(k, 0) + 1
                c[k] = new[k]
        for k in LIST_FIELDS:
            if k == "recommendedRelics" and not new[k]:
                continue
            if set(c.get(k, [])) != set(new[k]):
                refreshed[k] = refreshed.get(k, 0) + 1
                c[k] = new[k]
        old_ab = [x.strip() for x in c.get("abilities", "").split(",") if x.strip()]
        new_ab = [x.strip() for x in new["abilities"].split(",") if x.strip()]
        if set(old_ab) != set(new_ab):
            refreshed["abilities"] = refreshed.get("abilities", 0) + 1
            c["abilities"] = new["abilities"]
        for k in ("focusAttack", "focusDefense"):
            if new[k] and c.get(k) != new[k]:
                refreshed[k] = refreshed.get(k, 0) + 1
                c[k] = new[k]
    if refreshed:
        with open(os.path.join(ASSETS, "champions_db.json"), "w", encoding="utf-8") as f:
            json.dump(ours_list, f, ensure_ascii=False, indent=4)
    print("mevcut sampiyonlarda yenilenen alanlar:", refreshed or "yok")

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


SIG_LEVELS = list(range(0, 201, 20))


def cmd_prestige():
    """mcoc.gg gercek prestij tablolarindan prestige.json uretir ve champions_db.json'daki sentetik
    'progressions' alanini kaldirir. 7 yildizli sampiyonlar icin kademe basina 11 sig noktasi (0,20..200);
    ust duzey attack/health/pi degerleri R5 + sig 200 noktasina aittir (mcoc.gg tablolariyla dogrulandi)."""
    gg = load("champions.json")["data"]
    ours_list = load_ours()
    ours = {c["id"] for c in ours_list}
    champs, flags = {}, {}
    for g in gg:
        our_id = gg_to_ours_id(g["image"])
        if our_id not in ours:
            continue
        max_star = max(g.get("rarity", [0]))
        entries = []
        table_path = os.path.join(CACHE, "prestige", f"{g['image']}.json")
        champ_flags = []
        if os.path.isfile(table_path):
            rows = load(f"prestige/{g['image']}.json")["data"]
            for e in sorted(rows, key=lambda e: (e["rarity"], e["rank"])):
                vals = [int(v) if int(v) > 0 else None for v in e["values"]]
                entries.append({"star": e["rarity"], "rank": e["rank"], "prestige": vals, "attack": None, "health": None})
            r5 = [e for e in entries if e["star"] == 7 and e["rank"] == 5]
            if r5:  # ust duzey degerler R5 sig 200 noktasi
                r5[0]["attack"], r5[0]["health"] = g["attack"], g["health"]
                last = r5[0]["prestige"][-1]
                if last is not None and abs(last - g["pi"]) > 100:
                    champ_flags.append("pi_vs_table_r5")
            r4 = [e for e in entries if e["star"] == 7 and e["rank"] == 4]
            if r4 and g.get("pi_74") and r4[0]["prestige"][-1] is not None and abs(r4[0]["prestige"][-1] - g["pi_74"]) > 100:
                champ_flags.append("pi74_vs_table_r4")
            if any(None in e["prestige"] for e in entries):
                champ_flags.append("table_has_unknown_values")
        else:
            entries.append({"star": max_star, "rank": None, "prestige": None, "maxPrestige": g["pi"], "attack": g["attack"], "health": g["health"]})
        row = {"maxStar": max_star, "ascendable": bool(g.get("ascend")), "entries": entries}
        if champ_flags:
            row["flags"] = champ_flags
        champs[our_id] = row
    data = {
        "source": "https://mcoc.gg/json/prestige/*.json ve champions.json",
        "generatedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "sigLevels": SIG_LEVELS,
        "note": "prestige: sig 0,20,...,200 icin gercek degerler (mcoc.gg'nin kendi tablosu); null = kaynakta bilinmiyor ('???'). "
                "attack/health yalnizca R5 sig 200 noktasi icin kaynakta var (diger kademelerde null). Tablosu olmayan "
                "(en fazla 6 yildiz) sampiyonlarda tek giris vardir: maxPrestige/attack/health en yuksek kademe degeridir, kademe belirtilmemis. "
                "flags: mcoc.gg'nin kendi alanlari arasinda >100 fark (pi_vs_table_r5, pi74_vs_table_r4) ya da bilinmeyen deger.",
        "ascension": {"a1": 1.0799, "a2": 1.07995, "note": "Yalnizca ascendable=true sampiyonlar icin. mcoc.gg sitesinin kendi yuvarlama formulu (resmi veri degil): a1=round(v*1.0799/10)*10, a2=round(a1*1.07995/10)*10"},
        "champions": champs,
    }
    target = os.path.join(ASSETS, "prestige.json")
    if os.path.isfile(target):
        with open(target, encoding="utf-8") as f:
            prev = json.load(f)
        if {k: v for k, v in prev.items() if k != "generatedAt"} == {k: v for k, v in data.items() if k != "generatedAt"}:
            data["generatedAt"] = prev["generatedAt"]  # icerik ayniysa dosya (ve manifest) degismesin
    with open(target, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, separators=(",", ":"))
    # sentetik progressions alanini kaldir
    removed = 0
    for c in ours_list:
        if "progressions" in c:
            del c["progressions"]
            removed += 1
    with open(os.path.join(ASSETS, "champions_db.json"), "w", encoding="utf-8") as f:
        json.dump(ours_list, f, ensure_ascii=False, indent=4)
    nflag = sum(1 for r in champs.values() if r.get("flags"))
    print(f"prestige.json: {len(champs)} sampiyon ({sum(1 for r in champs.values() if r['maxStar']==7)} adet 7 yildizli), "
          f"isaretli {nflag}; champions_db'den kaldirilan progressions: {removed}")


def cmd_relics():
    """mcoc.gg'den tam andac verisi: relics.json'u yeniden uretir (mevcut sema korunur, yeni alanlar eklenir)
    ve relic_statcast.json yazar. Onerilen sampiyonlar sampiyonlarin relic/alt_relics alanlarindan turetilir."""
    gg = load("champions.json")["data"]
    ours_ids = {c["id"]: c for c in load_ours()}
    relics = load("relics.json")["data"]
    r_ab = {r["id"]: r for r in load("relics_abilities.json")["data"]}
    r_at = {r["id"]: r for r in load("relics_attributes.json")["data"]}
    ranks = {r["id"]: r for r in load("relics_ranks.json")["data"]}
    holders = {}
    for g in gg:
        oid = gg_to_ours_id(g["image"])
        if oid not in ours_ids:
            continue
        for rid in ([g["relic"]] if g.get("relic") else []) + g.get("alt_relics", []):
            holders.setdefault(rid, [])
            if oid not in holders[rid]:
                holders[rid].append(oid)

    def rank_of(rid):
        cell = ranks.get(rid, {}).get("pi")
        if not cell:
            return None
        digits = re.sub(r"\D", "", str(cell[0]))
        return int(digits) if digits else None

    PI_KEYS = [("pi_63", 6, 3), ("pi_64", 6, 4), ("pi_65", 6, 5), ("pi_71", 7, 1), ("pi_72", 7, 2)]
    out = []
    for r in relics:
        stars = max(r["rarity"])
        month, day, year = r["date"].split("/")
        innate = r_ab.get(r.get("innate"))
        abilities = [r_ab[a] for a in r.get("abilities", []) if a in r_ab]
        attrs = [r_at[a] for a in r.get("attributes", []) if a in r_at]
        ids = sorted(holders.get(r["id"], []), key=lambda i: ours_ids[i]["name"])
        prestige = [{"star": s, "rank": k, "value": r[key]} for key, s, k in PI_KEYS if key in r and r[key]]
        out.append({
            "id": f"relic{r['id']}",
            "name": r["name"],
            "relicClass": CLASS_NAME[r["class"]],
            "relicType": r["type"],
            "image": r["image"],
            "innateAbilities": [innate["name"]] if innate else [],
            "abilityRunes": [a["name"] for a in abilities],
            "attributeRunes": [a["name"] for a in attrs],
            "recommendedChampions": [ours_ids[i]["name"] for i in ids],
            "description": clean(re.sub(r"<h>(.*?)</h>", r"\1: ", r.get("striker", ""))).replace("\n", " ").strip(),
            "releaseDate": f"{int(day):02d}-{int(month):02d}-{year}",
            "rarity": r["rarity"],
            # --- yeni alanlar (mcoc.gg) ---
            "innate": {"name": innate["name"], "desc": clean(innate["desc"])} if innate else None,
            "abilities": [{"name": a["name"], "desc": clean(a["desc"])} for a in abilities],
            "attributes": [{"name": a["name"], "desc": clean(a["desc"])} for a in attrs],
            "recommendedChampionIds": ids,
            "maxLevel": f"{stars}★ " + ("R2/200" if stars == 7 else "R5/200"),
            "maxPrestige": r["pi"] if r.get("pi") else None,
            "prestigeRank": rank_of(r["id"]),
            "prestigeByLevel": prestige,
        })
    with open(os.path.join(ASSETS, "relics.json"), "w", encoding="utf-8") as f:
        json.dump(out, f, ensure_ascii=False, indent=1)
    statcast = load("relics/statcast.json")["data"]
    table = [{"star": e["rarity"], "rank": e["rank"], "prestige": [v if v else None for v in e["values"]]} for e in statcast]
    with open(os.path.join(ASSETS, "relic_statcast.json"), "w", encoding="utf-8") as f:
        json.dump({"source": "https://mcoc.gg/json/relics/statcast.json", "sigLevels": SIG_LEVELS,
                   "note": "Tum Statcast andaclari icin ortak prestij tablosu (seviye 0,20..200); null = kaynakta bilinmiyor.",
                   "table": table}, f, ensure_ascii=False, separators=(",", ":"))
    print(f"relics.json: {len(out)} andac; onerilen sampiyonu olan {sum(1 for o in out if o['recommendedChampionIds'])}; "
          f"yetenek aciklamasi olan {sum(1 for o in out if o['abilities'])}; relic_statcast: {len(table)} kademe")


def source_refs(g, ids_key, map_key, names):
    """<ids_key>/<map_key> (1-tabanli bolum/satir) -> details/*.json abilitySections'a gore 0-tabanli [bolum, satir] ciftleri."""
    raw = json.load(open(os.path.join(CACHE, "champions", f"{g['image']}.json"), encoding="utf-8"))["abilities"]
    sec_idx, line_idx, k = {}, {}, 0
    for si, a in enumerate(raw, 1):
        kept = [ci for ci, c in enumerate(a.get("content", []), 1) if clean(c)]
        if kept:
            sec_idx[si] = k
            line_idx[si] = {ci: li for li, ci in enumerate(kept)}
            k += 1
    refs = {}
    for aid, mp in zip(g.get(ids_key, []), g.get(map_key) or []):
        name = names.get(str(aid))
        if not name:
            continue
        pairs = refs.setdefault(name, [])
        for m in mp:
            for c in m["c"]:
                if m["t"] in sec_idx and c in line_idx[m["t"]]:
                    pr = [sec_idx[m["t"]], line_idx[m["t"]][c]]
                    if pr not in pairs:
                        pairs.append(pr)
    return {n: p for n, p in refs.items() if p}


def synergy_refs(g, ids_key, map_key, names):
    """synergy_<x> (yetenek/bagisiklik/counter kimlikleri) + synergy_<x>_map (sinerji kimlikleri) -> {ad: [sinerji id]}."""
    out = {}
    for aid, sid in zip(g.get(ids_key, []), g.get(map_key) or []):
        name = names.get(str(aid))
        if name and sid not in out.setdefault(name, []):
            out[name].append(sid)
    return out


def cmd_extras():
    """mcoc.gg'den sampiyon basina ek veri (champion_extra.json): takma ad, yildiz araligi, ilk cikis, ascend,
    ham direnc/delme istatistikleri, vurus deseni, Raid rolu, gorunur etiketler, alternatif relic'ler."""
    gg = load("champions.json")["data"]
    ours = {c["id"]: c for c in load_ours()}
    tags = {str(t["id"]): t for t in load("tags.json")["data"]}
    roles = {r["id"]: r for r in load("roles.json")["data"]}
    relics = {r["id"]: r["name"] for r in load("relics.json")["data"]}
    ab_names = {str(a["id"]): a["name"] for a in load("abilities.json")["data"]}
    im_names = {str(a["id"]): a["name"] for a in load("immunities.json")["data"]}
    out = {}
    linked = 0
    for g in gg:
        oid = gg_to_ours_id(g["image"])
        if oid not in ours:
            continue
        hits = ["+".join(h) for h in g.get("hits", [])]
        dpath = os.path.join(ASSETS, "details", f"{oid}.json")
        if os.path.isfile(dpath):
            d = json.load(open(dpath, encoding="utf-8"))
            fields = {
                "abilityRefs": source_refs(g, "ability", "ability_map", ab_names),
                "immunityRefs": source_refs(g, "immune", "immune_map", im_names),
                "counterRefs": source_refs(g, "xability", "xability_map", ab_names),
                "abilitySynergies": synergy_refs(g, "synergy_ability", "synergy_ability_map", ab_names),
                "immunitySynergies": synergy_refs(g, "synergy_immune", "synergy_immune_map", im_names),
                "counterSynergies": synergy_refs(g, "synergy_xability", "synergy_xability_map", ab_names),
            }
            refs = fields["abilityRefs"]
            if any(d.get(k) != v for k, v in fields.items()):
                d.update(fields)
                with open(dpath, "w", encoding="utf-8") as f:
                    json.dump(d, f, ensure_ascii=False, indent=4)
            linked += 1 if refs else 0
        role = roles.get(g.get("role"))
        out[oid] = {
            "alias": g.get("alias"),
            "stars": g.get("rarity", []),
            "firstAppearance": g.get("first_appearance"),
            "ascendable": bool(g.get("ascend")),
            "rawStats": {k: g[k] for k in ("armorpen", "blockpen", "critresist", "physicalresist", "energyresist") if k in g},
            "hits": hits,
            "raidBoostRole": role["boost"] if role else None,
            "tags": [tags[str(t)]["tag"] for t in g.get("tags", []) if str(t) in tags and not tags[str(t)].get("hidden")],
            "relic": relics.get(g.get("relic")),
            "altRelics": [relics[r] for r in g.get("alt_relics", []) if r in relics],
        }
    path = os.path.join(ASSETS, "champion_extra.json")
    doc = {"source": "mcoc.gg champions.json (yetkili)", "note": "hits kodlari mcoc.gg'den ham: CP, PE, CE, PP (anlami kaynakta aciklanmiyor, yorumlanmadi). rawStats ham sayilardir, yuzdeye cevrilmemistir.", "champions": out}
    new = json.dumps(doc, ensure_ascii=False, indent=1, sort_keys=True)
    if os.path.isfile(path) and open(path, encoding="utf-8").read() == new:
        print("champion_extra.json degismedi")
        return
    open(path, "w", encoding="utf-8").write(new)
    print(f"champion_extra.json: {len(out)} sampiyon, abilityRefs: {linked}")


def cmd_synergies():
    """mcoc.gg sinerjileri (synergies.json), iki tablo:
    - texts: sinerji kimligi -> {ad, benzersiz mi, etki satirlari} (her metin YALNIZ BIR KEZ saklanir)
    - champions: sampiyon -> [{id, partners, coPartners?, reverse?}] (yalniz kimlik + ortaklar)
    Sinerji tek tarafin listesinde olabilir; site 'SYNERGIES' listesi ters yonu de gosterir, bu yuzden ortak listesinde adi
    gecen sampiyona da ters yonlu kayit (reverse=true, partners=[sinerjiyi listeleyen sampiyon]) eklenir."""
    gg = load("champions.json")["data"]
    ours = {c["id"] for c in load_ours()}
    syn = {str(x["id"]): x for x in load("synergies.json")["data"]}
    gid = {g["id"]: gg_to_ours_id(g["image"]) for g in gg}
    texts, out = {}, {}

    def use(sid):
        x = syn.get(str(sid))
        if x and str(sid) not in texts:
            # kaynak isaretleme bozuk olabiliyor (kapanmayan <g>): her <g> yeni bir katilimci satiridir
            segs = [clean(t.replace("</g>", "")) for t in x["desc"].split("<g>")]
            texts[str(sid)] = {"name": x["name"], "unique": bool(x.get("unique")), "effects": [t for t in segs if t]}
        return str(sid) in texts

    roster = [g for g in gg if gid[g["id"]] in ours]
    for g in roster:  # 1) sampiyonun kendi listesi
        rows = []
        for partners, sid in zip(g.get("synergy", []), g.get("synergy_map", [])):
            if use(sid):
                rows.append({"id": int(sid), "partners": [gid[p] for p in partners if gid.get(p) in ours]})
        if rows:
            out[gid[g["id"]]] = rows
    for g in roster:  # 2) ters yon
        oid = gid[g["id"]]
        for partners, sid in zip(g.get("synergy", []), g.get("synergy_map", [])):
            if str(sid) not in texts:
                continue
            members = [gid[p] for p in partners if gid.get(p) in ours]
            for pid in members:
                rows = out.setdefault(pid, [])
                same = [r for r in rows if r["id"] == int(sid)]
                if same:  # ayni kimlik baska ortak grubuyla da gecerli: sahibi mevcut kayda ekle
                    for r in same:
                        if oid not in r["partners"]:
                            r["partners"].append(oid)
                    continue
                rows.append({"id": int(sid), "partners": [oid], "coPartners": [m for m in members if m != pid], "reverse": True})
    for g in roster:  # 3) yetenek/bagisiklik/counter satirlarinin dayandigi sinerjiler (kendi listesinde olmayabilir)
        for key in ("synergy_ability_map", "synergy_immune_map", "synergy_xability_map"):
            for sid in g.get(key) or []:
                use(sid)
    path = os.path.join(ASSETS, "synergies.json")
    doc = {"source": "mcoc.gg champions.json + synergies.json (yetkili)",
           "note": "texts: sinerji metinleri (Ingilizce, mcoc.gg'den oldugu gibi). champions: sampiyon basina sinerji kimlikleri ve ortaklar; "
                   "partners = sinerjiyi etkinlestiren diger sampiyonlar (yalniz bizim kadrodakiler).",
           "texts": texts, "champions": out}
    new = json.dumps(doc, ensure_ascii=False, indent=1, sort_keys=True)
    if os.path.isfile(path) and open(path, encoding="utf-8").read() == new:
        print("synergies.json degismedi")
        return
    open(path, "w", encoding="utf-8").write(new)
    print(f"synergies.json: {len(out)} sampiyon, {sum(len(v) for v in out.values())} kayit, {len(texts)} metin")


def _norm_name(s):
    s = re.sub(r"^(AW:\s*|Defense:\s*|Attack:\s*)", "", s.strip(), flags=re.I)
    return re.sub(r"[^a-z0-9]", "", s.lower().replace("wraith", "wrath"))


def cmd_events():
    """mcoc.gg'den oyun ici etkinlik/uyelik verisi (events.json): AW taktikleri, Raid/AQ rolleri, havuz cikislari,
    kristal havuzlari, saga/yil/evren gruplari. Uyelikler sampiyon 'tags' ve 'pool' alanlarindan, aciklamalar content.json'dan."""
    gg = load("champions.json")["data"]
    ours = {c["id"]: c for c in load_ours()}
    tags = {str(t["id"]): t for t in load("tags.json")["data"]}
    content = {e["id"]: e for e in load("content.json")["data"]}
    custom = {e["id"]: e for e in load("custom.json")["data"]}
    crystals = load("crystals.json")["data"]
    by_gid = {}
    for g in gg:
        oid = gg_to_ours_id(g["image"])
        if oid in ours:
            by_gid[g["id"]] = oid
    members = {}
    for g in gg:
        oid = by_gid.get(g["id"])
        if not oid:
            continue
        for t in g["tags"]:
            members.setdefault(str(t), []).append(oid)

    def ids_of(tag_id):
        return sorted(members.get(tag_id, []), key=lambda i: ours[i]["name"])

    def tag_row(tid):
        t = tags[tid]
        return {"tagId": tid, "name": t["tag"], "hidden": bool(t.get("hidden")), "champions": ids_of(tid)}

    # --- AW taktikleri: content (rol + aciklama) ile etiket adini eslestir
    tactic_content = {}
    for cid, e in content.items():
        titles = [clean(t) for t in e.get("titles", [])]
        if len(titles) >= 2 and all(re.match(r"^(Defense|Attack):", t) for t in titles[:2]):
            for i, t in enumerate(titles[:2]):
                role = "defense" if t.lower().startswith("defense") else "attack"
                desc = clean(e["descriptions"][i]) if i < len(e.get("descriptions", [])) else ""
                tactic_content[_norm_name(t)] = {"role": role, "description": desc, "contentId": cid, "title": t}
    aw = []
    for tid, t in tags.items():
        if re.fullmatch(r"AW\d+", tid) and int(tid[2:]) >= 1:
            row = tag_row(tid)
            key = _norm_name(t["tag"])
            if key not in tactic_content:  # kaynaktaki yazim hatalari (orn. "House of Mirros")
                close = difflib.get_close_matches(key, list(tactic_content), n=1, cutoff=0.85)
                key = close[0] if close else key
            info = tactic_content.get(key)
            row["role"] = info["role"] if info else None
            row["descriptionEn"] = info["description"] if info else None
            row["contentTitle"] = info["title"] if info else None
            aw.append(row)
    aw.sort(key=lambda r: int(r["tagId"][2:]))

    # --- Raid rolleri ve amplifikatorler
    raid_boost_text = {}
    if 3 in content:
        e = content[3]
        for i, t in enumerate(e["titles"]):
            raid_boost_text[_norm_name(t.split(":", 1)[-1])] = {"title": clean(t), "descriptionEn": clean(e["descriptions"][i])}
    raids = []
    for rid, rname in (("AQ1", "Assault"), ("AQ2", "Tactician"), ("AQ3", "Vanguard")):
        boosts = []
        for tid, t in tags.items():
            if tid.startswith(rid + "-"):
                row = tag_row(tid)
                info = raid_boost_text.get(_norm_name(t["tag"]))
                row["descriptionEn"] = info["descriptionEn"] if info else None
                boosts.append(row)
        raids.append({**tag_row(rid), "role": rname, "boosts": boosts})

    # --- Havuz cikislari (Titan): content 1 basliklari ile custom exit_N sirali eslesir (sitenin pool-titan gorunumu)
    exits = []
    if 1 in content:
        for i, title in enumerate(content[1]["titles"]):
            key = f"exit_{i + 1}"
            champs = [by_gid[c] for c in custom.get(key, {}).get("champions", []) if c in by_gid]
            exits.append({"title": clean(title), "champions": sorted(champs, key=lambda i2: ours[i2]["name"])})

    # --- Kristal havuzlari
    pools = []
    for c in crystals:
        pid = int(c["id"])
        champs = [by_gid[g["id"]] for g in gg if pid in g.get("pool", []) and g["id"] in by_gid]
        pools.append({"poolId": pid, "name": c["name"], "image": c["image"], "champions": sorted(champs, key=lambda i: ours[i]["name"])})

    # --- Yukselis (ascension) havuzlari: sitenin 'ascend' gorunumundeki filtre kurallari
    asc = {"base": [], "superDuper": [], "featured": [], "eventOrOffer": []}
    for g in gg:
        oid = by_gid.get(g["id"])
        if not oid or not g.get("ascend"):
            continue
        p = set(g.get("pool", []))
        if "AVEO" in [str(t) for t in g["tags"]]:
            asc["eventOrOffer"].append(oid)
        elif 13 in p:
            asc["base"].append(oid)
        elif 14 in p:
            asc["superDuper"].append(oid)
        else:
            asc["featured"].append(oid)
    titles51 = [clean(t) for t in content.get(51, {}).get("titles", [])]
    descs51 = [clean(t) for t in content.get(51, {}).get("descriptions", [])]
    ascension = []
    for i, key in enumerate(["base", "superDuper", "featured", "eventOrOffer"]):  # content 51 baslik sirasi
        ascension.append({"title": titles51[i] if i < len(titles51) else key,
                          "descriptionEn": descs51[i] if i < len(descs51) else "", "key": key,
                          "champions": sorted(asc[key], key=lambda x: ours[x]["name"])})

    # --- Battlegrounds meta (mcoc.gg'nin guncel kaydi)
    bg = None
    if 2 in content:
        e = content[2]
        bg = {"titles": [clean(t) for t in e["titles"]], "descriptionsEn": [clean(t) for t in e["descriptions"]],
              "attackers": [by_gid[c] for c in custom.get("bga", {}).get("champions", []) if c in by_gid],
              "defenders": [by_gid[c] for c in custom.get("bgd", {}).get("champions", []) if c in by_gid]}

    def group(test):
        return [tag_row(tid) for tid in tags if test(tid)]

    data = {
        "source": "https://mcoc.gg/json (champions.json tags/pool, tags.json, content.json, custom.json, crystals.json)",
        "generatedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "note": "Uyelikler mcoc.gg'nin sampiyon etiketlerinden (guncel). role/descriptionEn content.json'dan; role=null: o taktik icin mcoc.gg'de aciklama kaydi yok. "
                "ascension: sitenin 'ascend' gorunumunun filtre kurallariyla turetildi. exits: sitenin 'pool-titan' gorunumundeki sirayla (basliklar content.json). "
                "timelineTags ('Single/Double/Triple - Day N') ve squadBuilder etiketlerinin anlami sitede aciklanmiyor, ham olarak saklandi.",
        "battlegroundsMeta": bg,
        "awTactics": aw,
        "raids": raids,
        "aqRamp": tag_row("AQ0"),
        "pools": pools,
        "exits": exits,
        "ascension": ascension,
        "releaseYears": group(lambda t: re.fullmatch(r"R\d+", t) is not None),
        "affiliations": group(lambda t: re.fullmatch(r"O\d+", t) is not None),
        "traits": group(lambda t: re.fullmatch(r"A\d+", t) is not None),
        "roles": group(lambda t: re.fullmatch(r"S\d", t) is not None),
        "challenges": group(lambda t: t.startswith("CC") or re.fullmatch(r"C[5-8]", t) is not None),
        "timelineTags": group(lambda t: re.fullmatch(r"C[123][123]", t) is not None),
        "squadBuilder": group(lambda t: t.startswith("TB")),
        "other": group(lambda t: t in ("SC1", "7L", "AVEO")),
        "customGroups": {k: sorted((by_gid[c] for c in v.get("champions", []) if c in by_gid), key=lambda i: ours[i]["name"])
                         for k, v in custom.items() if k in ("pi", "couples", "box")},
    }
    target = os.path.join(ASSETS, "events.json")
    if os.path.isfile(target):
        with open(target, encoding="utf-8") as f:
            prev = json.load(f)
        if {k: v for k, v in prev.items() if k != "generatedAt"} == {k: v for k, v in data.items() if k != "generatedAt"}:
            print("events.json degismedi")
            return
    with open(target, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, separators=(",", ":"))
    print(f"events.json: {len(aw)} AW taktigi ({sum(1 for a in aw if a['role'])} rol/aciklamali), {len(pools)} havuz, "
          f"{len(exits)} cikis grubu, {sum(len(v['champions']) for v in ascension)} yukselis, {len(data['releaseYears'])} yil grubu")


if __name__ == "__main__":
    commands = {"fetch": cmd_fetch, "report": cmd_report, "apply": cmd_apply, "capabilities": cmd_capabilities, "prestige": cmd_prestige, "relics": cmd_relics, "events": cmd_events, "extras": cmd_extras, "synergies": cmd_synergies}
    if len(sys.argv) != 2 or sys.argv[1] not in commands:
        sys.exit(__doc__)
    commands[sys.argv[1]]()
