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
import com.nfar.allergydetective.ui.sick.SickPeriodSection
import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.profile_show_intro
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import kotlinx.serialization.Serializable

/** Type-safe routes. Each screen gets its own route when it is built. */
@Serializable
data class HomeRoute(
    /** After the intro: "add dishes now" opens the favorites tab. */
    val openFavorites: Boolean = false,
)

@Serializable
data class LogMealRoute(
    /** A logged meal opened from the history for editing (self-test 19.11.). */
    val editMealId: Long? = null,
)

@Serializable
data object LogSymptomRoute

@Serializable
data object DayCheckRoute

@Serializable
data object ResultsRoute

@Serializable
data object OnboardingRoute

/** The intro pages again, opened from the profile (self-test 18.11.). */
@Serializable
data object IntroRoute

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
                onEditMeal = { navController.navigate(LogMealRoute(editMealId = it)) },
                onHomeAction = { action ->
                    when (action) {
                        HomeAction.LOG_MEAL -> navController.navigate(LogMealRoute())
                        HomeAction.LOG_SYMPTOM -> navController.navigate(LogSymptomRoute)
                        HomeAction.DAY_CHECK -> navController.navigate(DayCheckRoute)
                        HomeAction.RESULTS -> navController.navigate(ResultsRoute)
                    }
                },
                profileExtra = {
                    ReminderSection()
                    SickPeriodSection(koinViewModel())
                    ExportSection()
                    OutlinedButton(
                        onClick = { navController.navigate(IntroRoute) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) { Text(stringResource(Res.string.profile_show_intro)) }
                },
            )
        }
composable<IntroRoute> {            OnboardingScreen(                replay = true,                onFinished = { openDishes ->                    if (openDishes) {                        navController.navigate(HomeRoute(openFavorites = true)) { popUpTo<HomeRoute> { inclusive = true } }                    } else {                        navController.popBackStack()                    }                },            )        }
        composable<LogMealRoute> { entry -> LogMealScreen(onBack = back, editMealId = entry.toRoute<LogMealRoute>().editMealId) }
        composable<LogSymptomRoute> { LogSymptomScreen(onBack = back) }
        composable<DayCheckRoute> { DayCheckScreen(onDone = back, onLogSymptom = { navController.navigate(LogSymptomRoute) }) }
        composable<ResultsRoute> { ResultsScreen(onBack = back) }
    }
}
