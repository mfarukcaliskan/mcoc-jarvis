#!/usr/bin/env python3
"""GuiaMTC "Best Defenders" sayfalarindan savunmaci -> counter verisini uretir.

Kullanim (proje kokunden):
  python tools/sync_mcoc.py fetch        # once mcoc.gg verisi (champions.json) gerekir
  python tools/sync_guia.py              # app/src/main/assets/guia_counters.json uretir

GuiaMTC icerigi gorsellerden olusur: her savunmacinin adi metinde, counter'lari ise tek tek
portre gorselleri olarak verilir. Portreler mcoc.gg portreleriyle goruntu benzerligi ile eslenir.
Guvenilir eslesmeyen portre ASLA tahmin edilmez; listeden dusurulur ve rapora yazilir.
Elle dogrulanmis duzeltmeler tools/guia_overrides.json icinde (gorselin SHA-1'i -> sampiyon id).

Gerekli: Pillow, numpy.
Kaynak site: https://www.guiamtc.com (icerik GuiaMTC'nin emegidir; uygulamada kaynak belirtilir).
"""
import hashlib
import html
import json
import os
import re
import sys
import time
import urllib.parse
import urllib.request
from datetime import datetime, timezone

import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CACHE = os.path.join(ROOT, "tools", ".cache")
ASSETS = os.path.join(ROOT, "app", "src", "main", "assets")
BASE = "https://www.guiamtc.com"
UA = {"User-Agent": "Mozilla/5.0", "Referer": BASE + "/"}
PAGES = {
    "COSMIC": "/best-defenders/defenders-cosmic", "TECH": "/best-defenders/defenders-tech",
    "MUTANT": "/best-defenders/defenders-mutant", "SKILL": "/best-defenders/defenders-skill",
    "SCIENCE": "/best-defenders/defenders-science", "MYSTIC": "/best-defenders/defenders-mystic",
}
# GuiaMTC'nin kullandigi kisaltma/yazimlar -> bizim id (elle dogrulandi)
NAME_ALIASES = {
    "phyllavell": "phylavell", "spideysupreme": "spidermansupreme", "destroyer": "thedestroyer",
    "spidermanpavitr": "pavitr", "imiw": "ironmaninfinitywar", "capwilson": "samwilson",
    "antfuture": "antmanfuture", "spiderslayer": "jjj", "mistyknigth": "mistyknight",
    "bpcw": "blackpanthercivilwar", "domin": "domino", "mrsinister": "mistersinister",
    "wolverinex": "wolverinex23", "stormx": "stormpyramidx", "negasonic": "negasonic",
    "mrfantastic": "misterfantastic", "immortalhulk": "hulkimmortal", "leader": "theleader",
}
S = 28
ACCEPT_D = 1.25       # bu mesafenin altindaki eslesme dogrudan kabul
ACCEPT_LOOSE = 1.45   # bu mesafeye kadar yalnizca ikinci adaydan yeterince uzaksa kabul
ACCEPT_MARGIN = 0.25


def http(url):
    return urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=60).read()


def norm(s):
    return re.sub(r"[^a-z0-9]", "", s.lower())


def feat(img):
    im = img.convert("RGBA")
    w, h = im.size
    im = im.crop((int(w * 0.08), int(h * 0.04), int(w * 0.92), int(h * 0.78)))
    bg = Image.new("RGBA", im.size, (128, 128, 128, 255))
    bg.alpha_composite(im)
    a = np.asarray(bg.convert("RGB").resize((S, S), Image.LANCZOS), dtype=np.float32)
    g = a.mean(axis=2)
    g = (g - g.mean()) / (g.std() + 1e-6)
    return g.ravel(), (a / 255.0).ravel()


def load_refs():
    champs = json.load(open(os.path.join(CACHE, "mcoc", "champions.json"), encoding="utf-8"))["data"]
    folder = os.path.join(CACHE, "portraits")
    os.makedirs(folder, exist_ok=True)
    names, G, C = [], [], []
    for c in champs:
        path = os.path.join(folder, c["image"] + ".webp")
        if not os.path.isfile(path):
            data = urllib.request.urlopen(urllib.request.Request(
                f"https://mcoc.gg/images/portraits/{c['image']}.webp", headers={"User-Agent": "Mozilla/5.0"}), timeout=30).read()
            open(path, "wb").write(data)
            time.sleep(0.05)
        g, col = feat(Image.open(path))
        names.append(c["image"])
        G.append(g)
        C.append(col)
    return names, np.stack(G), np.stack(C)


def page_images_and_text(path):
    raw = http(BASE + urllib.parse.quote(path, safe="/-_.~")).decode("utf-8", "ignore")
    body = html.unescape(re.sub(r"<(script|style|noscript)\b.*?</\1>", "", raw, flags=re.S))
    seq = []
    for m in re.finditer(r'<img[^>]*?src="(https://[^"]+)"[^>]*>|>([^<>]{2,})<', body):
        if m.group(1):
            seq.append(["I", m.group(1)])
        elif m.group(2).strip():
            seq.append(["T", m.group(2).strip()])
    return seq


