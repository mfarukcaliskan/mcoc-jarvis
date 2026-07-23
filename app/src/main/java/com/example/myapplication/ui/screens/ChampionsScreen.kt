package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
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
import com.example.myapplication.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChampionsScreen(onChampionClick: (String) -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedClasses by remember { mutableStateOf(setOf<ChampionClass>()) }
    var selectedTiers by remember { mutableStateOf(setOf<String>()) }
    var isGridView by remember { mutableStateOf(true) }
    var sortBy by remember { mutableStateOf(SortOption.PRESTIGE_DESC) }
    var showFilterSheet by remember { mutableStateOf(false) }

    // Advanced filter states
    var selectedAbilities by remember { mutableStateOf(setOf<String>()) }
    var abilityMatchMode by remember { mutableStateOf(MatchMode.ANY) }
    var selectedImmunities by remember { mutableStateOf(setOf<String>()) }
    var immunityMatchMode by remember { mutableStateOf(MatchMode.ANY) }
    var selectedReactsTo by remember { mutableStateOf(setOf<String>()) }
    var reactsToMatchMode by remember { mutableStateOf(MatchMode.ANY) }
    var selectedCounterAbilities by remember { mutableStateOf(setOf<String>()) }
    var counterAbilityMatchMode by remember { mutableStateOf(MatchMode.ANY) }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var tagsMatchMode by remember { mutableStateOf(MatchMode.ANY) }
    var counterChampionId by remember { mutableStateOf<String?>(null) }

    val filter = ChampionFilter(
        selectedClasses = selectedClasses,
        selectedTiers = selectedTiers,
        selectedImmunities = selectedImmunities,
        immunityMatchMode = immunityMatchMode,
        selectedAbilities = selectedAbilities,
        abilityMatchMode = abilityMatchMode,
        selectedReactsTo = selectedReactsTo,
        reactsToMatchMode = reactsToMatchMode,
        selectedCounterAbilities = selectedCounterAbilities,
        counterAbilityMatchMode = counterAbilityMatchMode,
        selectedTags = selectedTags,
        tagsMatchMode = tagsMatchMode,
        counterChampionId = counterChampionId,
        searchQuery = searchQuery,
        sortBy = sortBy
    )
    val filteredChampions = FilterEngine.applyFilter(ChampionRepository.champions, filter)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        // ===== HEADER =====
        Text(
            text = "ŞAMPIYONLAR",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BFFF),
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "${filteredChampions.size} / ${ChampionRepository.champions.size} şampiyon",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        // ===== SEARCH BAR + FILTER BUTTON =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Şampiyon ara...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF00BFFF)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Temizle", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00BFFF),
                    unfocusedBorderColor = Color(0xFF30363D),
                    cursorColor = Color(0xFF00BFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            // Filter button with badge
            BadgedBox(
                badge = {
                    if (filter.activeFilterCount > 0) {
                        Badge(
                            containerColor = Color(0xFFFF6B35)
                        ) {
                            Text("${filter.activeFilterCount}")
                        }
                    }
                }
            ) {
                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF161B22), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Filtre", tint = Color(0xFF00BFFF))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ===== CLASS FILTER CHIPS =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ChampionClass.entries.forEach { cls ->
                FilterChip(
                    selected = cls in selectedClasses,
                    onClick = {
                        selectedClasses = if (cls in selectedClasses)
                            selectedClasses - cls else selectedClasses + cls
                    },
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

        // ===== TIER FILTER + VIEW TOGGLE + SORT =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("S", "A", "B", "C", "D").forEach { tier ->
                FilterChip(
                    selected = tier in selectedTiers,
                    onClick = {
                        selectedTiers = if (tier in selectedTiers)
                            selectedTiers - tier else selectedTiers + tier
                    },
                    label = { Text(tier, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = getTierColor(tier),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF161B22),
                        labelColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Grid / List toggle
            TextButton(onClick = { isGridView = !isGridView }) {
                Text(
                    if (isGridView) "TABLO" else "IZGARA",
                    color = Color(0xFF00BFFF),
                    fontSize = 11.sp
                )
            }
        }

        // ===== Active Counter Champion indicator =====
        if (counterChampionId != null) {
            val counterChamp = ChampionRepository.champions.find { it.id == counterChampionId }
            if (counterChamp != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .background(Color(0xFF1C2333), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎯 Counter: ", color = Color(0xFFFF6B35), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(counterChamp.name, color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { counterChampionId = null }) {
                        Text("✕ Kaldır", color = Color(0xFFF44336), fontSize = 11.sp)
                    }
                }
            }
        }

        // ===== CHAMPION LIST =====
        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredChampions) { champion ->
                    ChampionGridCard(champion = champion, onClick = { onChampionClick(champion.id) })
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredChampions) { champion ->
                    ChampionTableRow(champion = champion, onClick = { onChampionClick(champion.id) })
                }
            }
        }
    }

    // ===== FILTER BOTTOM SHEET =====
    if (showFilterSheet) {
        FilterBottomSheet(
            selectedAbilities = selectedAbilities,
            abilityMatchMode = abilityMatchMode,
            selectedImmunities = selectedImmunities,
            immunityMatchMode = immunityMatchMode,
            selectedReactsTo = selectedReactsTo,
            reactsToMatchMode = reactsToMatchMode,
            selectedCounterAbilities = selectedCounterAbilities,
            counterAbilityMatchMode = counterAbilityMatchMode,
            selectedTags = selectedTags,
            tagsMatchMode = tagsMatchMode,
            counterChampionId = counterChampionId,
            onApply = { ab, abMode, imm, immMode, rt, rtMode, ca, caMode, tags, tagsMode, ccId ->
                selectedAbilities = ab
                abilityMatchMode = abMode
                selectedImmunities = imm
                immunityMatchMode = immMode
                selectedReactsTo = rt
                reactsToMatchMode = rtMode
                selectedCounterAbilities = ca
                counterAbilityMatchMode = caMode
                selectedTags = tags
                tagsMatchMode = tagsMode
                counterChampionId = ccId
                showFilterSheet = false
            },
            onCancel = { showFilterSheet = false }
        )
    }
}

