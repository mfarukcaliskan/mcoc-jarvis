package com.example.myapplication.data

enum class MatchMode(val displayName: String) {
    ALL("Tümü"),
    ANY("Herhangi")
}

data class ChampionFilter(
    val selectedClasses: Set<ChampionClass> = emptySet(),
    val selectedTiers: Set<String> = emptySet(),
    val selectedImmunities: Set<String> = emptySet(),
    val immunityMatchMode: MatchMode = MatchMode.ANY,
    val selectedAbilities: Set<String> = emptySet(),
    val abilityMatchMode: MatchMode = MatchMode.ANY,
    val selectedReactsTo: Set<String> = emptySet(),
    val reactsToMatchMode: MatchMode = MatchMode.ANY,
    val selectedCounterAbilities: Set<String> = emptySet(),
    val counterAbilityMatchMode: MatchMode = MatchMode.ANY,
    val selectedTags: Set<String> = emptySet(),
    val tagsMatchMode: MatchMode = MatchMode.ANY,
    val counterChampionId: String? = null,
    val searchQuery: String = "",
    val sortBy: SortOption = SortOption.PRESTIGE_DESC
) {
    val activeFilterCount: Int
        get() {
            var count = 0
            if (selectedClasses.isNotEmpty()) count++
            if (selectedTiers.isNotEmpty()) count++
            if (selectedImmunities.isNotEmpty()) count++
            if (selectedAbilities.isNotEmpty()) count++
            if (selectedReactsTo.isNotEmpty()) count++
            if (selectedCounterAbilities.isNotEmpty()) count++
            if (selectedTags.isNotEmpty()) count++
            if (counterChampionId != null) count++
            return count
        }
}

object FilterEngine {
    fun applyFilter(champions: List<Champion>, filter: ChampionFilter): List<Champion> {
        var result = champions

        // Search by name
        if (filter.searchQuery.isNotBlank()) {
            val query = filter.searchQuery.lowercase()
            result = result.filter { it.name.lowercase().contains(query) }
        }

        // Filter by class
        if (filter.selectedClasses.isNotEmpty()) {
            result = result.filter { it.mcocClass in filter.selectedClasses }
        }

        // Filter by tier
        if (filter.selectedTiers.isNotEmpty()) {
            result = result.filter { it.tier in filter.selectedTiers }
        }

        // Filter by immunity
        if (filter.selectedImmunities.isNotEmpty()) {
            result = result.filter { champion ->
                val matchFunc: (Set<String>, List<String>) -> Boolean = if (filter.immunityMatchMode == MatchMode.ALL) {
                    { selected, immunities -> selected.all { s -> immunities.any { it.lowercase().contains(s.lowercase()) } } }
                } else {
                    { selected, immunities -> selected.any { s -> immunities.any { it.lowercase().contains(s.lowercase()) } } }
                }
                matchFunc(filter.selectedImmunities, champion.immunities)
            }
        }

        // Filter by ability
        if (filter.selectedAbilities.isNotEmpty()) {
            result = result.filter { champion ->
                val abilityList = champion.abilities.split(",").map { it.trim().lowercase() }
                if (filter.abilityMatchMode == MatchMode.ALL) {
                    filter.selectedAbilities.all { selected ->
                        abilityList.any { it.contains(selected.lowercase()) }
                    }
                } else {
                    filter.selectedAbilities.any { selected ->
                        abilityList.any { it.contains(selected.lowercase()) }
                    }
                }
            }
        }

        // Filter by Reacts To
        if (filter.selectedReactsTo.isNotEmpty()) {
            result = result.filter { champion ->
                if (filter.reactsToMatchMode == MatchMode.ALL) {
                    filter.selectedReactsTo.all { selected ->
                        champion.reactsTo.any { it.lowercase().contains(selected.lowercase()) }
                    }
                } else {
                    filter.selectedReactsTo.any { selected ->
                        champion.reactsTo.any { it.lowercase().contains(selected.lowercase()) }
                    }
                }
            }
        }

        // Filter by Counter Ability
        if (filter.selectedCounterAbilities.isNotEmpty()) {
            result = result.filter { champion ->
                if (filter.counterAbilityMatchMode == MatchMode.ALL) {
                    filter.selectedCounterAbilities.all { selected ->
                        champion.counterAbilities.any { it.lowercase().contains(selected.lowercase()) }
                    }
                } else {
                    filter.selectedCounterAbilities.any { selected ->
                        champion.counterAbilities.any { it.lowercase().contains(selected.lowercase()) }
                    }
                }
            }
        }

        // Filter by tags
        if (filter.selectedTags.isNotEmpty()) {
            result = result.filter { champion ->
                if (filter.tagsMatchMode == MatchMode.ALL) {
                    filter.selectedTags.all { selected ->
                        champion.tags.any { it.lowercase().contains(selected.lowercase()) }
                    }
                } else {
                    filter.selectedTags.any { selected ->
                        champion.tags.any { it.lowercase().contains(selected.lowercase()) }
                    }
                }
            }
        }

        // Filter by Counter Champion
        if (filter.counterChampionId != null) {
            result = result.filter { champion ->
                champion.strongMatchups.any { it.lowercase() == filter.counterChampionId.lowercase() }
            }
        }

        // Sort
        result = when (filter.sortBy) {
            SortOption.PRESTIGE_DESC -> result.sortedByDescending { it.prestige }
            SortOption.PRESTIGE_ASC -> result.sortedBy { it.prestige }
            SortOption.ATTACK_DESC -> result.sortedByDescending { it.attack }
            SortOption.HEALTH_DESC -> result.sortedByDescending { it.health }
            SortOption.NAME_ASC -> result.sortedBy { it.name }
            SortOption.NAME_DESC -> result.sortedByDescending { it.name }
        }

        return result
    }

