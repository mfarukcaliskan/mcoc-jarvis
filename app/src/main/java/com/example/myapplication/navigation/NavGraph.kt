package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.*

sealed class Screen(val route: String, val title: String, val icon: String) {
    object Home : Screen("home", "Asistan", "\uD83E\uDD16")
    object Champions : Screen("champions", "Şampiyonlar", "\u2694\uFE0F")
    object Relics : Screen("relics", "Andaçlar", "\uD83D\uDD2E")
    object Meta : Screen("meta", "Meta", "\uD83D\uDCC5")
    object Deck : Screen("deck", "BG Deste", "🃏")
    object Glossary : Screen("glossary", "Sözlük", "📚")
    object Quests : Screen("quests", "Macera", "🗺️")
}

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen()
        }
        composable(route = Screen.Champions.route) {
            ChampionsScreen(
                onChampionClick = { championId ->
                    navController.navigate("champion_detail/$championId")
                }
            )
        }
        composable(
            route = "champion_detail/{championId}",
            arguments = listOf(navArgument("championId") { type = NavType.StringType })
        ) { backStackEntry ->
            val championId = backStackEntry.arguments?.getString("championId") ?: ""
            ChampionDetailScreen(
                championId = championId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.Relics.route) {
            RelicsScreen()
        }
        composable(route = Screen.Meta.route) {
            MetaScreen()
        }
        composable(route = Screen.Deck.route) {
            DeckAnalyzerScreen()
        }
        composable(route = Screen.Glossary.route) {
            GlossaryScreen()
        }
        composable(route = Screen.Quests.route) {
            QuestNavigatorScreen()
        }
    }
}
