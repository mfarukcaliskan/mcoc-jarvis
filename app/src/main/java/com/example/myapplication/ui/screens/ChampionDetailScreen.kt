package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Champion
import com.example.myapplication.data.ChampionRepository

fun calculateDetailDynamicPrestige(champion: Champion, star: Int, rank: Int, sig: Int): Int {
    val starProg = champion.progressions.find { it.starRating == star } ?: champion.progressions.firstOrNull()
    if (starProg == null) return champion.prestige
    val rankStat = starProg.ranks.find { it.rank == rank } ?: starProg.ranks.firstOrNull()
    if (rankStat == null) return champion.prestige

    val maxSig = 200.0
    val factor = Math.pow(sig.toDouble() / maxSig, 0.8)
    val dynamicPrestige = rankStat.basePrestige + (rankStat.maxPrestige - rankStat.basePrestige) * factor
    return dynamicPrestige.toInt()
}

fun getDetailDynamicAttack(champion: Champion, star: Int, rank: Int): Int {
    val starProg = champion.progressions.find { it.starRating == star } ?: champion.progressions.firstOrNull()
    if (starProg == null) return champion.attack
    val rankStat = starProg.ranks.find { it.rank == rank } ?: starProg.ranks.firstOrNull()
    return rankStat?.attack ?: champion.attack
}

fun getDetailDynamicHealth(champion: Champion, star: Int, rank: Int): Int {
    val starProg = champion.progressions.find { it.starRating == star } ?: champion.progressions.firstOrNull()
    if (starProg == null) return champion.health
    val rankStat = starProg.ranks.find { it.rank == rank } ?: starProg.ranks.firstOrNull()
    return rankStat?.health ?: champion.health
}

