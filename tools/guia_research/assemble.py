import collections
import json
import re
import sys
import warnings

warnings.filterwarnings("ignore")
sys.stdout.reconfigure(encoding="utf-8")
from cells import find_cells_labeled

J = "C:/Users/muhammed faruk/Desktop/JARVIS ASISTAN/JARVIS/app/src/main/assets/"
DB = {c["id"] for c in json.load(open(J + "champions_db.json", encoding="utf-8"))}
G = json.load(open("grids_all.json", encoding="utf-8"))
D = json.load(open("dump.json", encoding="utf-8"))
TIT = json.load(open("titles.json", encoding="utf-8"))
L = json.load(open("layout.json", encoding="utf-8"))
META = json.load(open("review1.json"))
STRIPS = json.load(open("strips.json"))

# Gozle dogrulanmis duzeltmeler: review1 sirasi -> id (None = sampiyon degil / bos hucre)
REVIEW = {
    0: "madelynepryor", 1: "blackpanthercivilwar", 2: "jabaripanther", 3: "howardtheduck", 4: "visionaarkus",
    5: "mrknight", 6: "samwilson", 7: "shehulk", 8: "whitetiger", 9: None, 10: "deathlessvision", 11: None,
    12: "spiderman", 13: "deathlessvision", 14: "deathlesskinggroot", 15: "superiorironman", 16: "deadpool",
    17: None, 18: "theleader", 19: "antivenom", 28: "magnetomarvelnow", 31: "omegasentinel",
    32: "spidermansupreme", 33: "bwdo", 34: "symbiotesupreme", 35: "deathlesskinggroot", 36: "whitetiger",
    37: "deathlessguillotine", 38: "scarletwitch", 39: "blackwidowclairevoyant", 40: "captainmarvelmcu",
    41: "doctoroctopus", 42: "ebonymaw", 43: "visionaarkus", 44: "bwdo", 45: "colossus", 46: "symbiotesupreme",
    47: "thorjanefoster", 48: "silversurfer", 49: "blackpanthercivilwar", 50: "jabaripanther", 51: "magnetomarvelnow",
}
OVR = {}
for n, fn, k in META:
    if n in REVIEW:
        OVR[(fn, k)] = REVIEW[n]
# Bu iki gorsel serit biciminde: ayri islendi
STRIP_FILES = {"globais-aw_wrath-aw__002.png", "globais-aw_poder-de-proeza__002.png"}
STRIP_FIX = {  # strips.json sirasi -> id
    8: "shehulk", 13: "spiderman2099", 14: "toad", 15: "silk", 24: "wiccan", 25: "elsabloodstone",
}
STRIP_DROP = {12, 22, 26, 41, 42, 43, 44, 45, 46, 47, 48, 49, 60, 61, 62, 63}
FIXED = {
    "globais-aw_armadura-insuper_vel__001.jpg": ("defenders", "Armadura Insuperável (Defesa)", [
        "moleman", "sasquatch", "cosmicghostrider", "kingpin", "annihilus", "mangog", "hulkragnarok", "redskull",
        "warlock", "venom", "colossus", "ultron", "hulkbuster", "iceman", "superskrull", "angela", "rhino",
        "doctoroctopus", "thechampion", "venomtheduck", "blackpanthercivilwar", "heimdall", "manthing",
        "juggernaut", "civilwarrior", "nimrod"], 0),
    "globais-aw_disp-err-s_o-m_stica__001.png": ("defenders", "Disp-ERR-são Mística (Defesa)", [
        "ebonymaw", "silversurfer", "sorcerersupreme", "dragonman", "mangog", "sasquatch", "tigra", "manthing",
        "mysterio", "guillotine2099", "symbiotesupreme", "americachavez", "ghost", "invisiblewoman", "thehood",
        "spidermanmilesmorales", "modok", "superskrull"], 7),
    "globais-aw_disp-err-s_o-m_stica__010.png": ("attackers", "Desafio (Ataque)", [
        "abomination", "agentvenom", "blackpanther", "captainamericaiw", "falcon", "gwenpool", "hawkeye", "hitmonkey",
        "hulk", "hulkragnarok", "abominationimmortal", "hulkimmortal", "invisiblewoman", "joefixit", "karnak",
        "killmonger", "masacre", "spidermanmilesmorales", "modok", "misterfantastic", "ronin", "shehulk",
        "spidergwen", "spidermanstealthsuit", "yellowjacket"], 0),
}

