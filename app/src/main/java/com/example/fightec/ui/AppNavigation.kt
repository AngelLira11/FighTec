package com.example.fightec.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fightec.entities.GameCatalog

@Composable
fun AppNavigation() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "menu") {

        composable("menu") {
            MenuScreen(onPlay = { nav.navigate("select") })
        }

        composable("select") {
            CharacterSelectScreen(
                onFight = { playerId, arenaId ->
                    val enemyId = GameCatalog.characters.filter { it.id != playerId }.random().id
                    nav.navigate("fight/$playerId/$enemyId/$arenaId")
                },
                onBack = { nav.popBackStack() }
            )
        }

        composable(
            route = "fight/{playerId}/{enemyId}/{arenaId}",
            arguments = listOf(
                navArgument("playerId") { type = NavType.IntType },
                navArgument("enemyId") { type = NavType.IntType },
                navArgument("arenaId") { type = NavType.IntType }
            )
        ) { entry ->
            val playerId = entry.arguments!!.getInt("playerId")
            val enemyId = entry.arguments!!.getInt("enemyId")
            val arenaId = entry.arguments!!.getInt("arenaId")

            FightArenaScreen(
                playerChar = GameCatalog.character(playerId),
                enemyChar = GameCatalog.character(enemyId),
                arena = GameCatalog.arena(arenaId),
                onFightEnd = { playerWon ->
                    nav.navigate("victory/$playerWon/$playerId/$enemyId/$arenaId") {
                        popUpTo("select")          // quita la pelea de la pila
                    }
                }
            )
        }

        composable(
            route = "victory/{playerWon}/{playerId}/{enemyId}/{arenaId}",
            arguments = listOf(
                navArgument("playerWon") { type = NavType.BoolType },
                navArgument("playerId") { type = NavType.IntType },
                navArgument("enemyId") { type = NavType.IntType },
                navArgument("arenaId") { type = NavType.IntType }
            )
        ) { entry ->
            val playerWon = entry.arguments!!.getBoolean("playerWon")
            val playerId = entry.arguments!!.getInt("playerId")
            val enemyId = entry.arguments!!.getInt("enemyId")
            val arenaId = entry.arguments!!.getInt("arenaId")

            VictoryScreen(
                playerChar = GameCatalog.character(playerId),
                enemyChar = GameCatalog.character(enemyId),
                playerWon = playerWon,
                onRematch = {
                    nav.navigate("fight/$playerId/$enemyId/$arenaId") { popUpTo("select") }
                },
                onMenu = { nav.popBackStack("menu", inclusive = false) }
            )
        }
    }
}