def download_all(path, seq):
    """Adresler birkac dakikada gecersizleniyor: her 35 indirmede sayfayi yeniden alip taze adres kullanir."""
    pos = [i for i, (k, _) in enumerate(seq) if k == "I"]
    data = {}
    urls = [seq[i][1] for i in pos]
    done_since_refresh = 0
    for n, url in enumerate(urls):
        for attempt in range(3):
            try:
                data[n] = http(url)
                break
            except Exception:
                urls = [seq[i][1] for i in pos]
                fresh = [x[1] for x in page_images_and_text(path) if x[0] == "I"]
                if len(fresh) == len(urls):
                    urls = fresh
                url = urls[n]
        done_since_refresh += 1
        if done_since_refresh >= 35:
            fresh = [x[1] for x in page_images_and_text(path) if x[0] == "I"]
            if len(fresh) == len(urls):
                urls = fresh
            done_since_refresh = 0
        time.sleep(0.03)
    return pos, data


def blocks(seq, data, pos):
    """Metin + ardindan gelen gorsel dizisi bloklari."""
    img_at = {p: n for n, p in enumerate(pos)}
    out, text, run = [], [], []
    for i, (k, v) in enumerate(seq + [["T", "\0END"]]):
        if k == "T":
            if run:
                out.append({"text": " ".join(text), "images": run})
                text, run = [], []
            text.append(v)
        elif i in img_at and img_at[i] in data:
            run.append(data[img_at[i]])
    return out


def defender_name_and_tip(text):
    m = re.search(r"Counters\s+para\s+(?:o\s+|a\s+|os\s+|as\s+)?(.+?)\s*Dicas", text, re.S)
    if not m:
        return None, None
    # ipucu "Dicas contra este defensor" ifadesinden sonra baslar; 💡 simgesi her sayfada yok
    tip = text[m.end():].replace("contra este defensor", "", 1).replace("💡", "")
    return m.group(1).strip(), re.sub(r"\s+", " ", tip).strip()


def classify(blob, refs, overrides):
    h = hashlib.sha1(blob).hexdigest()
    if h in overrides:
        return overrides[h], "override"
    names, G, C = refs
    import io
    g, c = feat(Image.open(io.BytesIO(blob)))
    d = np.sqrt(((G - g) ** 2).mean(axis=1)) + 2.0 * np.sqrt(((C - c) ** 2).mean(axis=1))
    order = np.argsort(d)[:2]
    d1, d2 = float(d[order[0]]), float(d[order[1]])
    if d1 <= ACCEPT_D or (d1 <= ACCEPT_LOOSE and d2 - d1 >= ACCEPT_MARGIN):
        return names[order[0]], "auto"
    return None, f"belirsiz({names[order[0]]} {d1:.2f})"


def main():
    if not os.path.isfile(os.path.join(CACHE, "mcoc", "champions.json")):
        sys.exit("Once: python tools/sync_mcoc.py fetch")
    ours = json.load(open(os.path.join(ASSETS, "champions_db.json"), encoding="utf-8"))
    name2id = {}
    for c in ours:
        name2id.setdefault(norm(c["name"]), c["id"])
    valid_ids = {c["id"] for c in ours}
    # mcoc.gg 'image' kimligi ile bizim id ayni olmayabilir (agatha)
    alias_ids = {"agatha": "agathaharkness"}
    ov_path = os.path.join(ROOT, "tools", "guia_overrides.json")
    overrides = json.load(open(ov_path, encoding="utf-8")) if os.path.isfile(ov_path) else {}
    refs = load_refs()
    result, report = [], {"belirsiz_portre": [], "eslesmeyen_savunmaci": []}
    for klass, path in PAGES.items():
        seq = page_images_and_text(path)
        pos, data = download_all(path, seq)
        bl = blocks(seq, data, pos)
        for i, b in enumerate(bl):
            name, tip = defender_name_and_tip(b["text"])
            if not name:
                continue
            is_last = not any(defender_name_and_tip(x["text"])[0] for x in bl[i + 1:])
            imgs = b["images"] if is_last else b["images"][:-1]  # son gorsel bir sonraki savunmacinin kartidir
            did = NAME_ALIASES.get(norm(name)) or name2id.get(norm(name))
            if did not in valid_ids:
                report["eslesmeyen_savunmaci"].append(name)
                continue
            counters = []
            for blob in imgs:
                cid, how = classify(blob, refs, overrides)
                if cid is None:
                    report["belirsiz_portre"].append((name, how, hashlib.sha1(blob).hexdigest()))
                    continue
                cid = alias_ids.get(cid, cid)
                if cid in valid_ids and cid not in counters and cid != did:
                    counters.append(cid)
            result.append({"id": did, "mcocClass": klass, "tip": tip, "counters": counters})
        print(klass, "tamam", flush=True)
    out = {
        "source": "https://www.guiamtc.com/best-defenders",
        "note": "Counter listeleri ve ipuclari GuiaMTC'den (Portekizce); portreler mcoc.gg ile eslendi.",
        "generatedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "defenders": result,
    }
    target = os.path.join(ASSETS, "guia_counters.json")
    if os.path.isfile(target):
        with open(target, encoding="utf-8") as f:
            previous = json.load(f)
        # zaman damgasi disinda ayniysa dosyaya dokunma (bos commit olmasin)
        if {k: v for k, v in previous.items() if k != "generatedAt"} == {k: v for k, v in out.items() if k != "generatedAt"}:
            print("guia_counters.json degismedi")
            return
    with open(target, "w", encoding="utf-8") as f:
        json.dump(out, f, ensure_ascii=False, indent=1)
    print(f"{len(result)} savunmaci, {sum(len(r['counters']) for r in result)} counter yazildi")
    print("belirsiz portre:", report["belirsiz_portre"] or "yok")
    print("eslesmeyen savunmaci adi:", report["eslesmeyen_savunmaci"] or "yok")


if __name__ == "__main__":
    main()
