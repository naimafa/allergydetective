package com.nfar.allergydetective.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.ui.daycheck.DayCheckScreen
import com.nfar.allergydetective.ui.results.ResultsScreen
import org.koin.compose.viewmodel.koinViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nfar.allergydetective.ui.home.HomeAction
import com.nfar.allergydetective.ui.meal.LogMealScreen
import com.nfar.allergydetective.ui.profile.OnboardingScreen
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

@Serializable
data object OnboardingRoute

@Composable
fun AppNavHost(startViewModel: StartViewModel = koinViewModel()) {
    val onboardingCompleted by startViewModel.onboardingCompleted.collectAsStateWithLifecycle()
    // Nothing to show until we know whether this is the first start.
    val completed = onboardingCompleted ?: return
    val navController = rememberNavController()
    val back: () -> Unit = { navController.popBackStack() }
    NavHost(navController = navController, startDestination = if (completed) HomeRoute else OnboardingRoute) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(HomeRoute) { popUpTo<OnboardingRoute> { inclusive = true } }
                },
            )
        }
        composable<HomeRoute> {
            MainScreen(
                onHomeAction = { action ->
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
