package com.example.myapplication.data

object QuestAnalyzer {
    
    data class RecommendationResult(
        val matchedChampions: List<Champion>,
        val reason: String
    )

    fun analyzePath(path: QuestPath, userDeck: List<Champion>): RecommendationResult {
        val requiredImmunities = mutableSetOf<String>()
        val requiredAbilities = mutableSetOf<String>()
        val descriptionParts = mutableListOf<String>()

        for (node in path.pathNodes) {
            val nodeLower = node.lowercase()
            if (nodeLower.contains("biohazard") || nodeLower.contains("biyolojik tehlike")) {
                requiredImmunities.add("bleed")
                requiredImmunities.add("poison")
                descriptionParts.add("Biyolojik Tehlike karosu için Kanama (Bleed) ve Zehir (Poison) bağışıklığı gerekir.")
            }
            if (nodeLower.contains("zehir")) {
                requiredImmunities.add("poison")
                descriptionParts.add("Zehir bağışıklığı gerekir.")
            }
            if (nodeLower.contains("kanama")) {
                requiredImmunities.add("bleed")
                descriptionParts.add("Kanama bağışıklığı gerekir.")
            }
            if (nodeLower.contains("kaçış") || nodeLower.contains("evade")) {
                requiredAbilities.add("slow")
                requiredAbilities.add("coldsnap")
                requiredAbilities.add("true strike")
                requiredAbilities.add("true accuracy")
                descriptionParts.add("Kaçış (Evade) etkisini kapatmak için Yavaşlatma (Slow) veya Ani Soğuk (Coldsnap) gerekir.")
            }
            if (nodeLower.contains("durdurulamaz") || nodeLower.contains("unstoppable")) {
                requiredAbilities.add("slow")
                requiredAbilities.add("stagger")
                descriptionParts.add("Durdurulamaz (Unstoppable) etkisini iptal etmek için Yavaşlatma (Slow) veya Sendeletme (Stagger) önerilir.")
            }
            if (nodeLower.contains("güç hattı") || nodeLower.contains("power gain")) {
                requiredAbilities.add("petrify")
                requiredAbilities.add("power sting")
                requiredAbilities.add("power drain")
                requiredAbilities.add("wither")
                descriptionParts.add("Güç kazanımını kontrol etmek için Güç Sızması (Power Sting), Güç Boşaltma veya Zayıflatma (Wither) önerilir.")
            }
        }

        // Try to match deck champions
        val recommended = userDeck.filter { champ ->
            val hasImmunity = requiredImmunities.any { req ->
                champ.immunities.any { it.lowercase().contains(req) }
            }
            val hasAbility = requiredAbilities.any { req ->
                champ.abilities.lowercase().contains(req)
            }
            hasImmunity || hasAbility
        }

        val reasonText = if (descriptionParts.isEmpty()) {
            "Bu yol için kritik bir karo kısıtlaması bulunamadı. Genel amaçlı en güçlü şampiyonlarınızı getirebilirsiniz."
        } else {
            descriptionParts.joinToString(" ")
        }

        return RecommendationResult(
            matchedChampions = recommended.distinctBy { it.id }.take(3),
            reason = reasonText
        )
    }
}
