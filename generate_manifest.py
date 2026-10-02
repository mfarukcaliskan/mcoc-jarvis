import glob
import hashlib
import json
import os
from datetime import datetime, timezone

ASSETS_DIR = "app/src/main/assets"
MANIFEST_PATH = os.path.join(ASSETS_DIR, "data_manifest.json")
TRACKED_TOP_LEVEL_FILES = ["champions_db.json", "relics.json", "meta.json", "guia_counters.json", "guia_aw_globals.json", "guia_raids.json", "guia_champions.json", "guia_tiers.json", "capabilities.json", "prestige.json", "relic_statcast.json", "events.json", "guia_relics.json", "guia_rank7.json", "guia_guides.json", "champion_extra.json", "synergies.json"]
TRACKED_DIRS = ["details", "quests"]


def sha256_of(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def collect_files():
    files = []
    for name in TRACKED_TOP_LEVEL_FILES:
        path = os.path.join(ASSETS_DIR, name)
        if os.path.isfile(path):
            files.append(name)
    # AW sezon dosyalari sezon numarasina gore degisir (tools/aw_retention.py en yeni 2 sezonu tutar)
    for pattern in ("guia_aw_season*.json", "guia_aw*_helpers.json", "guia_aw_bigthing.json"):
        for path in sorted(glob.glob(os.path.join(ASSETS_DIR, pattern))):
            files.append(os.path.basename(path))
    for dirname in TRACKED_DIRS:
        dirpath = os.path.join(ASSETS_DIR, dirname)
        if not os.path.isdir(dirpath):
            continue
        for fname in sorted(os.listdir(dirpath)):
            if fname.endswith(".json"):
                files.append(f"{dirname}/{fname}")
    return sorted(files)


def load_previous_manifest():
    if not os.path.isfile(MANIFEST_PATH):
        return {"dataVersion": 0, "files": {}}
    try:
        with open(MANIFEST_PATH, "r", encoding="utf-8") as f:
            data = json.load(f)
            return {"dataVersion": data.get("dataVersion", 0), "files": data.get("files", {})}
    except (json.JSONDecodeError, OSError):
        return {"dataVersion": 0, "files": {}}


def generate_manifest():
    relative_paths = collect_files()
    file_hashes = {
        rel_path: sha256_of(os.path.join(ASSETS_DIR, rel_path))
        for rel_path in relative_paths
    }

    previous = load_previous_manifest()
    content_changed = file_hashes != previous["files"]
    new_version = previous["dataVersion"] + 1 if content_changed else previous["dataVersion"]

    manifest = {
        "dataVersion": new_version,
        "generatedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "files": file_hashes,
    }

    with open(MANIFEST_PATH, "w", encoding="utf-8") as f:
        json.dump(manifest, f, ensure_ascii=False, indent=2)

    if content_changed:
        print(f"data_manifest.json guncellendi -> dataVersion={manifest['dataVersion']}, {len(file_hashes)} dosya")
    else:
        print(f"data_manifest.json degismedi (icerik ayni) -> dataVersion={manifest['dataVersion']}, {len(file_hashes)} dosya")


if __name__ == "__main__":
    generate_manifest()
