package com.nfar.allergydetective.ui

import com.nfar.allergydetective.ui.profile.KnownAllergiesScreen
import com.nfar.allergydetective.ui.profile.GroupPart
import allergydetective.shared.generated.resources.common_done
import allergydetective.shared.generated.resources.onboarding_purpose
import allergydetective.shared.generated.resources.onboarding_purpose_title
import allergydetective.shared.generated.resources.profile_privacy_hint
import allergydetective.shared.generated.resources.profile_group_about
import allergydetective.shared.generated.resources.profile_group_settings
import com.nfar.allergydetective.ui.theme.AppColors
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Tune
import com.nfar.allergydetective.ui.profile.ProfileRow
import com.nfar.allergydetective.ui.profile.LanguageRow
import androidx.compose.material.icons.outlined.WavingHand
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import com.nfar.allergydetective.ui.profile.PaywallPreviewScreen
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

/** Payment page as a test view only (user 23.11.2026), reached by a long press in the profile. */
@Serializable
data object PaywallPreviewRoute

/** Known allergies on their own page (user 23.11.2026). */
@Serializable
data object AllergiesRoute

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
                    // User 23.11.2026 (profile "überladen"): language, look and reminder in one card.
                    ProfileCard(ProfileTint.WARM) {
                        SectionTitle(stringResource(Res.string.profile_group_settings), Icons.Outlined.Tune)
                        GroupPart { LanguageRow() }
                        GroupPart { ThemeSection(inGroup = true) }
                        GroupPart { ReminderSection(inGroup = true) }
                    }
                },
                onOpenAllergies = { navController.navigate(AllergiesRoute) },
                profileLower = {
                    // User 23.11.2026: privacy, intro and purpose as lines of one card "App-Info & Rechtliches".
                    // A long press on "Was die App ist und was nicht" opens the hidden payment test view.
                    var showPurpose by remember { mutableStateOf(false) }
                    ProfileCard(ProfileTint.WARM) {
                        SectionTitle(stringResource(Res.string.profile_group_about), Icons.Outlined.Info)
                        GroupPart { ProfileRow(stringResource(Res.string.privacy_title), stringResource(Res.string.profile_privacy_hint), onClick = { navController.navigate(PrivacyRoute) }) }
                        GroupPart { ProfileRow(stringResource(Res.string.welcome_title), stringResource(Res.string.profile_show_intro), onClick = { navController.navigate(IntroRoute) }) }
                        GroupPart { ProfileRow(
                            stringResource(Res.string.onboarding_purpose_title),
                            null,
                            onClick = { showPurpose = true },
                            onLongClick = { navController.navigate(PaywallPreviewRoute) },
                        ) }
                    }
                    if (showPurpose) {
                        AlertDialog(
                            onDismissRequest = { showPurpose = false },
                            title = { Text(stringResource(Res.string.onboarding_purpose_title)) },
                            text = { Text(stringResource(Res.string.onboarding_purpose)) },
                            confirmButton = { Button(onClick = { showPurpose = false }, shape = CircleShape) { Text(stringResource(Res.string.common_done)) } },
                        )
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
        composable<PaywallPreviewRoute> { PaywallPreviewScreen(onBack = back) }
        composable<AllergiesRoute> { KnownAllergiesScreen(onBack = back) }
    }
}
