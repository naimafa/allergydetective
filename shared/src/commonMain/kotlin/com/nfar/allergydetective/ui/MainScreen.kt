package com.nfar.allergydetective.ui

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_saved
import allergydetective.shared.generated.resources.tab_favorites
import allergydetective.shared.generated.resources.tab_history
import allergydetective.shared.generated.resources.tab_home
import allergydetective.shared.generated.resources.tab_profile
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.nfar.allergydetective.ui.home.HomeProgress
import com.nfar.allergydetective.ui.home.HomeScreen
import com.nfar.allergydetective.ui.home.HomeViewModel
import com.nfar.allergydetective.ui.profile.ProfileScreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Bottom tabs (UX 6). Emojis stand in for icons until the design phase. */
enum class MainTab(val icon: String, val label: StringResource) {
    HOME("🏠", Res.string.tab_home),
    HISTORY("📖", Res.string.tab_history),
    FAVORITES("⭐", Res.string.tab_favorites),
    PROFILE("👤", Res.string.tab_profile),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onHomeAction: (HomeAction) -> Unit, profileExtra: @Composable () -> Unit = {}) {
    var tab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = { if (tab != MainTab.HOME) TopAppBar(title = { Text(stringResource(tab.label)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { entry ->
                    NavigationBarItem(
                        selected = tab == entry,
                        onClick = { tab = entry },
                        icon = { Text(entry.icon) },
                        label = { Text(stringResource(entry.label)) },
                    )
                }
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (tab) {
            MainTab.HOME -> {
                val homeViewModel: HomeViewModel = koinViewModel()
                val confirmedDays by homeViewModel.confirmedDays.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { homeViewModel.refresh() }
                HomeScreen(
                    modifier = modifier,
                    header = { confirmedDays?.let { HomeProgress(it) } },
                    onAction = onHomeAction,
                )
            }
            MainTab.HISTORY -> HistoryScreen(modifier = modifier)
            MainTab.FAVORITES -> FavoritesScreen(modifier = modifier)
            MainTab.PROFILE -> ProfileScreen(
                modifier = modifier,
                onSaved = { scope.launch { snackbarHostState.showSnackbar(getString(Res.string.common_saved)) } },
                extraContent = profileExtra,
            )
        }
    }
}
