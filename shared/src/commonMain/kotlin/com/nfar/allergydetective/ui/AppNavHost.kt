package com.nfar.allergydetective.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.ui.daycheck.DayCheckScreen
import com.nfar.allergydetective.ui.results.ResultsScreen
import com.nfar.allergydetective.ui.home.HomeProgress
import com.nfar.allergydetective.ui.home.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nfar.allergydetective.ui.home.HomeAction
import com.nfar.allergydetective.ui.home.HomeScreen
import com.nfar.allergydetective.ui.meal.LogMealScreen
import com.nfar.allergydetective.ui.symptom.LogSymptomScreen
import kotlinx.serialization.Serializable

/** Type-safe routes. Each screen gets its own route when it is built. */
@Serializable
data object HomeRoute

@Serializable
data object LogMealRoute

@Serializable
data object LogSymptomRoute

@Serializable
data object DayCheckRoute

@Serializable
data object ResultsRoute

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val back: () -> Unit = { navController.popBackStack() }
    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            val homeViewModel: HomeViewModel = koinViewModel()
            val confirmedDays by homeViewModel.confirmedDays.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { homeViewModel.refresh() }
            HomeScreen(
                header = { confirmedDays?.let { HomeProgress(it) } },
                onAction = { action ->
                    when (action) {
                        HomeAction.LOG_MEAL -> navController.navigate(LogMealRoute)
                        HomeAction.LOG_SYMPTOM -> navController.navigate(LogSymptomRoute)
                        HomeAction.DAY_CHECK -> navController.navigate(DayCheckRoute)
                        HomeAction.RESULTS -> navController.navigate(ResultsRoute)
                    }
                },
            )
        }
        composable<LogMealRoute> { LogMealScreen(onBack = back) }
        composable<LogSymptomRoute> { LogSymptomScreen(onBack = back) }
        composable<DayCheckRoute> { DayCheckScreen(onDone = back) }
        composable<ResultsRoute> { ResultsScreen(onBack = back) }
    }
}
