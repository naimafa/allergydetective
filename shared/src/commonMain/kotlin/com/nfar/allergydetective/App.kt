package com.nfar.allergydetective

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.nfar.allergydetective.domain.reminder.ThemeMode
import com.nfar.allergydetective.ui.AppNavHost
import com.nfar.allergydetective.ui.theme.AppTheme
import com.nfar.allergydetective.ui.theme.ThemeController
import org.koin.compose.koinInject

/**
 * Root composable for Android and iOS. Koin must be started before (see `startApp`).
 * [openDayCheck] is true when the app was opened from the evening reminder.
 * The surface paints the theme background, also while the first screen is still loading.
 * [onDarkThemeChange] lets the platform match its status bar to the chosen look.
 */
@Composable
fun App(
    openDayCheck: Boolean = false,
    onDayCheckOpened: () -> Unit = {},
    onDarkThemeChange: (Boolean) -> Unit = {},
    themeController: ThemeController = koinInject(),
) {
    val mode by themeController.mode.collectAsState()
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    LaunchedEffect(dark) { onDarkThemeChange(dark) }
    AppTheme(darkTheme = dark) {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavHost(openDayCheck = openDayCheck, onDayCheckOpened = onDayCheckOpened)
        }
    }
}