LABELED = {  # gorselin kendi etiketlerinden aktarildi (etiketler okunaklı)
    "globais-aw_dinosaur-aw": ("attackers", "Meteor Attack Tactic - Season 64 & 65", "64&65", [
        "pavitr", "doctorstrange", "absorbingman", "kushala", "thorjanefoster", "mojo", "doctorvoodoo", "dracula",
        "sorcerersupreme", "spidermansupreme", "kittypryde", "toad", "wolverinex23", "namor", "nightcrawler",
        "storm", "stormpyramidx", "sauron", "emmafrost", "magnetomarvelnow"]),
    "globais-aw_resist_ncia-planet_ria": ("defenders", "Tática de Defesa: Resistência Planetária", None, [
        "korg", "nickfury", "crossbones", "moleman", "valkyrie", "storm", "magnetomarvelnow", "apocalypse", "cable",
        "punisher2099", "warmachine", "howardtheduck", "peniparker", "juggernaut", "scarletwitch", "ikaris", "sersi",
        "gorr", "terrax", "odin", "hulkling", "venom", "visionaarkus", "annihilus", "nova"]),
}

PAGES = {  # slug -> (id, ad, mcoc.gg capraz kontrol sonucu)
    "globais-aw_stone-global-aw": ("stone_water", "Stone (Savunma) / Water (Saldırı)", "uyumlu"),
    "globais-aw_wrath-aw": ("wrath_forbearance", "Wrath (Savunma) / Forbearance (Saldırı)", "uyumlu"),
    "globais-aw_dinosaur-aw": ("dinosaur_meteor", "Dinosaur (Savunma) / Meteor (Saldırı)", "uyumlu (mcoc.gg derece değerlerini 70/50/30% verir)"),
    "globais-aw_frightful-aw": ("frightful_fantastic", "Frightful (Savunma) / Fantastic (Saldırı)", "uyumlu"),
    "globais-aw_genesis-coda": ("genesis_coda", "Genesis (Savunma) / Coda (Saldırı)", "uyumlu"),
    "globais-aw_desviar-aw": ("house_of_mirrors_clarity", "House of Mirrors / Desviar (Savunma) / Clarity-Foice (Saldırı)", "uyumlu"),
    "globais-aw_provocador": ("provocateur_secutor", "Provocateur (Savunma) / Secutor (Saldırı)", "uyumlu"),
    "globais-aw_ladr_o-m_gico": ("magic_thief_xmagica", "Magic Thief (Savunma) / X-Magica (Saldırı)",
                                 "ÇELİŞKİ: GuiaMTC her 10 sn / en fazla 5 yığın, mcoc.gg her 20 sn / en fazla 3 yığın diyor; farklı derece/sürüm olabilir"),
    "ricochet-global-aw": ("ricochet_stabilize", "Ricochet (Savunma) / Stabilize (Saldırı)",
                           "Savunma tarafı uyumlu. Saldırı tarafı ÇELİŞKİ: GuiaMTC %30 Direnç pasifi (olumsuz etkilerin süresi -%30, 3 yığın), mcoc.gg %20 Dayanıklılık (yalnızca Dengesizlik süresi -%10 her yığın)"),
    "globais-aw_esmagar-2-0": ("crush_2_0", "Esmagar 2.0 (Savunma) / Robustez (Saldırı)", "mcoc.gg'de yok, doğrulanamadı"),
    "globais-aw_canalizador": ("conduit", "Canalizador (Savunma) / Desafio (Saldırı)", "mcoc.gg'de yok, doğrulanamadı"),
    "globais-aw_poder-de-proeza": ("prowess_power", "Poder de Proeza (Savunma) / Queima de Armadura (Saldırı)", "mcoc.gg'de yok, doğrulanamadı"),
    "globais-aw_resist_ncia-planet_ria": ("planetary_resistance", "Resistência Planetária (Savunma) / Subjugados (Saldırı)", "mcoc.gg'de yok, doğrulanamadı"),
    "globais-aw_terreno-inst_vel": ("unstable_terrain", "Terreno Instável (Savunma) / Debilitação de Detox (Saldırı)", "mcoc.gg'de yok, doğrulanamadı"),
    "globais-aw_armadura-insuper_vel": ("unstoppable_armor", "Armadura Insuperável (Savunma)", "mcoc.gg'de yok, doğrulanamadı"),
    "globais-aw_disp-err-s_o-m_stica": ("mystic_dispersion", "Disp-ERR-são Mística (Savunma) / Desafio (Saldırı)", "mcoc.gg'de yok, doğrulanamadı"),
}

cnt = collections.Counter()
for s, seq in L.items():
    for t in set(v for k, v in seq if k == "T"):
        cnt[t] += 1
EMOJI = re.compile("[\U0001F000-\U0001FFFF\u2600-\u27BF\u2B00-\u2BFF\uFE0F\u200d\u2300-\u23FF]")


def page_text(slug):
    t = " ".join(v for k, v in L[slug] if k == "T" and cnt[v] <= 12 and not v.startswith("GuiaMTC -"))
    return re.sub(r"\s+", " ", EMOJI.sub("", t)).strip()


