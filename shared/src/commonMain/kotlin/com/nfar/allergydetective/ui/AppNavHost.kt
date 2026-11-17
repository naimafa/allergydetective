package com.nfar.allergydetective.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nfar.allergydetective.ui.home.HomeScreen
import kotlinx.serialization.Serializable

/** Type-safe routes. Each screen gets its own route when it is built. */
@Serializable
data object HomeRoute

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(onAction = { /* wired up per screen */ })
        }
    }
}
