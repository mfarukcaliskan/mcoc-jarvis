package com.example.mcocjarvis.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation3.runtime.NavKey

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier
) {
  var nodeName by remember { mutableStateOf("") }
  var analysisResult by remember { mutableStateOf("") }

  Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
    Text(text = "JARVIS MCOC Asistanı", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))
    
    OutlinedTextField(
      value = nodeName,
      onValueChange = { nodeName = it },
      label = { Text("Karo (Node) İsmi Girin") },
      modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(
      onClick = {
        if (nodeName.lowercase().contains("bleed")) {
            analysisResult = "Tavsiye: 16.250 PI Kara Panter'i kullanın! Siper yapmadan önleme (intercept) ile saldırın."
        } else if (nodeName.lowercase().contains("biohazard")) {
            analysisResult = "Tavsiye: Nimrod veya Warlock kullanın (Çift Bağışıklık)."
        } else if (nodeName.lowercase().contains("stop")) {
            analysisResult = "Tavsiye: Titania'yı seçin (Durdurulamaz mekaniği)."
        } else {
            analysisResult = "Strateji Motoru: En iyi şampiyon Doktor Doom (16.289). Sendeletme ve güç kontrolü uygulayın."
        }
      },
      modifier = Modifier.fillMaxWidth()
    ) {
      Text("Veritabanında Analiz Et")
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    
    if (analysisResult.isNotEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("JARVIS Analizi:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(analysisResult)
            }
        }
    }
  }
}
