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
import androidx.navigation.toRoute
import com.nfar.allergydetective.ui.home.HomeAction
import com.nfar.allergydetective.ui.meal.LogMealScreen
import com.nfar.allergydetective.ui.profile.OnboardingScreen
import com.nfar.allergydetective.ui.profile.ReminderSection
import com.nfar.allergydetective.ui.profile.ExportSection
import androidx.compose.runtime.LaunchedEffect
import com.nfar.allergydetective.ui.symptom.LogSymptomScreen
import kotlinx.serialization.Serializable

/** Type-safe routes. Each screen gets its own route when it is built. */
@Serializable
data class HomeRoute(
    /** After the intro: "add dishes now" opens the favorites tab. */
    val openFavorites: Boolean = false,
)

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
fun AppNavHost(
    openDayCheck: Boolean = false,
    onDayCheckOpened: () -> Unit = {},
    startViewModel: StartViewModel = koinViewModel(),
) {
    val onboardingCompleted by startViewModel.onboardingCompleted.collectAsStateWithLifecycle()
    // Nothing to show until we know whether this is the first start.
    val completed = onboardingCompleted ?: return
    val navController = rememberNavController()
    val back: () -> Unit = { navController.popBackStack() }
    // Tapping the evening reminder opens the day check directly (UX 4).
    LaunchedEffect(openDayCheck) {
        if (openDayCheck && completed) {
            navController.navigate(DayCheckRoute) { launchSingleTop = true }
            onDayCheckOpened()
        }
    }
    NavHost(navController = navController, startDestination = if (completed) HomeRoute() else OnboardingRoute) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                onFinished = { openDishes ->
                    navController.navigate(HomeRoute(openFavorites = openDishes)) { popUpTo<OnboardingRoute> { inclusive = true } }
                },
            )
        }
        composable<HomeRoute> { entry ->
            MainScreen(
                initialTab = if (entry.toRoute<HomeRoute>().openFavorites) MainTab.FAVORITES else MainTab.HOME,
                onHomeAction = { action ->
                    when (action) {
                        HomeAction.LOG_MEAL -> navController.navigate(LogMealRoute)
                        HomeAction.LOG_SYMPTOM -> navController.navigate(LogSymptomRoute)
                        HomeAction.DAY_CHECK -> navController.navigate(DayCheckRoute)
                        HomeAction.RESULTS -> navController.navigate(ResultsRoute)
                    }
                },
                profileExtra = {
                    ReminderSection()
                    ExportSection()
                },
            )
        }
        composable<LogMealRoute> { LogMealScreen(onBack = back) }
        composable<LogSymptomRoute> { LogSymptomScreen(onBack = back) }
        composable<DayCheckRoute> { DayCheckScreen(onDone = back) }
        composable<ResultsRoute> { ResultsScreen(onBack = back) }
    }
}
