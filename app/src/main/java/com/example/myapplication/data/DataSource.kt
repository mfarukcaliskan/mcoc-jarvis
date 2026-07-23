package com.example.myapplication.data

import android.content.Context
import java.io.File

/**
 * Tek okuma noktası: önce RemoteDataUpdater'ın indirdiği güncel veri var mı bakar,
 * yoksa APK'ya gömülü (assets) veriye düşer. Böylece ChampionRepository/QuestRepository
 * verinin bundled mı yoksa indirilmiş mi olduğuyla ilgilenmez.
 */
object DataSource {
    fun openText(context: Context, relativePath: String): String {
        val downloaded = File(context.filesDir, "mcoc_data/$relativePath")
        if (downloaded.isFile) {
            return downloaded.readText()
        }
        return context.assets.open(relativePath).bufferedReader().use { it.readText() }
    }
}
