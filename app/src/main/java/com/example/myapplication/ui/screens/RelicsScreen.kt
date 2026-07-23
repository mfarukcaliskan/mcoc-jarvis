package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.ChampionClass
import com.example.myapplication.data.Relic
import com.example.myapplication.data.RelicRepository

@Composable
fun RelicsScreen() {
    var selectedClass by remember { mutableStateOf<ChampionClass?>(null) }
    var selectedType by remember { mutableStateOf("") }
    var expandedRelicId by remember { mutableStateOf("") }

    val filteredRelics = RelicRepository.relics.filter { relic ->
        (selectedClass == null || relic.relicClass == selectedClass) &&
        (selectedType.isEmpty() || relic.relicType == selectedType)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        Text(
            text = "ANDAÇLAR (RELICS)",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BFFF),
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "${filteredRelics.size} andaç listeleniyor",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        // Type filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedType == "Battlecast",
                onClick = { selectedType = if (selectedType == "Battlecast") "" else "Battlecast" },
                label = { Text("Battlecast", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF6B35),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF161B22),
                    labelColor = Color.LightGray
                ),
                shape = RoundedCornerShape(20.dp)
            )
            FilterChip(
                selected = selectedType == "Statcast",
                onClick = { selectedType = if (selectedType == "Statcast") "" else "Statcast" },
                label = { Text("Statcast", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF2196F3),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF161B22),
                    labelColor = Color.LightGray
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }

        // Class filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedClass == null,
                onClick = { selectedClass = null },
                label = { Text("Hepsi", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00BFFF),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF161B22),
                    labelColor = Color.LightGray
                ),
                shape = RoundedCornerShape(20.dp)
            )
            ChampionClass.entries.forEach { cls ->
                FilterChip(
                    selected = selectedClass == cls,
                    onClick = { selectedClass = if (selectedClass == cls) null else cls },
                    label = { Text(cls.displayName, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(cls.color),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF161B22),
                        labelColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredRelics) { relic ->
                RelicCard(
                    relic = relic,
                    isExpanded = expandedRelicId == relic.id,
                    onClick = {
                        expandedRelicId = if (expandedRelicId == relic.id) "" else relic.id
                    }
                )
            }
        }
    }
}

@Composable
fun RelicCard(relic: Relic, isExpanded: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) Color(0xFF1C2333) else Color(0xFF161B22)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val context = LocalContext.current
                val characterName = relic.id.substringBefore("_relic").replace("_", "")
                val drawableName = "relic_$characterName"
                val imageResId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(relic.relicClass.color).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageResId != 0) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = relic.name,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            relic.name.take(1),
                            fontWeight = FontWeight.Bold,
                            color = Color(relic.relicClass.color),
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(relic.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text(
                        relic.relicType,
                        fontSize = 10.sp,
                        color = if (relic.relicType == "Battlecast") Color(0xFFFF6B35) else Color(0xFF2196F3)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFF30363D))
                Spacer(modifier = Modifier.height(8.dp))

                Text(relic.description, fontSize = 12.sp, color = Color.LightGray, lineHeight = 16.sp)

                Spacer(modifier = Modifier.height(8.dp))
                Text("Doğal Yetenekler:", fontWeight = FontWeight.Bold, color = Color(0xFFFF9800), fontSize = 11.sp)
                relic.innateAbilities.forEach { ability ->
                    Text("• $ability", fontSize = 11.sp, color = Color.LightGray)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text("Uyumlu Şampiyonlar:", fontWeight = FontWeight.Bold, color = Color(0xFF00BFFF), fontSize = 11.sp)
                relic.recommendedChampions.forEach { champ ->
                    Text("• $champ", fontSize = 11.sp, color = Color.LightGray)
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Detay için tıklayın", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}
