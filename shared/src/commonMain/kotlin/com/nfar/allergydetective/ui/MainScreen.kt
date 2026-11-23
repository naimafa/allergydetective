package com.nfar.allergydetective.ui

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_saved
import allergydetective.shared.generated.resources.tab_favorites
import allergydetective.shared.generated.resources.tab_history
import allergydetective.shared.generated.resources.tab_home
import allergydetective.shared.generated.resources.tab_profile
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfar.allergydetective.ui.theme.AppColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import com.nfar.allergydetective.ui.common.PageTitle
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.ui.favorites.FavoritesScreen
import com.nfar.allergydetective.ui.history.HistoryScreen
import com.nfar.allergydetective.ui.home.HomeAction
import com.nfar.allergydetective.ui.home.HomeScreen
import com.nfar.allergydetective.ui.home.homeBackground
import com.nfar.allergydetective.ui.home.HomeViewModel
import com.nfar.allergydetective.ui.sick.SickBanner
import com.nfar.allergydetective.ui.sick.SickPeriodsViewModel
import com.nfar.allergydetective.ui.profile.ProfileScreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Bottom tabs (UX 6). */
enum class MainTab(val icon: ImageVector, val label: StringResource, val selectedIcon: ImageVector = icon) {
    HOME(Icons.Outlined.Home, Res.string.tab_home, Icons.Filled.Home),
    HISTORY(Icons.Outlined.Schedule, Res.string.tab_history),
    FAVORITES(Icons.Outlined.StarOutline, Res.string.tab_favorites),
    PROFILE(Icons.Outlined.Person, Res.string.tab_profile),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onHomeAction: (HomeAction) -> Unit,
    /** Opens a logged meal from the history for editing. */
    onEditMeal: (Long) -> Unit = {},
    /** Opens the food log with a dish from the favourites. */
    onLogDish: (Long) -> Unit = {},
    /** Opens the day check of a day from the history. */
    onOpenDayCheck: (Long) -> Unit = {},
    /** History: correct a complaint, add food or a complaint for an earlier day (feedback 19.11.). */
    onEditSymptom: (Long) -> Unit = {},
    onAddMealForDay: (Int) -> Unit = {},
    onAddSymptomForDay: (Int) -> Unit = {},
    profileExtra: @Composable () -> Unit = {},
    initialTab: MainTab = MainTab.HOME,
) {
    var tab by rememberSaveable { mutableStateOf(initialTab) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        // Start screen: its cream page with the peach tint fills the whole screen.
        // User note 22.11.2026: every tab has the start screen background and a big title in its style.
        modifier = Modifier.homeBackground(),
        containerColor = Color.Transparent,
        topBar = { if (tab != MainTab.HOME) PageTitle(stringResource(tab.label), Modifier.statusBarsPadding().padding(top = 20.dp, bottom = 12.dp)) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Design of 21.11.2026: a cream bar, rounded on top with a soft shadow; the active tab in
            // forest green with a short line under its name instead of the pill.
            // (Dark mode feedback 19.11.: the bar stays clearly lighter than the page.)
            val ink = AppColors.extra.home.accent
            Surface(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = AppColors.extra.home.nav,
                shadowElevation = 8.dp,
            ) {
                // Picture of 21.11.2026: small labels; the system font scale is not applied to the four short names.
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, minOf(density.fontScale, 1f))) {
                NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
                    MainTab.entries.forEach { entry ->
                        val selected = tab == entry
                        NavigationBarItem(
                            selected = selected,
                            onClick = { tab = entry },
                            icon = { Icon(if (selected) entry.selectedIcon else entry.icon, contentDescription = null) },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(entry.label), fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
                                    Box(
                                        Modifier.padding(top = 4.dp).size(width = 40.dp, height = 3.dp)
                                            .background(if (selected) ink else Color.Transparent, RoundedCornerShape(2.dp)),
                                    )
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent,
                                selectedIconColor = ink,
                                selectedTextColor = ink,
                                unselectedIconColor = AppColors.extra.home.navInactive,
                                unselectedTextColor = AppColors.extra.home.navInactive,
                            ),
                        )
                    }
                }
                }
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (tab) {
            MainTab.HOME -> {
                val homeViewModel: HomeViewModel = koinViewModel()
                val homeState by homeViewModel.state.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { homeViewModel.refresh() }
                val sickViewModel: SickPeriodsViewModel = koinViewModel()
                val sickState by sickViewModel.state.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { sickViewModel.refresh() }
                HomeScreen(
                    modifier = modifier,
                    state = homeState,
                    onAction = onHomeAction,
                    onOpenToday = { tab = MainTab.HISTORY },
                    onOpenReminders = { tab = MainTab.PROFILE },
                    banner = { sickState.ongoing?.let { SickBanner(it, sickViewModel::onRecovered) } },
                )
            }
            MainTab.HISTORY -> HistoryScreen(
                onEditMeal = onEditMeal,
                onOpenDayCheck = onOpenDayCheck,
                onEditSymptom = onEditSymptom,
                onAddMealForDay = onAddMealForDay,
                onAddSymptomForDay = onAddSymptomForDay,
                modifier = modifier,
            )
            MainTab.FAVORITES -> FavoritesScreen(onLogDish = onLogDish, modifier = modifier)
            MainTab.PROFILE -> ProfileScreen(
                modifier = modifier,
                onSaved = { scope.launch { snackbarHostState.showSnackbar(getString(Res.string.common_saved)) } },
                extraContent = profileExtra,
            )
        }
    }
}