    // === Sitedeki tüm immunities listesi ===
    val allImmunities = listOf(
        "AA Modification" to "Yetenek Doğruluğu Değişimi",
        "AR Modification" to "Zırh Değişimi",
        "Armor Break" to "Zırh Kırma",
        "Armor Shattered" to "Zırh Parçalanması",
        "Bleed" to "Kanama",
        "Buffs" to "Buff",
        "Coldsnap" to "Ani Soğuk",
        "Concussion" to "Sarsıntı",
        "CPR Modification" to "CPR Değişimi",
        "Critical Hits" to "Kritik Vuruşlar",
        "Crush" to "Ezme",
        "Daunted" to "Yıldırma",
        "Degeneration" to "Dejenerasyon",
        "Delirium" to "Deliryum",
        "Disorient" to "Şaşırtma",
        "Exhaustion" to "Tükenme",
        "Fate Seal" to "Kader Mührü",
        "Fear" to "Korku",
        "Fragility" to "Kırılganlık",
        "Heal Block" to "İyileşme Engeli",
        "Incinerate" to "Yakma",
        "Intimidate" to "Yıldırma",
        "Neutralize" to "Nötralize",
        "Nullify" to "Silme",
        "Petrify" to "Taşlaştırma",
        "Poison" to "Zehir",
        "Power Burn" to "Güç Yakma",
        "Power Drain" to "Güç Emme",
        "Power Lock" to "Güç Kilidi",
        "Power Steal" to "Güç Çalma",
        "Rupture" to "Yırtılma",
        "Shock" to "Şok",
        "Slow" to "Yavaşlatma",
        "Stagger" to "Sendeletme",
        "Stun" to "Sersemletme",
        "Weakness" to "Zayıflık"
    )

    // === Sitedeki tüm abilities listesi ===
    val allAbilities = listOf(
        "Adrenaline", "Acid Burn", "Agility", "Amplify", "Aptitude",
        "Armor Up", "Armor Break", "Armor Shattered", "Aspect of Evolution",
        "Atrophy", "Austere Armor Up", "Auto-Block", "Battered", "Berserk",
        "Bleed", "Cauterize", "Coldsnap", "Combo Detonation", "Concussion",
        "Cruelty", "Crush", "Daunted", "Death Immunity", "Degeneration",
        "Delirium", "Devolve", "Direct Damage", "Disorient", "Energize",
        "Evade", "Exhaustion", "Falter", "Fate Seal", "Fear",
        "Fragility", "Fury", "Glancing", "Guaranteed Crits", "Heal Block",
        "Immortality", "Incinerate", "Indestructible", "Inexorable", "Intimidate",
        "Invisible", "Limbo", "Miss", "Mystic Dispersion", "Neurotoxin",
        "Nullify", "Organic Magnetism", "Passive Stun", "Petrify", "Phase",
        "Physical Resistance", "Poison", "Power Burn", "Power Drain", "Power Gain",
        "Power Lock", "Power Sting", "Power Steal", "Precision", "Prowess",
        "Purify", "Pursuit", "Regeneration", "Reinforce", "Reverb",
        "Reverse Controls", "Rupture", "Shock", "Slow", "Stagger",
        "Stun", "True Accuracy", "True Strike", "Unblockable", "Unstoppable",
        "Weakness", "Wither"
    )

    // === Counter Ability listesi ===
    val allCounterAbilities = listOf(
        "Armor Up", "Auto-Block", "Death Immunity", "Energize",
        "Evade", "Falter", "Fury", "Glancing", "Guaranteed Crits",
        "Immortality", "Indestructible", "Inexorable", "Invisible",
        "Miss", "Mystic Dispersion", "Power Gain", "Precision",
        "Prowess", "Purify", "Regeneration", "Stagger",
        "True Accuracy", "True Strike", "Unblockable", "Unstoppable"
    )

    // === Reacts To listesi ===
    val allReactsTo = listOf(
        "Armor Break", "Bleed", "Critical Hits", "Crush",
        "Damaging Effects", "Debuffs", "Disorient", "Energy Damage",
        "Fragility", "Incinerate", "Intimidate", "Poison",
        "Power Burn", "Power Drain", "Power Steal"
    )

    // === Tags listesi ===
    val allTags = listOf(
        "Villain", "Hero", "Mercenary", "Metal", "Robot", 
        "Size: S", "Size: M", "Size: L", "Size: XL", 
        "Offensive: Burst", "Offensive: DOT", "Offensive: Raw Damage",
        "Defensive: Guard", "Defensive: Tank", "Defensive: Utility",
        "Psychic Shielding", "Dimensional Being", "X-Men", "Avengers",
        "Saga Champions", "Symbiote", "Spider-Verse"
    )
}
