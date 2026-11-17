package com.nfar.allergydetective.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nfar.allergydetective.ui.home.HomeAction
import com.nfar.allergydetective.ui.home.HomeScreen
import com.nfar.allergydetective.ui.meal.LogMealScreen
import kotlinx.serialization.Serializable

/** Type-safe routes. Each screen gets its own route when it is built. */
@Serializable
data object HomeRoute

@Serializable
data object LogMealRoute

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val back: () -> Unit = { navController.popBackStack() }
    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onAction = { action ->
                    when (action) {
                        HomeAction.LOG_MEAL -> navController.navigate(LogMealRoute)
                        else -> Unit
                    }
                },
            )
        }
        composable<LogMealRoute> { LogMealScreen(onBack = back) }
    }
}
