"""Bilesik gorsellerde portre hucrelerini bulur (tam siyah bosluklarla ayrilmis kutular)."""
import collections

import numpy as np
from PIL import Image

BLACK = 6


def runs_of(flags, min_len=1):
    out, s = [], None
    for i, f in enumerate(list(flags) + [False]):
        if f and s is None:
            s = i
        elif not f and s is not None:
            if i - s >= min_len:
                out.append((s, i))
            s = None
    return out


def find_cells(path):
    im = Image.open(path).convert("RGB")
    a = np.asarray(im).astype(int)
    h, w, _ = a.shape
    black = a.max(axis=2) <= BLACK
    row_black = black.mean(axis=1) >= 0.995
    bands = runs_of(~row_black, 60)
    raw = []
    for y0, y1 in bands:
        col_black = black[y0:y1].mean(axis=0) >= 0.995
        for x0, x1 in runs_of(~col_black, 60):
            raw.append((x0, y0, x1, y1))
    if not raw:
        return im, []
    widths = collections.Counter(x1 - x0 for x0, y0, x1, y1 in raw)
    mode_w = max(widths, key=lambda k: (sum(v for kk, v in widths.items() if abs(kk - k) <= 3), k))
    cells = []
    for x0, y0, x1, y1 in raw:
        cw = x1 - x0
        if abs(cw - mode_w) > 4 or (y1 - y0) < cw * 0.9:
            continue
        py1 = min(y1, y0 + cw + 2)  # portre kare; altindaki etiket seridini at
        region = a[y0:py1, x0:x1]
        if region.std() < 8:  # bos zemin
            continue
        cells.append((x0, y0, x1, py1))
    return im, cells


def find_cells_labeled(path, min_len=60):
    """Portre hucreleri + her birinin altindaki etiket seridi (ince siyah cizgiyle ayrilmis)."""
    im = Image.open(path).convert("RGB")
    a = np.asarray(im).astype(int)
    h, w, _ = a.shape
    black = a.max(axis=2) <= BLACK
    row_black = black.mean(axis=1) >= 0.995
    raw = []
    for y0, y1 in runs_of(~row_black, min_len):
        col_black = black[y0:y1].mean(axis=0) >= 0.995
        for x0, x1 in runs_of(~col_black, min_len):
            raw.append((x0, y0, x1, y1))
    if not raw:
        return im, []
    widths = collections.Counter(x1 - x0 for x0, y0, x1, y1 in raw)
    mode_w = max(widths, key=lambda k: (sum(v for kk, v in widths.items() if abs(kk - k) <= 3), k))
    out = []
    for x0, y0, x1, y1 in raw:
        cw = x1 - x0
        if abs(cw - mode_w) > 4 or (y1 - y0) < cw * 0.85:
            continue
        py1 = y1 if (y1 - y0) <= cw * 1.25 else y0 + cw + 2
        if a[y0:py1, x0:x1].std() < 8:
            continue
        # etiket: portre altindan baslayip ilk dolu satir grubu (10-70 px)
        ly0 = ly1 = None
        yy = py1
        while yy < min(h, py1 + 12) and black[yy, x0:x1].mean() >= 0.9:
            yy += 1
        ly0 = yy
        yy = ly0
        while yy < h and black[yy, x0:x1].mean() < 0.9:
            yy += 1
        ly1 = yy
        label = (x0, ly0, x1, ly1) if 8 <= ly1 - ly0 <= 70 else None
        out.append({"portrait": (x0, y0, x1, py1), "label": label})
    return im, out


def find_strip_cells(path, dark=40, period_range=(48, 80), icon_cols=1):
    """Etiketsiz, satirlara bolunmus portre seritleri: satirlari yatay siyah cizgilerden,
    hucreleri periyodik dikey siyah cerceve izlerinden bulur."""
    im = Image.open(path).convert("RGB")
    a = np.asarray(im).astype(int)
    h, w, _ = a.shape
    dk = a.max(axis=2) <= dark
    row_sep = dk.mean(axis=1) >= 0.9
    rows = runs_of(~row_sep, 30)
    out = []
    for ri, (y0, y1) in enumerate(rows):
        band = dk[y0:y1]
        col = band.mean(axis=0)
        best = None
        for p in range(period_range[0], period_range[1] + 1):
            for ph in range(p):
                xs = range(ph, w, p)
                sc = sum(col[x] for x in xs if x < w) / max(1, len(list(xs)))
                if best is None or sc > best[0]:
                    best = (sc, p, ph)
        _, p, ph = best
        bh = y1 - y0
        for k, x in enumerate(range(ph, w - p + 2, p)):
            x0, x1 = x + 2, min(x + p - 1, w)
            if x1 - x0 < p * 0.7 or bh < 30:
                continue
            reg = a[y0:y1, x0:x1]
            if reg.std() < 10:
                continue
            out.append({"row": ri, "col": k, "box": (x0, y0, x1, y1)})
    return im, out


def _best_period(profile, lo, hi):
    n = len(profile)
    best = None
    for p in range(lo, hi + 1):
        for ph in range(p):
            idx = np.arange(ph, n, p)
            sc = profile[idx].mean()
            if best is None or sc > best[0]:
                best = (sc, p, ph)
    return best[1], best[2]


def find_lattice_cells(path, dark=40, period=(54, 74)):
    """Duzgun araliklarla dizilmis (satir+sutun) portre kafesi; her kafes hucresini doner."""
    im = Image.open(path).convert("RGB")
    a = np.asarray(im).astype(int)
    h, w, _ = a.shape
    dk = a.max(axis=2) <= dark
    px, phx = _best_period(dk.mean(axis=0), *period)
    py, phy = _best_period(dk.mean(axis=1), *period)
    out = []
    for r, y in enumerate(range(phy, h - py + 2, py)):
        for c, x in enumerate(range(phx, w - px + 2, px)):
            x0, y0, x1, y1 = x + 3, y + 3, min(x + px - 2, w), min(y + py - 2, h)
            if x1 - x0 < px * 0.7 or y1 - y0 < py * 0.7:
                continue
            reg = a[y0 + 3:y1 - 3, x0 + 3:x1 - 3]
            if reg.std() < 22:
                continue
            out.append({"row": r, "col": c, "box": (x0, y0, x1, y1)})
    return im, out, (px, py)
