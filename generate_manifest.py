import hashlib
import json
import os
from datetime import datetime, timezone

ASSETS_DIR = "app/src/main/assets"
MANIFEST_PATH = os.path.join(ASSETS_DIR, "data_manifest.json")
TRACKED_TOP_LEVEL_FILES = ["champions_db.json", "roster.json"]
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
    for dirname in TRACKED_DIRS:
        dirpath = os.path.join(ASSETS_DIR, dirname)
        if not os.path.isdir(dirpath):
            continue
        for fname in sorted(os.listdir(dirpath)):
            if fname.endswith(".json"):
                files.append(f"{dirname}/{fname}")
    return sorted(files)


def load_previous_version():
    if not os.path.isfile(MANIFEST_PATH):
        return 0
    try:
        with open(MANIFEST_PATH, "r", encoding="utf-8") as f:
            return json.load(f).get("dataVersion", 0)
    except (json.JSONDecodeError, OSError):
        return 0


def generate_manifest():
    relative_paths = collect_files()
    file_hashes = {
        rel_path: sha256_of(os.path.join(ASSETS_DIR, rel_path))
        for rel_path in relative_paths
    }

    manifest = {
        "dataVersion": load_previous_version() + 1,
        "generatedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "files": file_hashes,
    }

    with open(MANIFEST_PATH, "w", encoding="utf-8") as f:
        json.dump(manifest, f, ensure_ascii=False, indent=2)

    print(f"data_manifest.json guncellendi -> dataVersion={manifest['dataVersion']}, {len(file_hashes)} dosya")


if __name__ == "__main__":
    generate_manifest()
