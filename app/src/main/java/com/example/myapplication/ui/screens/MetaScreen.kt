package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.myapplication.data.AwGroup
import com.example.myapplication.data.AwNode
import com.example.myapplication.data.ChampionRepository
import com.example.myapplication.data.BtNode
import com.example.myapplication.data.GuiaAwRepository
import com.example.myapplication.data.GuiaBigThingRepository
import com.example.myapplication.data.MetaRepository
import com.example.myapplication.data.MetaSeason

@Composable
fun MetaScreen() {
    var selectedMode by remember { mutableStateOf("Battlegrounds") }
    var seasonsList by remember { mutableStateOf<List<MetaSeason>>(MetaRepository.seasons) }
    var isLoading by remember { mutableStateOf(false) }

    val filteredSeasons = seasonsList.filter { it.mode == selectedMode }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        Text(
            text = "META & SEZONLAR",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BFFF),
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "Aktif ve geçmiş sezon kuralları",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        // Mode filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedMode == "Battlegrounds",
                onClick = { selectedMode = "Battlegrounds" },
                label = { Text("Savaş Alanları", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF6B35),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF161B22),
                    labelColor = Color.LightGray
                ),
                shape = RoundedCornerShape(20.dp)
            )
            FilterChip(
                selected = selectedMode == "Alliance War",
                onClick = { selectedMode = "Alliance War" },
                label = { Text("İttifak Savaşı", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF9C27B0),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF161B22),
                    labelColor = Color.LightGray
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00BFFF))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (filteredSeasons.isEmpty()) {
                    item {
                        Text(
                            "Bu mod için sezon bilgisi bulunamadı.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(filteredSeasons) { season ->
                        SeasonCard(season = season)
                    }
                }
                if (selectedMode == "Alliance War" && GuiaBigThingRepository.nodes.isNotEmpty()) {
                    item { BigThingSection() }
                }
                if (selectedMode == "Alliance War" && GuiaAwRepository.groups.isNotEmpty()) {
                    item { AwGuideSection() }
                }
            }
        }
    }
}


