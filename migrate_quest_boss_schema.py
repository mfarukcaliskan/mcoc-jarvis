import json
import glob
import os

QUESTS_DIR = "app/src/main/assets/quests"


def migrate_file(path):
    with open(path, encoding="utf-8") as f:
        data = json.load(f)

    if "bosses" in data:
        return False  # already migrated, idempotent no-op

    boss = data.pop("boss")
    data["bosses"] = [boss]

    champion_id = boss["championId"]
    for path_obj in data.get("paths", []):
        path_obj["leadsToBoss"] = champion_id

    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")

    return True


def main():
    files = sorted(glob.glob(os.path.join(QUESTS_DIR, "*.json")))
    migrated = 0
    skipped = 0
    for fp in files:
        if migrate_file(fp):
            migrated += 1
        else:
            skipped += 1
    print(f"Migrated: {migrated}, already up to date: {skipped}, total: {len(files)}")


if __name__ == "__main__":
    main()