fun getInboundSynergies(targetChampion: Champion): List<Pair<Champion, String>> {
    val result = mutableListOf<Pair<Champion, String>>()
    ChampionRepository.champions.forEach { champ ->
        champ.synergies.forEach { synergy ->
            if (synergy.partnerName.equals(targetChampion.name, ignoreCase = true)) {
                result.add(champ to synergy.bonus)
            }
        }
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChampionDetailScreen(championId: String, onBack: () -> Unit = {}) {
    val allChampions = ChampionRepository.champions
    val currentIndex = allChampions.indexOfFirst { it.id == championId }
    var displayIndex by remember(championId) { mutableIntStateOf(currentIndex) }

    val champion = if (displayIndex in allChampions.indices) allChampions[displayIndex] else null
    if (champion == null) {
        Text("Şampiyon bulunamadı", color = Color.White)
        return
    }

    val context = LocalContext.current
    var championDetails by remember(displayIndex) { mutableStateOf<com.example.myapplication.data.ChampionDetails?>(null) }
    var isDetailsLoading by remember(displayIndex) { mutableStateOf(true) }

    LaunchedEffect(displayIndex) {
        isDetailsLoading = true
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            championDetails = ChampionRepository.loadChampionDetails(context, champion.id)
        }
        isDetailsLoading = false
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("İstatistikler", "Yetenekler", "Sinerjiler", "Rehber")

    // Dynamic stats selection state
    var selectedStar by remember(championId) { mutableIntStateOf(6) }
    var selectedRank by remember(championId) { mutableIntStateOf(5) }
    var selectedSig by remember(championId) { mutableIntStateOf(0) }

    var hasLiquidCourage by remember { mutableStateOf(false) }
    var hasDoubleEdge by remember { mutableStateOf(false) }
    var hasClassMastery by remember { mutableStateOf(false) }

    val basePrestige = calculateDetailDynamicPrestige(champion, selectedStar, selectedRank, selectedSig)
    val baseAttack = getDetailDynamicAttack(champion, selectedStar, selectedRank)
    val baseHealth = getDetailDynamicHealth(champion, selectedStar, selectedRank)

    var prestigeMultiplier = 1.0
    if (hasLiquidCourage) prestigeMultiplier += 0.05
    if (hasDoubleEdge) prestigeMultiplier += 0.05
    if (hasClassMastery) prestigeMultiplier += 0.05

    var attackMultiplier = 1.0
    if (hasLiquidCourage) attackMultiplier += 0.30
    if (hasDoubleEdge) attackMultiplier += 0.30

    val dynamicPrestige = (basePrestige * prestigeMultiplier).toInt()
    val dynamicAttack = (baseAttack * attackMultiplier).toInt()
    val dynamicHealth = baseHealth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            // ===== HEADER =====
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color(0xFF00BFFF))
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    val context = LocalContext.current
                    val drawableName = champion.id.lowercase().replace("-", "_").let { if (it[0].isDigit() || it == "void") "img_$it" else it }
                    val imageResId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

                    if (imageResId != 0) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = champion.name,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(champion.mcocClass.color).copy(alpha = 0.15f))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column {
                        Text(champion.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            "${champion.mcocClass.displayName} • ${champion.tier} Tier • #${champion.prestigeRank}",
                            fontSize = 13.sp,
                            color = Color(champion.mcocClass.color)
                        )
                        if (champion.releaseDate.isNotEmpty()) {
                            Text(
                                "Çıkış: ${champion.releaseDate}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // ===== STAT SIMULATOR PANEL =====
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2333)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("MCOC.gg Stat Simülatörü", color = Color(0xFF00BFFF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Star select
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Yıldız: ", color = Color.Gray, fontSize = 11.sp)
                                listOf(6, 7).forEach { star ->
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .background(
                                                if (selectedStar == star) Color(0xFF00BFFF).copy(alpha = 0.2f) else Color(0xFF161B22),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable { selectedStar = star }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("${star}★", color = if (selectedStar == star) Color(0xFF00BFFF) else Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                            
                            // Rank select
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Rütbe: ", color = Color.Gray, fontSize = 11.sp)
                                (1..5).forEach { r ->
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 2.dp)
                                            .background(
                                                if (selectedRank == r) Color(0xFFFF6B35).copy(alpha = 0.2f) else Color(0xFF161B22),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable { selectedRank = r }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Text("R$r", color = if (selectedRank == r) Color(0xFFFF6B35) else Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Sig level slider
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Kopya Seviyesi (Sig):", color = Color.Gray, fontSize = 11.sp)
                                Text("$selectedSig", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Slider(
                                value = selectedSig.toFloat(),
                                onValueChange = { selectedSig = it.toInt() },
                                valueRange = 0f..200f,
                                steps = 199,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF00BFFF),
                                    activeTrackColor = Color(0xFF00BFFF),
                                    inactiveTrackColor = Color(0xFF161B22)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFF30363D))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🛡️ Ustalık Profili (Mastery):", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = hasLiquidCourage,
                                onCheckedChange = { hasLiquidCourage = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00BFFF))
                            )
                            Text("Liquid Courage (+30% Saldırı, +5% PI)", color = Color.White, fontSize = 11.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = hasDoubleEdge,
                                onCheckedChange = { hasDoubleEdge = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00BFFF))
                            )
                            Text("Double Edge (+30% Saldırı, +5% PI)", color = Color.White, fontSize = 11.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = hasClassMastery,
                                onCheckedChange = { hasClassMastery = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00BFFF))
                            )
                            Text("Sınıf Ustalıkları (Class Masteries) (+5% PI)", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }

            // ===== CLASS BADGE + PRESTIGE =====
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBox("Prestij", "$dynamicPrestige", Color(0xFFFFD700))
                        StatBox("Saldırı", "$dynamicAttack", Color(0xFFF44336))
                        StatBox("Can", "$dynamicHealth", Color(0xFF4CAF50))
                    }
                }
            }

            // ===== TAB SELECTOR =====
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF0D1117),
                    contentColor = Color(0xFF00BFFF),
                    edgePadding = 16.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    color = if (selectedTab == index) Color(0xFF00BFFF) else Color.Gray
                                )
                            }
                        )
                    }
                }
            }

            // ===== TAB CONTENT =====
            when (selectedTab) {
                0 -> {
                    // Stats Tab — MCOC.gg style with ranks
                    item { SectionTitle("Relative Max Base Stats") }
                    item {
                        StatsCardWithRanks(champion, dynamicPrestige, dynamicAttack, dynamicHealth)
                    }
                    item { SectionTitle("Stat Focus") }
                    item {
                        StatFocusCard(champion)
                    }
                    item { SectionTitle("Bağışıklıklar & Dirençler") }
                    item {
                        AbilityGridSection(
                            items = champion.immunities.ifEmpty { listOf("Bağışıklık yok") },
                            color = Color(0xFF4CAF50)
                        )
                    }
                    item { SectionTitle("Reacts To") }
                    item {
                        AbilityGridSection(
                            items = champion.reactsTo.ifEmpty { listOf("Bilgi yok") },
                            color = Color(0xFFFF9800)
                        )
                    }
                    item { SectionTitle("Bu Yetenekleri Engeller (Counters)") }
                    item {
                        AbilityGridSection(
                            items = champion.counterAbilities.ifEmpty { listOf("Bilgi yok") },
                            color = Color(0xFFF44336)
                        )
                    }
                    // ===== STRONG MATCHUPS =====
                    if (champion.strongMatchups.isNotEmpty()) {
                        item { SectionTitle("Strong Matchups") }
                        item {
                            ChampionPortraitRow(
                                championIds = champion.strongMatchups,
                                label = "Bu şampiyonun iyi geldiği savunmacılar",
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                    // ===== STRONG COUNTERS =====
                    if (champion.strongCounters.isNotEmpty()) {
                        item { SectionTitle("Strong Counters") }
                        item {
                            ChampionPortraitRow(
                                championIds = champion.strongCounters,
                                label = "Bu şampiyonu counter eden saldırganlar",
                                color = Color(0xFFF44336)
                            )
                        }
                    }
                    item { SectionTitle("Etiketler") }
                    item {
                        TagsRow(items = champion.tags, color = Color(0xFF9E9E9E))
                    }
                }
                1 -> {
                    // Abilities Tab — Grid layout like MCOC.gg
                    item { SectionTitle("Yetenekler (Açıklama için tıklayın)") }
                    item {
                        if (isDetailsLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF00BFFF))
                            }
                        } else {
                            val details = championDetails
                            if (details != null) {
                                AbilitiesGrid(champion, details.abilityDetails)
                            }
                        }
                    }
                    item { SectionTitle("İmza Yeteneği (Signature Ability)") }
                    item {
                        if (isDetailsLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF00BFFF))
                            }
                        } else {
                            InfoCard("Uyanış Yeteneği", championDetails?.signatureAbility ?: "Uyanış yeteneği bulunamadı.")
                        }
                    }
                    item { SectionTitle("Önerilen Andaçlar") }
                    item {
                        TagsRow(items = champion.recommendedRelics, color = Color(0xFF00BCD4))
                    }
                }
                2 -> {
                    // Synergies Tab
                    item { SectionTitle("Sinerjiler (Verdikleri)") }
                    items(champion.synergies) { synergy ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Try to find partner champion portrait
                                val partnerChamp = ChampionRepository.champions.find {
                                    it.name.equals(synergy.partnerName, ignoreCase = true)
                                }
                                if (partnerChamp != null) {
                                    ChampionMiniPortrait(partnerChamp)
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00BFFF).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            synergy.partnerName.take(2).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00BFFF),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(synergy.partnerName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                    Text(synergy.bonus, color = Color(0xFF81C784), fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    if (champion.synergies.isEmpty()) {
                        item {
                            Text(
                                "Sinerji bilgisi henüz eklenmedi.",
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    
                    // Inbound Synergies (Ters sinerji araması)
                    item { SectionTitle("Bu Karakterden Sinerji Alanlar (Alınanlar)") }
                    val inbound = getInboundSynergies(champion)
                    items(inbound) { (partner, bonus) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChampionMiniPortrait(partner)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(partner.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                    Text(bonus, color = Color(0xFF4CAF50), fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    if (inbound.isEmpty()) {
                        item {
                            Text(
                                "Bu karaktere sinerji veren başka şampiyon bulunamadı.",
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                3 -> {
                    // Guide Tab
                    item { SectionTitle("Nasıl Oynanır?") }
                    item {
                        if (isDetailsLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF00BFFF))
                            }
                        } else {
                            InfoCard("Oynanış Rehberi", championDetails?.howToPlay ?: "Oynanış rehberi bulunamadı.")
                        }
                    }
                    item { SectionTitle("En İyi Kullanım Alanı") }
                    item {
                        if (isDetailsLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF00BFFF))
                            }
                        } else {
                            InfoCard("Tavsiye", championDetails?.bestUse ?: "Tavsiye bilgisi bulunamadı.")
                        }
                    }
                    item { SectionTitle("Odak Alanları") }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FocusCard("Saldırı Odağı", champion.focusAttack, Color(0xFFF44336), Modifier.weight(1f))
                            FocusCard("Savunma Odağı", champion.focusDefense, Color(0xFF2196F3), Modifier.weight(1f))
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // ===== PREV / NEXT NAVIGATION BAR =====
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            shape = RoundedCornerShape(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PREV button
                TextButton(
                    onClick = {
                        if (displayIndex > 0) {
                            displayIndex--
                            selectedTab = 0
                        }
                    },
                    enabled = displayIndex > 0
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Önceki",
                        tint = if (displayIndex > 0) Color(0xFF00BFFF) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "ÖNCEKİ",
                        color = if (displayIndex > 0) Color(0xFF00BFFF) else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Champion counter
                Text(
                    "${displayIndex + 1} / ${allChampions.size}",
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                // NEXT button
                TextButton(
                    onClick = {
                        if (displayIndex < allChampions.size - 1) {
                            displayIndex++
                            selectedTab = 0
                        }
                    },
                    enabled = displayIndex < allChampions.size - 1
                ) {
                    Text(
                        "SONRAKİ",
                        color = if (displayIndex < allChampions.size - 1) Color(0xFF00BFFF) else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Sonraki",
                        tint = if (displayIndex < allChampions.size - 1) Color(0xFF00BFFF) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ========== STATS CARD WITH RANKS (MCOC.gg style) ==========
@Composable
fun StatsCardWithRanks(champion: Champion, prestige: Int, attack: Int, health: Int) {
    val totalChamps = ChampionRepository.champions.size
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatBarWithRank("Prestij", prestige.toFloat(), 50000f, Color(0xFFFFD700), champion.prestigeRank, totalChamps)
            StatBarWithRank("Saldırı", attack.toFloat(), 15000f, Color(0xFFF44336), champion.attackRank, totalChamps)
            StatBarWithRank("Can", health.toFloat(), 200000f, Color(0xFF4CAF50), champion.healthRank, totalChamps)
            StatBarWithRank("Kritik Oranı", champion.critRate.toFloat(), 40f, Color(0xFFFF9800), champion.critRateRank, totalChamps)
            StatBarWithRank("Kritik Hasar", champion.critDamage.toFloat(), 200f, Color(0xFFE91E63), champion.critDamageRank, totalChamps)
            StatBarWithRank("Zırh", champion.armor.toFloat(), 40f, Color(0xFF2196F3), champion.armorRank, totalChamps)
            StatBarWithRank("Blok Becerisi", champion.blockProficiency.toFloat(), 80f, Color(0xFF009688), champion.blockProficiencyRank, totalChamps)
        }
    }
}

@Composable
fun StatBarWithRank(label: String, value: Float, maxValue: Float, color: Color, rank: Int, total: Int) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 12.sp, color = Color.LightGray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Progress bar
                LinearProgressIndicator(
                    progress = { (value / maxValue).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .width(120.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color,
                    trackColor = Color(0xFF30363D)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Rank badge
                if (rank > 0) {
                    Text(
                        "#$rank",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            rank <= 10 -> Color(0xFFFFD700)
                            rank <= 50 -> Color(0xFF00BFFF)
                            rank <= 100 -> Color(0xFF4CAF50)
                            else -> Color.Gray
                        }
                    )
                }
            }
        }
    }
}

// ========== STAT FOCUS CARD ==========
@Composable
fun StatFocusCard(champion: Champion) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚔️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saldırı", fontSize = 13.sp, color = Color.LightGray)
                Spacer(modifier = Modifier.weight(1f))
                Text(champion.focusAttack, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF44336))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🛡️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Savunma", fontSize = 13.sp, color = Color.LightGray)
                Spacer(modifier = Modifier.weight(1f))
                Text(champion.focusDefense, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
            }
        }
    }
}

// ========== ABILITIES GRID (MCOC.gg style) ==========
@Composable
fun AbilitiesGrid(champion: Champion, abilityDetails: Map<String, String>) {
    val abilities = champion.abilities.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    var selectedAbility by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val rows = abilities.chunked(2)
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { ability ->
                            val isSelected = selectedAbility == ability
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedAbility = if (isSelected) null else ability
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF00BFFF).copy(alpha = 0.2f) else Color(0xFF0D1117)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        getAbilityEmoji(ability),
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        ability,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color(0xFF00BFFF) else Color.White,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Expandable Detail Card
        val ability = selectedAbility
        AnimatedVisibility (visible = ability != null) {
            if (ability != null) {
                val desc = abilityDetails[ability] ?: "Yetenek açıklaması bulunamadı."
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2333)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(getAbilityEmoji(ability), fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(ability, fontWeight = FontWeight.Bold, color = Color(0xFF00BFFF), fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(desc, color = Color.LightGray, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

// ========== ABILITY GRID SECTION (for immunities, counters, reactsTo) ==========
@Composable
fun AbilityGridSection(items: List<String>, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val rows = items.chunked(2)
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    getAbilityEmoji(item),
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    item,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = color,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ========== CHAMPION PORTRAIT ROW (Strong Matchups/Counters) ==========
@Composable
fun ChampionPortraitRow(championIds: List<String>, label: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(championIds) { champId ->
                    val champ = ChampionRepository.champions.find { it.id == champId }
                    if (champ != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(60.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                ChampionMiniPortrait(champ)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                champ.name,
                                fontSize = 9.sp,
                                color = Color.White,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ========== HELPER: Ability Emoji Mapper ==========
fun getAbilityEmoji(ability: String): String {
    return when (ability.lowercase().trim()) {
        "bleed" -> "🩸"
        "poison" -> "☠️"
        "incinerate" -> "🔥"
        "shock" -> "⚡"
        "coldsnap" -> "❄️"
        "stun" -> "💫"
        "armor break", "armor shattered" -> "🔨"
        "armor up" -> "🛡️"
        "fury" -> "💢"
        "regeneration", "regen" -> "💚"
        "power drain", "power burn", "power steal" -> "🔋"
        "power gain" -> "⚡"
        "power lock" -> "🔒"
        "heal block" -> "🚫"
        "fate seal" -> "🔮"
        "nullify" -> "✨"
        "stagger" -> "💥"
        "evade" -> "💨"
        "auto-block" -> "🤖"
        "true strike" -> "🎯"
        "true accuracy" -> "🎯"
        "unstoppable" -> "🚀"
        "indestructible" -> "🛡️"
        "unblockable" -> "⚔️"
        "prowess" -> "🌟"
        "precision" -> "🎯"
        "cruelty" -> "💀"
        "miss" -> "👻"
        "phase" -> "👻"
        "slow" -> "🐌"
        "petrify" -> "🪨"
        "concussion" -> "🌀"
        "exhaustion" -> "😓"
        "weakness" -> "📉"
        "degeneration", "degen" -> "☣️"
        "limbo" -> "🌀"
        "glancing" -> "🛡️"
        "physical resistance" -> "💪"
        "critical hits" -> "🎯"
        "damaging effects" -> "💥"
        "debuffs" -> "⬇️"
        "energy damage" -> "⚡"
        "intimidate" -> "😨"
        "fragility" -> "💔"
        "crush" -> "🔨"
        "daunted" -> "😰"
        "neurotoxin" -> "🕷️"
        "rupture" -> "🩸"
        "disorient" -> "🌀"
        "death immunity" -> "💀"
        "immortality" -> "♾️"
        "invisible" -> "👁️"
        "pursuit" -> "🏃"
        "reinforce" -> "🔧"
        "reverb" -> "🔊"
        "wither" -> "🍂"
        "battered" -> "🤕"
        "berserk" -> "😤"
        "direct damage" -> "💥"
        "combo detonation" -> "💣"
        "passive stun" -> "💫"
        "organic magnetism" -> "🧲"
        "delirium" -> "🌀"
        "reverse controls" -> "🔄"
        "aspect of evolution" -> "🧬"
        "devolve" -> "🔄"
        "purify" -> "✨"
        "signature ability" -> "📜"
        "fear" -> "😱"
        "falter" -> "💫"
        "agility" -> "🏃"
        "aptitude" -> "📊"
        "amplify" -> "📢"
        "atrophy" -> "📉"
        "cauterize" -> "🔥"
        "energize" -> "⚡"
        "guaranteed crits" -> "💎"
        "inexorable" -> "🚀"
        "mystic dispersion" -> "🔮"
        "power sting" -> "🐝"
        else -> "⚡"
    }
}

// ========== EXISTING COMPONENTS (kept) ==========

@Composable
fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF00BFFF),
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun TagsRow(items: List<String>, color: Color) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowItems.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(tag, fontSize = 11.sp, color = color)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun InfoCard(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800), fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(content, color = Color.LightGray, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
fun FocusCard(title: String, content: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = color, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(content, color = Color.LightGray, fontSize = 12.sp)
        }
    }
}