@Composable
fun MetaChampionPortraitRow(championIds: List<String>, title: String, color: Color) {
    if (championIds.isEmpty()) return
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(title, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(championIds) { id ->
                val champ = ChampionRepository.champions.find { it.id == id }
                if (champ != null) {
                    val context = LocalContext.current
                    val drawableName = champ.id.lowercase().replace("-", "_").let { if (it[0].isDigit() || it == "void") "img_$it" else it }
                    val imageResId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(48.dp)
                    ) {
                        if (imageResId != 0) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = champ.name,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(champ.mcocClass.color).copy(alpha = 0.15f))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(champ.name.take(2).uppercase(), color = Color.White, fontSize = 9.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            champ.name, 
                            fontSize = 8.sp, 
                            color = Color.LightGray, 
                            maxLines = 1, 
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeasonCard(season: MetaSeason) {
    var isExpanded by remember { mutableStateOf(false) }
    val isActive = season.seasonNumber >= 40 || season.seasonNumber >= 28

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) Color(0xFF1C2333) else Color(0xFF161B22)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(season.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Text(season.dateRange, fontSize = 12.sp, color = Color.Gray)
                }
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("AKTİF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF30363D))
                Spacer(modifier = Modifier.height(12.dp))

                Text(season.description, fontSize = 13.sp, color = Color.LightGray, lineHeight = 18.sp)

                Spacer(modifier = Modifier.height(12.dp))
                Text("Aktif Karolar ve Öneriler:", fontWeight = FontWeight.Bold, color = Color(0xFFFF9800), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                season.nodes.forEach { node ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(node.name, fontWeight = FontWeight.Bold, color = Color(0xFF00BFFF), fontSize = 13.sp)
                            Text(node.effect, fontSize = 12.sp, color = Color.LightGray)
                            
                            if (node.bestAttackers.isNotEmpty() || node.bestDefenders.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color(0xFF30363D))
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                MetaChampionPortraitRow(
                                    championIds = node.bestAttackers,
                                    title = "⚔️ ÖNERİLEN SALDIRGANLAR",
                                    color = Color(0xFF4CAF50)
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                MetaChampionPortraitRow(
                                    championIds = node.bestDefenders,
                                    title = "🛡️ EN İYİ SAVUNMACILAR",
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }
                    }
                }

                if (season.bannedChampions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Yasaklı Şampiyonlar:", fontWeight = FontWeight.Bold, color = Color(0xFFF44336), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        season.bannedChampions.forEach { banned ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF44336).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(banned, fontSize = 11.sp, color = Color(0xFFF44336))
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${season.nodes.size} karo • ${season.weekRange}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}


/** GuiaMTC AW Sezon 69 rehberi: yol/bölüm seç, düğüm başına etkileri ve önerilen şampiyonları gör. */
@Composable
fun AwGuideSection() {
    val groups = GuiaAwRepository.groups
    var selectedId by remember { mutableStateOf(groups.first().id) }
    val group: AwGroup = groups.firstOrNull { it.id == selectedId } ?: groups.first()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "AW Sezon ${GuiaAwRepository.season} Rehberi (GuiaMTC)",
            fontWeight = FontWeight.Bold, color = Color(0xFF9C27B0), fontSize = 16.sp
        )
        Text(
            "Düğüm başına etkiler (İngilizce) ve savunmacı/saldırgan önerileri. Sarı çerçeve: yazarın öne çıkardığı şampiyon. Kaynak: guiamtc.com",
            color = Color.Gray, fontSize = 11.sp
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(groups) { g ->
                FilterChip(
                    selected = g.id == selectedId,
                    onClick = { selectedId = g.id },
                    label = { Text(g.title.substringBefore(" - ").ifBlank { g.title }, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF9C27B0), selectedLabelColor = Color.White,
                        containerColor = Color(0xFF161B22), labelColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
        Text(group.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        group.nodes.forEach { node -> AwNodeCard(node) }
    }
}

@Composable
fun AwNodeCard(node: AwNode) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Düğüm ${node.node}", color = Color(0xFF00BFFF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(node.effects.joinToString("\n"), color = Color.LightGray, fontSize = 12.sp)
            AwPortraitRow("Savunmacılar", node.defenders.map { it.id to it.highlighted }, Color(0xFFF44336))
            AwPortraitRow("Saldırganlar", node.attackers.map { it.id to it.highlighted }, Color(0xFF4CAF50))
        }
    }
}

@Composable
fun AwPortraitRow(title: String, items: List<Pair<String, Boolean>>, color: Color) {
    if (items.isEmpty()) return
    val context = LocalContext.current
    Column {
        Text(title, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 2.dp)) {
            items(items) { (id, highlighted) ->
                val champ = ChampionRepository.champions.find { it.id == id }
                val drawableName = id.lowercase().replace("-", "_").let { if (it[0].isDigit() || it == "void") "img_$it" else it }
                val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(52.dp)) {
                    if (resId != 0) {
                        Image(
                            painter = painterResource(id = resId), contentDescription = champ?.name ?: id,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (highlighted) Color(0xFFFFD700) else Color.Transparent, CircleShape)
                                .padding(if (highlighted) 2.dp else 0.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(Modifier.size(44.dp).clip(CircleShape).background(Color.DarkGray))
                    }
                    Text(champ?.name ?: id, color = Color.LightGray, fontSize = 8.sp, maxLines = 1)
                }
            }
        }
    }
}


/** AW Big Thing (Sezon 70, Ekim 2026) rehberi: resmi düğüm adı, zorluk, güç yükü kuralları, en iyi savunmacılar. */
@Composable
fun BigThingSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "AW Big Thing • Sezon ${GuiaBigThingRepository.season} (${GuiaBigThingRepository.startDate})",
            fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), fontSize = 16.sp
        )
        Text(GuiaBigThingRepository.rulesSummary, color = Color.LightGray, fontSize = 12.sp)
        Text(
            "Kaynak: guiamtc.com (rehber duyuru niteliğinde; oyunda başlamadan değişebilir). Düğüm adları resmi harita görselinden, açıklamalar Portekizce.",
            color = Color.Gray, fontSize = 11.sp
        )
        GuiaBigThingRepository.nodes.forEach { BigThingNodeCard(it) }
    }
}

@Composable
fun BigThingNodeCard(node: BtNode) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Düğüm ${node.node} • ${node.nameEn}", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Zorluk (Challenge): ${node.challengeEn}", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(node.guidePt, color = Color.LightGray, fontSize = 12.sp)
            if (node.rules.isNotEmpty()) {
                Text("Güç Yükü kazanma:", color = Color(0xFF00BFFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                node.rules.forEach { r ->
                    Text("• " + r.textPt, color = Color.LightGray, fontSize = 11.sp)
                }
            }
            AwPortraitRow("En iyi savunmacılar", node.bestDefenders.map { it to false }, Color(0xFFF44336))
        }
    }
}
