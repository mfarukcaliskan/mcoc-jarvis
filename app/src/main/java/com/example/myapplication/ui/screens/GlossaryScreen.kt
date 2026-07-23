package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.GlossaryRepository

@Composable
fun GlossaryScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }

    val categories = listOf("Buff", "Debuff", "Mekanik", "Durum")
    val filteredTerms = GlossaryRepository.terms.filter { term ->
        (searchQuery.isBlank() ||
            term.term.lowercase().contains(searchQuery.lowercase()) ||
            term.termTr.lowercase().contains(searchQuery.lowercase())) &&
        (selectedCategory.isEmpty() || term.category == selectedCategory)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        Text(
            text = "SÖZLÜK (GLOSSARY)",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BFFF),
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "${filteredTerms.size} terim listeleniyor",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Terim ara...", color = Color.Gray) },
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategory.isEmpty(),
                onClick = { selectedCategory = "" },
                label = { Text("Hepsi", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00BFFF),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF161B22),
                    labelColor = Color.LightGray
                ),
                shape = RoundedCornerShape(20.dp)
            )
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = if (selectedCategory == cat) "" else cat },
                    label = { Text(cat, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = getCategoryColor(cat),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF161B22),
                        labelColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredTerms) { term ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(term.term, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Text(term.termTr, fontSize = 12.sp, color = Color(0xFF00BFFF))
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        getCategoryColor(term.category).copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    term.category,
                                    fontSize = 10.sp,
                                    color = getCategoryColor(term.category),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(term.description, fontSize = 13.sp, color = Color.LightGray, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Buff" -> Color(0xFF4CAF50)
        "Debuff" -> Color(0xFFF44336)
        "Mekanik" -> Color(0xFFFF9800)
        "Durum" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
}