def season_of(fn):
    t = TIT.get(fn, {})
    s = (t.get("top", "") + " " + t.get("bottom", ""))
    m = re.search(r"Season\s*(\d+)(?:\s*&\s*(\d+))?", s)
    if not m:
        return None
    return m.group(1) + ("&" + m.group(2) if m.group(2) else "")


def role_of(ctx, fn):
    c = ctx.lower()
    t = (TIT.get(fn, {}).get("top", "") + " " + TIT.get(fn, {}).get("bottom", "")).lower()
    if "defense" in t or "defensive" in t or "efensor" in c:
        return "defenders"
    if "attack" in t or "tacantes" in c or "atacantes" in c:
        return "attackers"
    return "list"


def check(i):
    assert i in DB, f"DB'de yok: {i}"
    return i


tactics = []
problems = []
for slug, (tid, name, cross) in PAGES.items():
    lists = []
    last = ""
    for it in D[slug]:
        if "t" in it:
            last = (last + " " + it["t"])[-160:]
            continue
        i = it.get("i")
        if not i:
            continue
        fn = i["file"]
        if fn in FIXED:
            role, title, ids, unk = FIXED[fn]
            lists.append({"role": role, "title": title, "season": season_of(fn), "championIds": [check(x) for x in ids], "unidentified": unk})
            continue
        if fn in STRIP_FILES:
            lists.append({"_strip": fn})
            continue
        if i["kind"] != "grid":
            continue
        cells = G[fn]["cells"]
        ids, unk = [], 0
        for k, c in enumerate(cells):
            cid = None
            if c["st"] in ("ok", "ok_ocr", "ok_img"):
                cid = c["id"]
            elif (fn, k) in OVR:
                cid = OVR[(fn, k)]
            elif c["st"] in ("conflict", "unresolved"):
                unk += 1
                continue
            if cid:
                ids.append(check(cid))
        role = "counters" if "Bloqueiam Golpes" in last or "Bloqueiam" in last else role_of(last, fn)
        lists.append({"role": role, "title": (TIT.get(fn, {}).get("top", "") or TIT.get(fn, {}).get("bottom", "")).strip()[:60], "season": season_of(fn), "championIds": list(dict.fromkeys(ids)), "unidentified": unk})
    if slug in LABELED:
        role, title, season, ids = LABELED[slug]
        entry = {"role": role, "title": title, "season": season, "championIds": [check(x) for x in ids], "unidentified": 0}
        # savunmacilar once gelsin
        lists.insert(0 if role == "defenders" else len(lists), entry)
    tactics.append({"id": tid, "name": name, "pageUrl": "https://www.guiamtc.com/" + slug.replace("globais-aw_", "globais-aw/").replace("_", "-") if slug.startswith("globais") else "https://www.guiamtc.com/" + slug, "crossCheck": cross, "textPt": page_text(slug), "lists": lists})

# serit gorselleri: wrath unstoppable counters, poder remove prowess
strip_ids = {"globais-aw_wrath-aw__002.png": [], "globais-aw_poder-de-proeza__002.png": []}
for n, (f, r, c, m) in enumerate(STRIPS):
    if n in STRIP_DROP:
        continue
    cid = STRIP_FIX.get(n)
    if cid is None and (m[1] <= 1.25 or (m[1] <= 1.45 and m[2] >= 0.25)):
        cid = m[0]
    if cid is None:
        continue
    key = "globais-aw_wrath-aw__002.png" if f.startswith("-aw") else "globais-aw_poder-de-proeza__002.png"
    strip_ids[key].append(check(cid))
STRIP_META = {
    "globais-aw_wrath-aw__002.png": ("counters", "Unstoppable Counters / Evitam Insuperável (kısmi)"),
    "globais-aw_poder-de-proeza__002.png": ("counters", "Remove Prowess Effects / Removem Efeitos de Proeza (kısmi)"),
}
for t in tactics:
    for lst in t["lists"]:
        if "_strip" in lst:
            fn = lst.pop("_strip")
            role, title = STRIP_META[fn]
            lst.update({"role": role, "title": title, "season": None, "championIds": list(dict.fromkeys(strip_ids[fn])), "unidentified": -1})
    t["lists"] = [l for l in t["lists"] if l.get("championIds")]

out = {
    "source": "https://www.guiamtc.com (Globais AW sayfaları)",
    "capturedAt": "2026-10-01",
    "note": "Mekanik metinleri GuiaMTC'den (Portekizce). Şampiyon listeleri görsellerden okundu; etiket OCR'ı ve portre eşleşmesi çapraz doğrulandı, belirsiz hücreler listeye alınmadı (unidentified). 'season' görselin başlığındaki damgadır: liste o sezondaki kadro içindir.",
    "tactics": tactics,
}
json.dump(out, open(J + "guia_aw_globals.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)
for t in tactics:
    print(t["id"].ljust(26), [(l["role"], len(l["championIds"]), l["unidentified"], l["season"]) for l in t["lists"]])
