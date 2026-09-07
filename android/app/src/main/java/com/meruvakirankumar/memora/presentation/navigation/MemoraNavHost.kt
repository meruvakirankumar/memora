package com.meruvakirankumar.memora.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meruvakirankumar.memora.presentation.add.AddMemoryScreen
import com.meruvakirankumar.memora.presentation.capture.CaptureScreen
import com.meruvakirankumar.memora.presentation.detail.MemoryDetailScreen
import com.meruvakirankumar.memora.presentation.home.HomeScreen

private object Routes {
    const val HOME = "home"
    const val CAPTURE = "capture"
    const val ADD = "add"
    const val ADD_WITH_IMAGE = "add?imageUri={imageUri}"
    const val DETAIL = "detail/{memoryId}"
}

@Composable
fun MemoraNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddMemory = { navController.navigate(Routes.CAPTURE) },
                onOpenMemory = { id -> navController.navigate("detail/$id") },
            )
        }
        composable(Routes.CAPTURE) {
            CaptureScreen(
                onContinue = { uri -> navController.navigate("${Routes.ADD}?imageUri=${Uri.encode(uri)}") },
                onManualEntry = { navController.navigate(Routes.ADD) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.ADD_WITH_IMAGE,
            arguments = listOf(
                navArgument("imageUri") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            AddMemoryScreen(
                onSaved = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("memoryId") { type = NavType.StringType }),
        ) {
            MemoryDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
