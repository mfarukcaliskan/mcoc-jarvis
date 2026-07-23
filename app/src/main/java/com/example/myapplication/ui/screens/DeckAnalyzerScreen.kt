package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.data.Champion
import com.example.myapplication.data.ChampionClass
import com.example.myapplication.data.ChampionRepository
import com.example.myapplication.data.MetaRepository

@Composable
fun DeckAnalyzerScreen() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("bg_deck_prefs", android.content.Context.MODE_PRIVATE) }
    
    // Load saved deck IDs
    val initialDeck = remember {
        val savedIds = sharedPrefs.getString("deck_ids", "") ?: ""
        val list = MutableList<Champion?>(30) { null }
        if (savedIds.isNotEmpty()) {
            val ids = savedIds.split(",")
            ids.forEachIndexed { index, id ->
                if (index < 30 && id.isNotEmpty() && id != "null") {
                    list[index] = ChampionRepository.champions.find { it.id == id }
                }
            }
        }
        list
    }

    // 30 champion slots
    var deckList by remember { mutableStateOf<List<Champion?>>(initialDeck) }
    var showSelectionDialog by remember { mutableStateOf(false) }
    var selectedSlotIndex by remember { mutableStateOf(-1) }
    var dialogSearchQuery by remember { mutableStateOf("") }

    // Save deck IDs when deckList changes
    LaunchedEffect(deckList) {
        val idsString = deckList.joinToString(",") { it?.id ?: "null" }
        sharedPrefs.edit().putString("deck_ids", idsString).apply()
    }

    // Modal Champion Selection Dialog
    if (showSelectionDialog) {
        Dialog(onDismissRequest = { showSelectionDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Şampiyon Seç (Slot ${selectedSlotIndex + 1})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00BFFF),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Search field inside dialog
                    OutlinedTextField(
                        value = dialogSearchQuery,
                        onValueChange = { dialogSearchQuery = it },
                        placeholder = { Text("Şampiyon ara...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF00BFFF)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00BFFF),
                            unfocusedBorderColor = Color(0xFF30363D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredChamps = ChampionRepository.champions.filter { champ ->
                        champ.name.lowercase().contains(dialogSearchQuery.lowercase()) &&
                                champ !in deckList.filterNotNull() // prevent duplicate picks
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredChamps) { champ ->
                            val drawableName = champ.id.lowercase().replace("-", "_").let { if (it[0].isDigit() || it == "void") "img_$it" else it }
                            val imageResId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0D1117), RoundedCornerShape(10.dp))
                                    .clickable {
                                        val newList = deckList.toMutableList()
                                        newList[selectedSlotIndex] = champ
                                        deckList = newList
                                        showSelectionDialog = false
                                        dialogSearchQuery = ""
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (imageResId != 0) {
                                    Image(
                                        painter = painterResource(id = imageResId),
                                        contentDescription = champ.name,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(champ.mcocClass.color).copy(alpha = 0.15f))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.Gray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(champ.name.take(2).uppercase(), color = Color.White, fontSize = 12.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(champ.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(champ.mcocClass.displayName, color = Color(champ.mcocClass.color), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117)),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "🃏 BG DESTE ANALİSTİ",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00BFFF)
            )
            Text(
                text = "Savaş Alanları için 30 şampiyonluk destenizin röntgenini çekin.",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
        }

        // 30 Slots grid
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Desteniz (${deckList.count { it != null }} / 30)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (deckList.any { it != null }) {
                            TextButton(onClick = { deckList = List(30) { null } }) {
                                Text("Desteyi Boşalt", color = Color(0xFFF44336), fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(deckList) { index, champ ->
                            if (champ != null) {
                                val drawableName = champ.id.lowercase().replace("-", "_").let { if (it[0].isDigit() || it == "void") "img_$it" else it }
                                val imageResId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0D1117))
                                        .border(1.dp, Color(champ.mcocClass.color), RoundedCornerShape(8.dp))
                                        .clickable {
                                            val newList = deckList.toMutableList()
                                            newList[index] = null
                                            deckList = newList
                                        }
                                ) {
                                    if (imageResId != 0) {
                                        Image(
                                            painter = painterResource(id = imageResId),
                                            contentDescription = champ.name,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(champ.name.take(2).uppercase(), color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                    // small remove overlay icon
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(14.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Clear,
                                            contentDescription = "Kaldır",
                                            tint = Color.Red,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0D1117))
                                        .border(1.dp, Color(0xFF30363D), RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedSlotIndex = index
                                            showSelectionDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = Color(0xFF00BFFF), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Analysis Result Panel
        val occupiedCount = deckList.count { it != null }
        if (occupiedCount == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
                ) {
                    Text(
                        text = "Analiz raporunu görmek için destenize şampiyon ekleyin.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
            }
        } else {
            val nonNullDeck = deckList.filterNotNull()

            // 1. Sınıf Dağılımı Analizi
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "📊 Deste Sınıf Dağılımı",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val total = nonNullDeck.size.toFloat()
                        ChampionClass.entries.forEach { cls ->
                            val count = nonNullDeck.count { it.mcocClass == cls }
                            val percentage = if (total > 0) count / total else 0f
                            val displayPercent = (percentage * 100).toInt()

                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cls.displayName, fontSize = 12.sp, color = Color(cls.color), fontWeight = FontWeight.Bold)
                                    Text("$count şampiyon (%$displayPercent)", fontSize = 11.sp, color = Color.LightGray)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                LinearProgressIndicator(
                                    progress = { percentage },
                                    color = Color(cls.color),
                                    trackColor = Color(0xFF0D1117),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // 2. Mekanik Eksiklik Raporu (Evade, Auto-Block, Unstoppable, Immunities)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "🛡️ Mekanik ve Zayıflık Röntgeni",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Evade Counters
                        val hasEvadeCounter = nonNullDeck.any { champ ->
                            val abLower = champ.abilities.lowercase()
                            abLower.contains("slow") || abLower.contains("true strike") || abLower.contains("true accuracy")
                        }
                        TacticalIndicator(
                            title = "Kaçış (Evade) Counter Mekaniği",
                            isValid = hasEvadeCounter,
                            validText = "Yeterli kaçış karşıtı yeteneğiniz var (Slow/True Strike).",
                            invalidText = "Destedeki kaçış (Evade) karşıtı şampiyonlar eksik! (Örn: Shang-Chi, Falcon, Slow yetenekleri)"
                        )

                        // Auto-Block Counters
                        val hasAutoBlockCounter = nonNullDeck.any { champ ->
                            val abLower = champ.abilities.lowercase()
                            abLower.contains("unblockable") || abLower.contains("armor break") || abLower.contains("true strike")
                        }
                        TacticalIndicator(
                            title = "Otomatik Blok Counter Mekaniği",
                            isValid = hasAutoBlockCounter,
                            validText = "Otomatik Blok kırıcı yeteneğiniz var (Armor Break/Unblockable).",
                            invalidText = "Otomatik Blok karşıtı şampiyon yetersiz! (Örn: Hulkling, Hercules)"
                        )

                        // Unstoppable Counters
                        val hasUnstoppableCounter = nonNullDeck.any { champ ->
                            val abLower = champ.abilities.lowercase()
                            abLower.contains("slow") || abLower.contains("stagger") || abLower.contains("nullify")
                        }
                        TacticalIndicator(
                            title = "Durdurulamaz (Unstoppable) Karşıtı",
                            isValid = hasUnstoppableCounter,
                            validText = "Durdurulamaz mod kırıcı yeteneğiniz var (Slow/Stagger/Nullify).",
                            invalidText = "Durdurulamaz karşıtı yetenekler eksik! (Örn: Spider-Man 2099, Doom)"
                        )

                        // Immunities
                        val hasImmunity = nonNullDeck.any { champ ->
                            champ.immunities.any { it.lowercase().contains("bleed") || it.lowercase().contains("poison") }
                        }
                        TacticalIndicator(
                            title = "Bağışıklık (Bleed/Poison Immune)",
                            isValid = hasImmunity,
                            validText = "Kanama veya Zehir bağışıklığına sahip tanklar mevcut.",
                            invalidText = "Zehir/Kanama bağışık tank eksiğiniz var! (Örn: Warlock, Nimrod, Colossus)"
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // 3. Sezon Meta Uyumluluk Oranı
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2333))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "🏆 Sezon 40 Meta Uyumluluk Raporu",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Match recomended attackers from Season 40
                        val s40Season = MetaRepository.seasons.find { it.id == "bg_s40" }
                        val recommendedIds = s40Season?.nodes?.flatMap { it.bestAttackers }?.distinct() ?: emptyList()
                        
                        val matchedAttackerCount = nonNullDeck.count { it.id in recommendedIds }
                        val matchRatio = if (recommendedIds.isNotEmpty()) {
                            (matchedAttackerCount.toFloat() / recommendedIds.size.toFloat()).coerceAtMost(1f)
                        } else 0f
                        
                        val matchPercent = (matchRatio * 100).toInt()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(64.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { matchRatio },
                                    color = Color(0xFF4CAF50),
                                    trackColor = Color(0xFF0D1117),
                                    strokeWidth = 6.dp,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Text(
                                    "%$matchPercent",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Meta Uyumluluğu: %$matchPercent",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (matchPercent >= 70) {
                                        "Harika! Desteniz Sezon 40 karolarına (I Am Root, Daunting Doom) karşı mükemmel cevaplara sahip."
                                    } else if (matchPercent >= 40) {
                                        "Orta Seviye. Sezon meta şampiyonlarından (Hercules, Doom, Torch) birkaçını daha destenize eklemenizi öneririz."
                                    } else {
                                        "Düşük Uyumluluk. Metadaki Groot/Doom/Spite karolarına karşı counter şampiyonlarınız yetersiz!"
                                    },
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TacticalIndicator(title: String, isValid: Boolean, validText: String, invalidText: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isValid) Color(0xFF4CAF50) else Color(0xFFF44336))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (isValid) validText else invalidText,
            color = if (isValid) Color.LightGray else Color(0xFFFFB74D),
            fontSize = 11.sp,
            lineHeight = 15.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
