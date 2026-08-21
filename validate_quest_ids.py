import json
import glob

ASSETS = "app/src/main/assets"

# Act 8/9'da gecen ama MCOC'ta hicbir zaman oynanabilir cikmamis, tek seferlik
# boss/NPC gorunumleri. Fandom'in tam sampiyon listesiyle dogrulandi
# (2026-08-21, bkz. eksik_bilgiler/TAKIP.md bolum 1b) - roster'a eklenecek
# bir eksik degil, gercek bir ozellik sinirlamasi. Bu id'ler valid_ids'e asla
# girmeyecegi icin asagida ayri bir "bilinen" kovaya alinip normal sorun
# listesinden ayristiriliyor.
KNOWN_NON_ROSTER_DEFENDERS = {
    "black_dwarf", "thena", "us_agent", "scourge", "mystique",
    "nightmare", "morgan_le_fay", "quasar",
}


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

    known = [p for p in problems if p[1] == "defender" and p[2] in KNOWN_NON_ROSTER_DEFENDERS]
    unknown = [p for p in problems if p not in known]

    if not unknown and not known:
        print("Tum quest dosyalari gecerli - hicbir sorun bulunamadi.")
        return

    if unknown:
        print(f"{len(unknown)} GERCEK sorun bulundu (arastirilmali):")
        for fp, kind, value in unknown:
            print(f"  {fp}: {kind} -> '{value}'")
    else:
        print("Gercek sorun yok.")

    if known:
        print(f"\n{len(known)} bilinen/onaylanmis durum (duzeltme gerektirmiyor - bkz. eksik_bilgiler/TAKIP.md bolum 1b):")
        for fp, kind, value in known:
            print(f"  {fp}: {kind} -> '{value}'")


if __name__ == "__main__":
    main()
