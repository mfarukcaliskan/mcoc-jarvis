package com.example.myapplication.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuestNavigatorScreen() {
    val context = LocalContext.current
    var selectedActId by remember { mutableStateOf(6) }
    var selectedQuestId by remember { mutableStateOf<String?>(null) }
    var selectedPathLetter by remember { mutableStateOf("A") }
    var questDetail by remember { mutableStateOf<QuestMap?>(null) }
    var isLoadingDetail by remember { mutableStateOf(false) }

    // Load saved BG Deck for recommendations
    val userDeck = remember {
        val sharedPrefs = context.getSharedPreferences("bg_deck_prefs", Context.MODE_PRIVATE)
        val savedIds = sharedPrefs.getString("deck_ids", "") ?: ""
        val deckIds = savedIds.split(",").filter { it.isNotBlank() && it != "null" }
        ChampionRepository.champions.filter { it.id in deckIds }
    }

    // Load quest details when selectedQuestId changes
    LaunchedEffect(selectedQuestId) {
        if (selectedQuestId != null) {
            isLoadingDetail = true
            // Run on IO thread
            val result = QuestRepository.loadQuestMap(context, selectedQuestId!!)
            questDetail = result
            // Reset selected path to Easiest path or A
            selectedPathLetter = result?.paths?.firstOrNull { it.isEasiest }?.pathLetter 
                ?: result?.paths?.firstOrNull()?.pathLetter 
                ?: "A"
            isLoadingDetail = false
        } else {
            questDetail = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Sleek slate-900 background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Title
            Text(
                text = "Macera Geçme Rehberi",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Act selection buttons (Act 6, 7, 8)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestRepository.acts.forEach { act ->
                    val isSelected = act.id == selectedActId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Brush.horizontalGradient(
                                    listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)) // Cyberpunk pink/purple gradient
                                ) else Brush.horizontalGradient(
                                    listOf(Color(0xFF1E293B), Color(0xFF1E293B))
                                )
                            )
                            .clickable {
                                selectedActId = act.id
                                selectedQuestId = null
                                questDetail = null
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = act.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Main Layout: Split list or single column based on selection
            if (selectedQuestId == null) {
                // Show Chapters and Quests list
                val activeAct = QuestRepository.acts.firstOrNull { it.id == selectedActId }
                if (activeAct != null) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(activeAct.chapters) { chapter ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = chapter.name,
                                        color = Color(0xFFF472B6), // Light Pink Accent
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Divider(color = Color(0xFF334155), thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))
                                    
                                    chapter.quests.forEach { quest ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedQuestId = quest.id }
                                                .padding(vertical = 12.dp, horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = quest.name,
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "Rehberi Gör →",
                                                color = Color(0xFF38BDF8), // Blue Accent
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Quest details is open
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "← Harita Listesine Dön",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { selectedQuestId = null }
                    )
                }

                if (isLoadingDetail) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFEC4899))
                    }
                } else {
                    questDetail?.let { quest ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Title & Global Nodes card
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = quest.name,
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        if (quest.globalNodes.isNotEmpty()) {
                                            Text(
                                                text = "Global Karolar (Harita Geneli)",
                                                color = Color(0xFFF472B6),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            quest.globalNodes.forEach { node ->
                                                Text(
                                                    text = "⚠️ $node",
                                                    color = Color(0xFFCBD5E1),
                                                    fontSize = 13.sp,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Path Picker & Easiest indicator
                            item {
                                Text(
                                    text = "Yol Seçimi",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    quest.paths.forEach { path ->
                                        val isSelected = path.pathLetter == selectedPathLetter
                                        val isEasiest = path.isEasiest
                                        
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) Color(0xFFEC4899)
                                                    else Color(0xFF334155)
                                                )
                                                .clickable { selectedPathLetter = path.pathLetter }
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Yol ${path.pathLetter}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (isEasiest) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFF22C55E), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "En Kolay",
                                                            color = Color.White,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Path Details & Node Analysis
                            val currentPath = quest.paths.firstOrNull { it.pathLetter == selectedPathLetter }
                            if (currentPath != null) {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Yol ${currentPath.pathLetter} Karoları",
                                                color = Color(0xFFEC4899),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            if (currentPath.pathNodes.isEmpty()) {
                                                Text("Spesifik karo bulunmuyor.", color = Color.Gray, fontSize = 13.sp)
                                            } else {
                                                currentPath.pathNodes.forEach { node ->
                                                    Text(text = "• $node", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Yoldaki Şampiyonlar",
                                                color = Color(0xFFEC4899),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                items(currentPath.defenders) { defId ->
                                                    val matchedChamp = ChampionRepository.champions.firstOrNull { it.id == defId }
                                                    val name = matchedChamp?.name ?: defId.replace("_", " ").capitalize()
                                                    val color = when (matchedChamp?.mcocClass) {
                                                        ChampionClass.SCIENCE -> Color(0xFF22C55E)
                                                        ChampionClass.MYSTIC -> Color(0xFF8B5CF6)
                                                        ChampionClass.COSMIC -> Color(0xFF38BDF8)
                                                        ChampionClass.TECH -> Color(0xFF0EA5E9)
                                                        ChampionClass.MUTANT -> Color(0xFFEAB308)
                                                        ChampionClass.SKILL -> Color(0xFFEF4444)
                                                        else -> Color.Gray
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(text = name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Matchup Engine Recommendation
                                val analysis = QuestAnalyzer.analyzePath(currentPath, userDeck)
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "🤖 Akıllı Yol Analizi",
                                                color = Color(0xFF38BDF8),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = analysis.reason,
                                                color = Color(0xFFCBD5E1),
                                                fontSize = 13.sp
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Destenizden Önerilen Şampiyonlar",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))

                                            if (analysis.matchedChampions.isEmpty()) {
                                                Text(
                                                    text = "⚠️ Destenizde bu yoldaki karolara (bağışıklık veya yetenek) doğrudan cevap veren şampiyon yok veya desteniz boş. BG Deste sayfasından destenizi güncelleyin.",
                                                    color = Color(0xFFEAB308),
                                                    fontSize = 12.sp
                                                )
                                            } else {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    analysis.matchedChampions.forEach { champ ->
                                                        val color = when (champ.mcocClass) {
                                                            ChampionClass.SCIENCE -> Color(0xFF22C55E)
                                                            ChampionClass.MYSTIC -> Color(0xFF8B5CF6)
                                                            ChampionClass.COSMIC -> Color(0xFF38BDF8)
                                                            ChampionClass.TECH -> Color(0xFF0EA5E9)
                                                            ChampionClass.MUTANT -> Color(0xFFEAB308)
                                                            ChampionClass.SKILL -> Color(0xFFEF4444)
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(color)
                                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                        ) {
                                                            Text(
                                                                text = champ.name,
                                                                color = Color.Black,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Boss Section
                            item {
                                val bossChamp = ChampionRepository.champions.firstOrNull { it.id == quest.boss.championId }
                                val bossName = bossChamp?.name ?: quest.boss.championId.replace("_", " ").capitalize()
                                val bossClassColor = when (bossChamp?.mcocClass) {
                                    ChampionClass.SCIENCE -> Color(0xFF22C55E)
                                    ChampionClass.MYSTIC -> Color(0xFF8B5CF6)
                                    ChampionClass.COSMIC -> Color(0xFF38BDF8)
                                    ChampionClass.TECH -> Color(0xFF0EA5E9)
                                    ChampionClass.MUTANT -> Color(0xFFEAB308)
                                    ChampionClass.SKILL -> Color(0xFFEF4444)
                                    else -> Color.Gray
                                }

                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "👑 Harita Sonu Boss: $bossName",
                                                color = Color.White,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .background(bossClassColor, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = bossChamp?.mcocClass?.name ?: "BİLİNMEYEN",
                                                    color = Color.Black,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Özel Boss Karoları:",
                                            color = Color(0xFFF472B6),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        quest.boss.bossNodes.forEach { node ->
                                            Text(text = "🔥 $node", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Önerilen En İyi Karşıtlar (Boss Counters):",
                                            color = Color(0xFFF472B6),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                        ) {
                                            items(quest.boss.idealCounters) { counterId ->
                                                val counterChamp = ChampionRepository.champions.firstOrNull { it.id == counterId }
                                                val name = counterChamp?.name ?: counterId.replace("_", " ").capitalize()
                                                val isOwn = userDeck.any { it.id == counterId }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .border(
                                                            width = if (isOwn) 2.dp else 0.dp,
                                                            color = if (isOwn) Color(0xFF22C55E) else Color.Transparent,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .background(Color(0xFF334155), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        if (isOwn) {
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(text = "✔️", fontSize = 10.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (quest.boss.idealCounters.any { id -> userDeck.any { it.id == id } }) {
                                            Text(
                                                text = "💡 Harika! Destenizdeki boss karşıtı şampiyonu dövüş sonuna kadar saklamayı unutmayın!",
                                                color = Color(0xFF22C55E),
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(top = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