// ========== FILTER BOTTOM SHEET ==========
@Composable
fun FilterBottomSheet(
    selectedAbilities: Set<String>,
    abilityMatchMode: MatchMode,
    selectedImmunities: Set<String>,
    immunityMatchMode: MatchMode,
    selectedReactsTo: Set<String>,
    reactsToMatchMode: MatchMode,
    selectedCounterAbilities: Set<String>,
    counterAbilityMatchMode: MatchMode,
    selectedTags: Set<String>,
    tagsMatchMode: MatchMode,
    counterChampionId: String?,
    onApply: (Set<String>, MatchMode, Set<String>, MatchMode, Set<String>, MatchMode, Set<String>, MatchMode, Set<String>, MatchMode, String?) -> Unit,
    onCancel: () -> Unit
) {
    var tempAbilities by remember { mutableStateOf(selectedAbilities) }
    var tempAbilityMode by remember { mutableStateOf(abilityMatchMode) }
    var tempImmunities by remember { mutableStateOf(selectedImmunities) }
    var tempImmunityMode by remember { mutableStateOf(immunityMatchMode) }
    var tempReactsTo by remember { mutableStateOf(selectedReactsTo) }
    var tempReactsToMode by remember { mutableStateOf(reactsToMatchMode) }
    var tempCounterAbilities by remember { mutableStateOf(selectedCounterAbilities) }
    var tempCounterAbilityMode by remember { mutableStateOf(counterAbilityMatchMode) }
    var tempTags by remember { mutableStateOf(selectedTags) }
    var tempTagsMode by remember { mutableStateOf(tagsMatchMode) }
    var tempCounterChampionId by remember { mutableStateOf(counterChampionId) }

    var abilitySearch by remember { mutableStateOf("") }
    var immunitySearch by remember { mutableStateOf("") }
    var counterAbilitySearch by remember { mutableStateOf("") }
    var tagSearch by remember { mutableStateOf("") }
    var counterChampionSearch by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000).copy(alpha = 0.7f))
            .clickable(onClick = onCancel)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .align(Alignment.BottomCenter)
                .background(
                    Color(0xFF0D1117),
                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .clickable(enabled = false) {}
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Şampiyon Filtrele",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BFFF)
                )
            }

            // Scrollable content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ===== ABILITY SECTION =====
                item {
                    FilterSection(
                        title = "ABILITY",
                        searchQuery = abilitySearch,
                        onSearchChange = { abilitySearch = it },
                        searchPlaceholder = "Yetenek ara...",
                        selectedItems = tempAbilities,
                        matchMode = tempAbilityMode,
                        onMatchModeChange = { tempAbilityMode = it },
                        allItems = FilterEngine.allAbilities,
                        onItemToggle = { item ->
                            tempAbilities = if (item in tempAbilities)
                                tempAbilities - item else tempAbilities + item
                        },
                        onReset = { tempAbilities = emptySet(); abilitySearch = "" }
                    )
                }

                // ===== IMMUNITY SECTION =====
                item {
                    FilterSection(
                        title = "IMMUNITY",
                        searchQuery = immunitySearch,
                        onSearchChange = { immunitySearch = it },
                        searchPlaceholder = "Bağışıklık ara...",
                        selectedItems = tempImmunities,
                        matchMode = tempImmunityMode,
                        onMatchModeChange = { tempImmunityMode = it },
                        allItems = FilterEngine.allImmunities.map { it.first },
                        onItemToggle = { item ->
                            tempImmunities = if (item in tempImmunities)
                                tempImmunities - item else tempImmunities + item
                        },
                        onReset = { tempImmunities = emptySet(); immunitySearch = "" }
                    )
                }

                // ===== REACTS TO SECTION =====
                item {
                    FilterSection(
                        title = "REACTS TO",
                        searchQuery = "",
                        onSearchChange = {},
                        searchPlaceholder = "",
                        selectedItems = tempReactsTo,
                        matchMode = tempReactsToMode,
                        onMatchModeChange = { tempReactsToMode = it },
                        allItems = FilterEngine.allReactsTo,
                        onItemToggle = { item ->
                            tempReactsTo = if (item in tempReactsTo)
                                tempReactsTo - item else tempReactsTo + item
                        },
                        onReset = { tempReactsTo = emptySet() },
                        showSearch = false
                    )
                }

                // ===== COUNTER ABILITY SECTION =====
                item {
                    FilterSection(
                        title = "COUNTER ABILITY",
                        searchQuery = counterAbilitySearch,
                        onSearchChange = { counterAbilitySearch = it },
                        searchPlaceholder = "Counter yetenek ara...",
                        selectedItems = tempCounterAbilities,
                        matchMode = tempCounterAbilityMode,
                        onMatchModeChange = { tempCounterAbilityMode = it },
                        allItems = FilterEngine.allCounterAbilities,
                        onItemToggle = { item ->
                            tempCounterAbilities = if (item in tempCounterAbilities)
                                tempCounterAbilities - item else tempCounterAbilities + item
                        },
                        onReset = { tempCounterAbilities = emptySet(); counterAbilitySearch = "" }
                    )
                }

                // ===== TAGS SECTION =====
                item {
                    FilterSection(
                        title = "TAGS (ETİKETLER)",
                        searchQuery = tagSearch,
                        onSearchChange = { tagSearch = it },
                        searchPlaceholder = "Etiket ara (#Robot, #Metal vb.)...",
                        selectedItems = tempTags,
                        matchMode = tempTagsMode,
                        onMatchModeChange = { tempTagsMode = it },
                        allItems = FilterEngine.allTags,
                        onItemToggle = { item ->
                            tempTags = if (item in tempTags)
                                tempTags - item else tempTags + item
                        },
                        onReset = { tempTags = emptySet(); tagSearch = "" }
                    )
                }

                // ===== COUNTER CHAMPION SECTION =====
                item {
                    CounterChampionSection(
                        searchQuery = counterChampionSearch,
                        onSearchChange = { counterChampionSearch = it },
                        selectedChampionId = tempCounterChampionId,
                        onSelect = { tempCounterChampionId = it },
                        onReset = { tempCounterChampionId = null; counterChampionSearch = "" }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // ===== APPLY / CANCEL BUTTONS =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        onApply(
                            tempAbilities, tempAbilityMode,
                            tempImmunities, tempImmunityMode,
                            tempReactsTo, tempReactsToMode,
                            tempCounterAbilities, tempCounterAbilityMode,
                            tempTags, tempTagsMode,
                            tempCounterChampionId
                        )
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("UYGULA", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("İPTAL", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

// ========== FILTER SECTION COMPOSABLE ==========
@Composable
fun FilterSection(
    title: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    searchPlaceholder: String,
    selectedItems: Set<String>,
    matchMode: MatchMode,
    onMatchModeChange: (MatchMode) -> Unit,
    allItems: List<String>,
    onItemToggle: (String) -> Unit,
    onReset: () -> Unit,
    showSearch: Boolean = true
) {
    val filteredItems = if (searchQuery.isNotBlank()) {
        allItems.filter { it.lowercase().contains(searchQuery.lowercase()) }
    } else allItems

    Column {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC8FF00)
            )
            if (selectedItems.isNotEmpty()) {
                TextButton(onClick = onReset) {
                    Text("🔄", fontSize = 14.sp)
                }
            }
        }

        // ALL / ANY Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            MatchMode.entries.forEach { mode ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (matchMode == mode) Color(0xFF1C2333) else Color(0xFF161B22),
                            if (mode == MatchMode.ALL) RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                            else RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        )
                        .clickable { onMatchModeChange(mode) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        mode.name,
                        color = if (matchMode == mode) Color(0xFF00BFFF) else Color.Gray,
                        fontWeight = if (matchMode == mode) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Search
        if (showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(searchPlaceholder, color = Color.Gray, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color(0xFF00BFFF), modifier = Modifier.size(16.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Filled.Clear, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF30363D),
                    unfocusedBorderColor = Color(0xFF30363D),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Items grid (3 columns with wrapping)
        val rows = filteredItems.chunked(3)
        rows.take(8).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowItems.forEach { item ->
                    val isSelected = item in selectedItems
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 3.dp)
                            .background(
                                if (isSelected) Color(0xFF00BFFF).copy(alpha = 0.2f) else Color(0xFF161B22),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onItemToggle(item) }
                            .padding(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Text(
                            item,
                            fontSize = 11.sp,
                            color = if (isSelected) Color(0xFF00BFFF) else Color.LightGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Fill remaining slots
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (selectedItems.isNotEmpty()) {
            Text(
                "${selectedItems.size} seçili",
                fontSize = 11.sp,
                color = Color(0xFF00BFFF),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ========== COUNTER CHAMPION SECTION ==========
@Composable
fun CounterChampionSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedChampionId: String?,
    onSelect: (String?) -> Unit,
    onReset: () -> Unit
) {
    val filtered = if (searchQuery.isNotBlank()) {
        ChampionRepository.champions.filter {
            it.name.lowercase().contains(searchQuery.lowercase())
        }
    } else {
        ChampionRepository.champions.filter { it.tier in listOf("S", "A") }
    }.take(20)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "COUNTER CHAMPION",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC8FF00)
            )
            if (selectedChampionId != null) {
                TextButton(onClick = onReset) {
                    Text("🔄", fontSize = 14.sp)
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Şampiyon ara...", color = Color.Gray, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color(0xFF00BFFF), modifier = Modifier.size(16.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Filled.Clear, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF30363D),
                unfocusedBorderColor = Color(0xFF30363D),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Selected champion highlight
        if (selectedChampionId != null) {
            val selChamp = ChampionRepository.champions.find { it.id == selectedChampionId }
            if (selChamp != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF00BFFF).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChampionMiniPortrait(selChamp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selChamp.name, color = Color(0xFF00BFFF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("✓ Seçili", color = Color(0xFF4CAF50), fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Champion grid (scrollable row)
        val rows = filtered.chunked(5)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowItems.forEach { champ ->
                    val isSelected = champ.id == selectedChampionId
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) Color(0xFF00BFFF).copy(alpha = 0.3f) else Color(0xFF161B22)
                            )
                            .clickable {
                                onSelect(if (isSelected) null else champ.id)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        ChampionMiniPortrait(champ)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun ChampionMiniPortrait(champion: Champion) {
    val context = LocalContext.current
    val drawableName = champion.id.lowercase().replace("-", "_").let {
        if (it[0].isDigit() || it == "void") "img_$it" else it
    }
    val imageResId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

    if (imageResId != 0) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = champion.name,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(champion.mcocClass.color).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                champion.name.take(2).uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(champion.mcocClass.color)
            )
        }
    }
}

// ========== EXISTING COMPONENTS ==========

@Composable
fun ChampionGridCard(champion: Champion, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Class color circle
            val context = LocalContext.current
            val drawableName = champion.id.lowercase().replace("-", "_").let { if (it[0].isDigit() || it == "void") "img_$it" else it }
            val imageResId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(champion.mcocClass.color).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = champion.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = champion.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(champion.mcocClass.color)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = champion.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Tier badge
                Box(
                    modifier = Modifier
                        .background(getTierColor(champion.tier), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(champion.tier, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                // Prestige
                Text(
                    text = "#${champion.prestigeRank}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ChampionTableRow(champion: Champion, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Class indicator
            val context = LocalContext.current
            val drawableName = champion.id.lowercase().replace("-", "_").let { if (it[0].isDigit() || it == "void") "img_$it" else it }
            val imageResId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(champion.mcocClass.color).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = champion.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        champion.name.take(2).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(champion.mcocClass.color)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(champion.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Text(champion.mcocClass.displayName, fontSize = 11.sp, color = Color(champion.mcocClass.color))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${champion.prestige}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF00BFFF)
                )
                Text("#${champion.prestigeRank}", fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(getTierColor(champion.tier), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(champion.tier, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

fun getTierColor(tier: String): Color {
    return when (tier) {
        "S" -> Color(0xFFFF6B35)
        "A" -> Color(0xFF4CAF50)
        "B" -> Color(0xFF2196F3)
        "C" -> Color(0xFF9E9E9E)
        "D" -> Color(0xFF795548)
        else -> Color.Gray
    }
}
