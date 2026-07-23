import os
import json

def split_champions_database():
    input_path = "app/src/main/assets/champions_db.json"
    output_dir = "app/src/main/assets/details"
    
    # Çıktı klasörünü kontrol et ve yoksa oluştur
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
        print(f"Klasor olusturuldu: {output_dir}")
        
    with open(input_path, "r", encoding="utf-8") as f:
        champions = json.load(f)
        
    lightweight_db = []
    
    for champ in champions:
        champ_id = champ.get("id")
        if not champ_id:
            continue
            
        # Ağır metin alanlarını ayır
        details = {
            "id": champ_id,
            "name": champ.get("name"),
            "abilityDetails": champ.get("abilityDetails", {}),
            "synergies": champ.get("synergies", []),
            "howToPlay": champ.get("howToPlay", ""),
            "bestUse": champ.get("bestUse", ""),
            "signatureAbility": champ.get("signatureAbility", "")
        }
        
        # Detay JSON dosyasını kaydet
        detail_path = os.path.join(output_dir, f"{champ_id}.json")
        with open(detail_path, "w", encoding="utf-8") as df:
            json.dump(details, df, ensure_ascii=False, indent=4)
            
        # Ana veritabanında kalacak hafif veri modelini oluştur
        light_champ = {
            "id": champ_id,
            "name": champ.get("name"),
            "mcocClass": champ.get("mcocClass"),
            "tier": champ.get("tier", "A"),
            "prestige": champ.get("prestige", 0),
            "prestigeRank": champ.get("prestigeRank", 0),
            "attack": champ.get("attack", 0),
            "health": champ.get("health", 0),
            "critRate": champ.get("critRate", 0.0),
            "critDamage": champ.get("critDamage", 0.0),
            "armor": champ.get("armor", 0.0),
            "blockProficiency": champ.get("blockProficiency", 0.0),
            "immunities": champ.get("immunities", []),
            "counters": champ.get("counters", []),
            "abilities": champ.get("abilities", ""),
            "synergies": champ.get("synergies", []), # Sinerjiler ters arama icin hafizada kalmali
            "tags": champ.get("tags", []),
            "strongMatchups": champ.get("strongMatchups", []),
            "strongCounters": champ.get("strongCounters", []),
            "reactsTo": champ.get("reactsTo", []),
            "counterAbilities": champ.get("counterAbilities", []),
            "releaseDate": champ.get("releaseDate", ""),
            "attackRank": champ.get("attackRank", 0),
            "healthRank": champ.get("healthRank", 0),
            "critRateRank": champ.get("critRateRank", 0),
            "critDamageRank": champ.get("critDamageRank", 0),
            "armorRank": champ.get("armorRank", 0),
            "blockProficiencyRank": champ.get("blockProficiencyRank", 0),
            "progressions": champ.get("progressions", []) # Prestij hesaplama için zorunlu matris
        }
        lightweight_db.append(light_champ)
        
    # Hafifletilmiş ana veritabanını üzerine yazdır
    with open(input_path, "w", encoding="utf-8") as lf:
        json.dump(lightweight_db, lf, ensure_ascii=False, indent=4)
        
    print(f"Islem Tamamlandi! {len(champions)} sampiyonun detaylari 'assets/details/' klasorune tasindi.")

if __name__ == "__main__":
    split_champions_database()
