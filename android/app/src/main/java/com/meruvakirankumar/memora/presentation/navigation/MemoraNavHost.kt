package com.meruvakirankumar.memora.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.meruvakirankumar.memora.presentation.add.AddMemoryScreen
import com.meruvakirankumar.memora.presentation.home.HomeScreen

private object Routes {
    const val HOME = "home"
    const val ADD = "add"
}

@Composable
fun MemoraNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(onAddMemory = { navController.navigate(Routes.ADD) })
        }
        composable(Routes.ADD) {
            AddMemoryScreen(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
