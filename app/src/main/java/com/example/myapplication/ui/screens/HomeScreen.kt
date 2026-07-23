package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.data.Champion
import com.example.myapplication.data.ChampionClass
import com.example.myapplication.data.ChampionRepository

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Counter Bulucu", "Prestij Hesaplayıcı")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        Text(
            text = "JARVIS ASİSTAN",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BFFF),
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF161B22),
            contentColor = Color(0xFF00BFFF),
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> MatchupCounterScreen()
            1 -> PrestigeCalculatorScreen()
        }
    }
}

@Composable
fun MatchupCounterScreen() {
    var showDialog by remember { mutableStateOf(false) }
    var selectedDefender by remember { mutableStateOf<Champion?>(null) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Savaş Alanları Counter Bulucu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00BFFF), modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Color(0xFF0D1117), RoundedCornerShape(12.dp)).clickable { showDialog = true }.padding(12.dp), contentAlignment = Alignment.Center) {
                    if (selectedDefender == null) {
                        Text("Savunmacı Seçmek İçin Dokunun...", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            val context = LocalContext.current
                            val drawableName = selectedDefender!!.id.lowercase().replace("-", "_").let { if (it[0].isDigit() || it == "void") "img_$it" else it }
                            val imageResId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                            if (imageResId != 0) {
                                Image(painter = painterResource(id = imageResId), contentDescription = selectedDefender!!.name, modifier = Modifier.size(48.dp).clip(CircleShape))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(selectedDefender!!.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Text(selectedDefender!!.mcocClass.displayName, color = Color(selectedDefender!!.mcocClass.color), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (selectedDefender != null) {
            Text("Önerilen Karşı (Counter) Şampiyonlar:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val counters = getCountersFor(selectedDefender!!)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(counters) { (counter, reason) ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            val context = LocalContext.current
                            val drawableName = counter.id.lowercase().replace("-", "_").let { if (it[0].isDigit() || it == "void") "img_$it" else it }
                            val imageResId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                            if (imageResId != 0) {
                                Image(painter = painterResource(id = imageResId), contentDescription = counter.name, modifier = Modifier.size(48.dp).clip(CircleShape))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(counter.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text(reason, color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showDialog) {
        ChampionSearchDialog(onDismiss = { showDialog = false }, onSelect = { selectedDefender = it; showDialog = false })
    }
}

@Composable
fun calculateDynamicPrestige(champion: Champion, star: Int, rank: Int, sig: Int): Int {
    val starProg = champion.progressions.find { it.starRating == star } ?: champion.progressions.firstOrNull()
    if (starProg == null) return champion.prestige
    val rankStat = starProg.ranks.find { it.rank == rank } ?: starProg.ranks.firstOrNull()
    if (rankStat == null) return champion.prestige

    val maxSig = 200.0
    val factor = Math.pow(sig.toDouble() / maxSig, 0.8)
    val dynamicPrestige = rankStat.basePrestige + (rankStat.maxPrestige - rankStat.basePrestige) * factor
    return dynamicPrestige.toInt()
}

@Composable
fun PrestigeCalculatorScreen() {
    var showDialogSlotIndex by remember { mutableIntStateOf(-1) }
    val selectedChampions = remember { mutableStateListOf<Champion?>(null, null, null, null, null) }
    val selectedStars = remember { mutableStateListOf(6, 6, 6, 6, 6) }
    val selectedRanks = remember { mutableStateListOf(5, 5, 5, 5, 5) }
    val selectedSigs = remember { mutableStateListOf(0, 0, 0, 0, 0) }
    val expandedSlots = remember { mutableStateListOf(false, false, false, false, false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Prestij (Prestige) Hesaplayıcı", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00BFFF))
                Spacer(modifier = Modifier.height(12.dp))
                
                for (i in 0 until 5) {
                    val champ = selectedChampions[i]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFF0D1117), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDialogSlotIndex = i },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "${i + 1}.", color = Color(0xFF00BFFF), fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                            if (champ == null) {
                                Text("Şampiyon Seç...", color = Color.Gray, fontSize = 13.sp)
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val context = LocalContext.current
                                val drawableName = champ.id.lowercase().replace("-", "_").let { if (it[0].isDigit() || it == "void") "img_$it" else it }
                                val imageResId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

                                if (imageResId != 0) {
                                    Image(
                                        painter = painterResource(id = imageResId),
                                        contentDescription = champ.name,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Column {
                                    Text(champ.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Text("${selectedStars[i]}★ R${selectedRanks[i]} Sig ${selectedSigs[i]}", color = Color.Gray, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                val dynamicP = calculateDynamicPrestige(champ, selectedStars[i], selectedRanks[i], selectedSigs[i])
                                Text("$dynamicP", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(
                                    onClick = { expandedSlots[i] = !expandedSlots[i] },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text(if (expandedSlots[i]) "▲" else "▼", color = Color(0xFF00BFFF), fontSize = 12.sp)
                                }
                            }
                        }

                        if (champ != null && expandedSlots[i]) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFF30363D))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Stars Selection (6★ vs 7★)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Yıldız:", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.width(50.dp))
                                listOf(6, 7).forEach { star ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (selectedStars[i] == star) Color(0xFF00BFFF).copy(alpha = 0.2f) else Color(0xFF161B22),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { selectedStars[i] = star }
                                            .padding(horizontal = 12.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${star}★", color = if (selectedStars[i] == star) Color(0xFF00BFFF) else Color.White, fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Ranks Selection (1 to 5)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Rütbe:", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.width(50.dp))
                                (1..5).forEach { r ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (selectedRanks[i] == r) Color(0xFFFF6B35).copy(alpha = 0.2f) else Color(0xFF161B22),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { selectedRanks[i] = r }
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("R$r", color = if (selectedRanks[i] == r) Color(0xFFFF6B35) else Color.White, fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Sig level Selection (0 to 200)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Kopya (Sig Level):", color = Color.Gray, fontSize = 11.sp)
                                    Text("${selectedSigs[i]}", color = Color(0xFF00BFFF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Slider(
                                    value = selectedSigs[i].toFloat(),
                                    onValueChange = { selectedSigs[i] = it.toInt() },
                                    valueRange = 0f..200f,
                                    steps = 199,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF00BFFF),
                                        activeTrackColor = Color(0xFF00BFFF),
                                        inactiveTrackColor = Color(0xFF30363D)
                                    )
                                )
                            }
                        }
                    }
                }
                
                var hasLiquidCourage by remember { mutableStateOf(false) }
                var hasDoubleEdge by remember { mutableStateOf(false) }
                var hasClassMastery by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🛡️ Gelişmiş Ustalık (Mastery) Profili:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    
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

                Spacer(modifier = Modifier.height(8.dp))
                
                // Average Calculation using calculateDynamicPrestige and Mastery modifiers
                val validChamps = selectedChampions.indices.filter { selectedChampions[it] != null }
                val baseAvg = if (validChamps.isNotEmpty()) {
                    validChamps.map { idx ->
                        calculateDynamicPrestige(selectedChampions[idx]!!, selectedStars[idx], selectedRanks[idx], selectedSigs[idx])
                    }.average().toInt()
                } else 0
                
                var prestigeMultiplier = 1.0
                if (hasLiquidCourage) prestigeMultiplier += 0.05
                if (hasDoubleEdge) prestigeMultiplier += 0.05
                if (hasClassMastery) prestigeMultiplier += 0.05
                
                val avgPrestige = (baseAvg * prestigeMultiplier).toInt()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ortalama Prestij (PI):", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text("$avgPrestige", fontWeight = FontWeight.Bold, color = Color(0xFF00BFFF), fontSize = 20.sp)
                }
            }
        }
    }
    if (showDialogSlotIndex != -1) {
        ChampionSearchDialog(
            onDismiss = { showDialogSlotIndex = -1 },
            onSelect = { selectedChampions[showDialogSlotIndex] = it; expandedSlots[showDialogSlotIndex] = true; showDialogSlotIndex = -1 }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChampionSearchDialog(onDismiss: () -> Unit, onSelect: (Champion) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = ChampionRepository.champions.filter { it.name.lowercase().contains(query.lowercase()) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Şampiyon Ara", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("İsim girin...", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00BFFF),
                        unfocusedBorderColor = Color(0xFF30363D),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered) { champ ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(champ) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val context = LocalContext.current
                            val drawableName = champ.id.lowercase().replace("-", "_").let { if (it[0].isDigit() || it == "void") "img_$it" else it }
                            val imageResId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

                            if (imageResId != 0) {
                                Image(
                                    painter = painterResource(id = imageResId),
                                    contentDescription = champ.name,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column {
                                Text(champ.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text(champ.mcocClass.displayName, color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        HorizontalDivider(color = Color(0xFF30363D))
                    }
                }
            }
        }
    }
}

fun getCountersFor(defender: Champion): List<Pair<Champion, String>> {
    val list = mutableListOf<Pair<Champion, String>>()

    // 1. Etiket Tabanlı Dinamik Eşleştirme Kuralları (Metal/Magnetizm, Robot/Robot Counter vb.)
    val isMetal = defender.tags.any { it.lowercase().contains("metal") }
    val isRobot = defender.tags.any { it.lowercase().contains("robot") }
    val isXL = defender.tags.any { it.lowercase().contains("size: xl") }

    // Magnetizma (Magneto vb.) metal şampiyonları doğrudan counter'lar
    if (isMetal) {
        val magnetos = ChampionRepository.champions.filter { it.name.lowercase().contains("magneto") }
        magnetos.forEach { magneto ->
            list.add(magneto to "Magnetizma: Metal rakibin tüm yetenek tetiklenmelerini %100 engeller.")
        }
    }

    // Robot Kırıcılar (Medusa, Warlock, Nebula vb.)
    if (isRobot) {
        val robotCounters = ChampionRepository.champions.filter { 
            it.name.equals("Medusa", ignoreCase = true) || 
            it.name.equals("Warlock", ignoreCase = true) || 
            it.name.equals("Nebula", ignoreCase = true) 
        }
        robotCounters.forEach { counter ->
            list.add(counter to "Robot Kırıcı: Robot rakibin yetenek gücünü ve güç kazanımını kilitler.")
        }
    }

    // Dev Katili (Giant Slayer)
    if (isXL) {
        val giantSlayers = ChampionRepository.champions.filter { 
            it.name.equals("Shang-Chi", ignoreCase = true) || 
            it.name.equals("Hercules", ignoreCase = true) 
        }
        giantSlayers.forEach { counter ->
            list.add(counter to "Dev Katili: XL boyutundaki dev rakiplere karşı ekstra hasar ve kritik darbe vurur.")
        }
    }

    // 2. JSON tabanlı tanımlı doğrudan counter'ları yükle
    defender.strongCounters.forEach { counterId ->
        val champ = ChampionRepository.champions.find { it.id == counterId }
        if (champ != null && list.none { it.first.id == champ.id }) {
            list.add(champ to "${champ.name}, bu savunmacının mekaniklerini doğrudan kilitler.")
        }
    }

    // 3. Akıllı Bağışıklık (Immunity) Filtrelemesi & Yetenek Karşılaştırması
    val isBleedImmune = defender.immunities.any { it.lowercase().contains("bleed") }
    val isPoisonImmune = defender.immunities.any { it.lowercase().contains("poison") }
    
    val hasEvade = defender.abilities.lowercase().contains("evade")
    val hasAutoBlock = defender.abilities.lowercase().contains("auto-block")
    val hasArmorUp = defender.abilities.lowercase().contains("armor up")

    val candidates = ChampionRepository.champions.filter { it.id != defender.id && it.tier in listOf("S", "A") }

    for (candidate in candidates) {
        // Zaten listeye eklenmişse atla
        if (list.any { it.first.id == candidate.id }) continue

        // Bağışıklık kontrolü (savunmacı bağışık olduğu hasar tipine dayanan saldırganı eliyoruz)
        if (isBleedImmune && (candidate.name == "Archangel" || candidate.name == "Wolverine" || candidate.name == "Sabretooth" || candidate.abilities.lowercase().contains("bleed") && !candidate.abilities.lowercase().contains("slow"))) {
            continue
        }
        if (isPoisonImmune && (candidate.name == "Abomination" || candidate.abilities.lowercase().contains("poison"))) {
            continue
        }

        // Evade counter eşleşmesi
        if (hasEvade && (candidate.abilities.lowercase().contains("slow") || candidate.abilities.lowercase().contains("true strike") || candidate.abilities.lowercase().contains("true accuracy"))) {
            list.add(candidate to "Savunmacının Kaçış (Evade) yeteneğini ${candidate.name} kendi Slow/True Strike yeteneğiyle sıfırlar.")
            continue
        }

        // Auto-Block counter eşleşmesi
        if (hasAutoBlock && (candidate.abilities.lowercase().contains("true accuracy") || candidate.abilities.lowercase().contains("unblockable") || candidate.abilities.lowercase().contains("armor break"))) {
            list.add(candidate to "Savunmacının Otomatik Bloğunu (Auto-Block) ${candidate.name} Unblockable/True Accuracy ile aşar.")
            continue
        }

        // Armor Up counter eşleşmesi
        if (hasArmorUp && (candidate.abilities.lowercase().contains("armor break") || candidate.abilities.lowercase().contains("armor shattered"))) {
            list.add(candidate to "Savunmacının yüksek zırh buff'larını ${candidate.name} Zırh Kırma (Armor Break) ile yok eder.")
            continue
        }
    }

    // 4. Sınıf Avantajına dayalı yedek counterler (yukarıdaki filtrelerden geçebildiyse)
    val classAdvantageMap = mapOf(
        ChampionClass.MYSTIC to ChampionClass.SCIENCE,
        ChampionClass.COSMIC to ChampionClass.MYSTIC,
        ChampionClass.TECH to ChampionClass.COSMIC,
        ChampionClass.MUTANT to ChampionClass.TECH,
        ChampionClass.SKILL to ChampionClass.MUTANT,
        ChampionClass.SCIENCE to ChampionClass.SKILL
    )
    val counterClass = classAdvantageMap[defender.mcocClass]
    if (counterClass != null) {
        val classCounters = ChampionRepository.champions.filter {
            it.mcocClass == counterClass && it.tier in listOf("S", "A") && it.id != defender.id
        }
        for (c in classCounters) {
            if (list.size >= 5) break
            if (list.none { it.first.id == c.id }) {
                // Kanama engeli kontrolü
                if (isBleedImmune && c.abilities.lowercase().contains("bleed") && !c.abilities.lowercase().contains("slow")) continue
                list.add(c to "${c.mcocClass.displayName} sınıf avantajı ve yüksek ${c.tier} Tier savaş mekanikleri.")
            }
        }
    }

    return list.distinctBy { it.first.id }.take(5)
}

private fun addCounter(list: MutableList<Pair<Champion, String>>, counterId: String, reason: String) {
    val champ = ChampionRepository.champions.find { it.id == counterId }
    if (champ != null) {
        list.add(champ to reason)
    }
}
