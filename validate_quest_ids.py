import json
import glob

ASSETS = "app/src/main/assets"


def main():
    with open(f"{ASSETS}/champions_db.json", encoding="utf-8") as f:
        champs = json.load(f)
    valid_ids = set(c["id"] for c in champs)

    problems = []
    for fp in sorted(glob.glob(f"{ASSETS}/quests/*.json")):
        with open(fp, encoding="utf-8") as f:
            data = json.load(f)

        boss_ids = {b["championId"] for b in data.get("bosses", [])}

        for path in data.get("paths", []):
            for defender in path.get("defenders", []):
                if defender not in valid_ids:
                    problems.append((fp, "defender", defender))
            leads_to = path.get("leadsToBoss")
            if leads_to and leads_to not in boss_ids:
                problems.append((fp, "leadsToBoss dangling reference", leads_to))

        for boss in data.get("bosses", []):
            if boss["championId"] not in valid_ids:
                problems.append((fp, "boss championId", boss["championId"]))
            for counter in boss.get("idealCounters", []):
                if counter not in valid_ids:
                    problems.append((fp, "idealCounter", counter))

    if not problems:
        print("Tum quest dosyalari gecerli - hicbir sorun bulunamadi.")
        return

    print(f"{len(problems)} sorun bulundu:")
    for fp, kind, value in problems:
        print(f"  {fp}: {kind} -> '{value}'")


if __name__ == "__main__":
    main()
