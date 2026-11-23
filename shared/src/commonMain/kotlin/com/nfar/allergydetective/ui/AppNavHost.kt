package com.nfar.allergydetective.ui

import allergydetective.shared.generated.resources.welcome_text
import allergydetective.shared.generated.resources.welcome_title
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import allergydetective.shared.generated.resources.privacy_card_button
import allergydetective.shared.generated.resources.privacy_card_text
import allergydetective.shared.generated.resources.privacy_title
import androidx.compose.material3.MaterialTheme
import com.nfar.allergydetective.ui.profile.LanguageSection
import com.nfar.allergydetective.ui.common.SectionTitle
import com.nfar.allergydetective.ui.profile.PrivacyScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.ui.profile.ProfileCard
import com.nfar.allergydetective.ui.profile.ProfileTint
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
import com.nfar.allergydetective.ui.profile.ThemeSection
import com.nfar.allergydetective.ui.profile.DoctorReportSection
import com.nfar.allergydetective.ui.profile.GoalSection
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
    /** Add for an earlier day from the history (feedback 19.11.). */
    val daysAgo: Int? = null,
    /** A dish from the favourites to log (self-test 19.11.). */
    val favoriteId: Long? = null,
)

@Serializable
data class LogSymptomRoute(
    /** A complaint opened from the history for correcting it (feedback 19.11.). */
    val editEntryId: Long? = null,
    /** Add for an earlier day from the history. */
    val daysAgo: Int? = null,
)

@Serializable
data class DayCheckRoute(
    /** A day opened from the history (today or up to two days back), null = today. */
    val day: Long? = null,
)

@Serializable
data object ResultsRoute

@Serializable
data object OnboardingRoute

/** The intro pages again, opened from the profile (self-test 18.11.). */
@Serializable
data object IntroRoute

@Serializable
data object PrivacyRoute

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
            navController.navigate(DayCheckRoute()) { launchSingleTop = true }
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
                onLogDish = { navController.navigate(LogMealRoute(favoriteId = it)) },
                onOpenDayCheck = { navController.navigate(DayCheckRoute(day = it)) },
                onEditSymptom = { navController.navigate(LogSymptomRoute(editEntryId = it)) },
                onAddMealForDay = { navController.navigate(LogMealRoute(daysAgo = it)) },
                onAddSymptomForDay = { navController.navigate(LogSymptomRoute(daysAgo = it)) },
                onHomeAction = { action ->
                    when (action) {
                        HomeAction.LOG_MEAL -> navController.navigate(LogMealRoute())
                        HomeAction.LOG_SYMPTOM -> navController.navigate(LogSymptomRoute())
                        HomeAction.DAY_CHECK -> navController.navigate(DayCheckRoute())
                        HomeAction.RESULTS -> navController.navigate(ResultsRoute)
                    }
                },
                // User note 21.11.2026: each area in its own card.
                profileExtra = {
                    // User note 22.11.2026: apricot and sage in turn, like the tiles on the start screen.
                    // User 23.11.2026: the evaluation goal first; it decides what the start screen counts towards.
                    ProfileCard(ProfileTint.WARM) { GoalSection() }
                    ProfileCard(ProfileTint.SAGE) { DoctorReportSection() }
                    ProfileCard(ProfileTint.WARM) { LanguageSection() }
                    ProfileCard(ProfileTint.SAGE) { ThemeSection() }
                    ProfileCard(ProfileTint.WARM) { ReminderSection() }
                    ProfileCard(ProfileTint.SAGE) { SickPeriodSection(koinViewModel()) }
                },
                // User 23.11.2026: the known allergies come before privacy; these cards follow them.
                profileLower = {
                    // User note 21.11.2026: privacy details on their own screen, here only a short line.
                    ProfileCard(ProfileTint.SAGE) {
                        SectionTitle(stringResource(Res.string.privacy_title))
                        Text(stringResource(Res.string.privacy_card_text), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        // User pictures 23.11.2026: one clear round button per card.
                        Button(onClick = { navController.navigate(PrivacyRoute) }, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                            Text(stringResource(Res.string.privacy_card_button))
                        }
                    }
                    // User picture 23.11.2026: the intro in its own card "Willkommen" instead of a loose button.
                    ProfileCard(ProfileTint.WARM) {
                        SectionTitle(stringResource(Res.string.welcome_title))
                        Text(stringResource(Res.string.welcome_text), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { navController.navigate(IntroRoute) }, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                            Text(stringResource(Res.string.profile_show_intro))
                        }
                    }
                },
            )
        }
composable<IntroRoute> {            OnboardingScreen(                replay = true,                onFinished = { openDishes ->                    if (openDishes) {                        navController.navigate(HomeRoute(openFavorites = true)) { popUpTo<HomeRoute> { inclusive = true } }                    } else {                        navController.popBackStack()                    }                },            )        }
        composable<LogMealRoute> { entry -> entry.toRoute<LogMealRoute>().let { route -> LogMealScreen(onBack = back, editMealId = route.editMealId, favoriteId = route.favoriteId, daysAgo = route.daysAgo) } }
        composable<LogSymptomRoute> { entry ->
            entry.toRoute<LogSymptomRoute>().let { route -> LogSymptomScreen(onBack = back, editEntryId = route.editEntryId, daysAgo = route.daysAgo) }
        }
        composable<DayCheckRoute> { entry ->
            DayCheckScreen(
                onDone = back,
                day = entry.toRoute<DayCheckRoute>().day,
                onLogSymptom = { navController.navigate(LogSymptomRoute()) },
                // User note 21.11.2026: after finishing a sick day go straight to the symptom form for that day.
                onSickDayFinished = { daysAgo ->
                    navController.navigate(LogSymptomRoute(daysAgo = daysAgo)) { popUpTo<DayCheckRoute> { inclusive = true } }
                },
            )
        }
        composable<ResultsRoute> { ResultsScreen(onBack = back) }
        composable<PrivacyRoute> { PrivacyScreen(onBack = back) }
    }
}
