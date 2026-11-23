package com.nfar.allergydetective

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.key
import androidx.compose.runtime.CompositionLocalProvider
import com.nfar.allergydetective.platform.LocalAppLocale
import com.nfar.allergydetective.ui.theme.LanguageController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.produceState
import com.nfar.allergydetective.domain.reminder.ThemeMode
import com.nfar.allergydetective.domain.time.AppClock
import kotlinx.coroutines.delay

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
    languageController: LanguageController = koinInject(),
    clock: AppClock = koinInject(),
) {
    val mode by themeController.mode.collectAsState()
    // Automatic: looked at once a minute, so the app turns dark at 20:00 while it is open.
    val night by produceState(initialValue = clock.isNightNow()) {
        while (true) {
            value = clock.isNightNow()
            delay(60_000)
        }
    }
    val dark = when (mode) {
        ThemeMode.AUTO -> night || isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    LaunchedEffect(dark) { onDarkThemeChange(dark) }
    // User note 21.11.2026: the language chosen in the profile; Arabic is laid out right to left.
    val language by languageController.language.collectAsState()
    CompositionLocalProvider(LocalAppLocale provides language) {
        val rtl = LanguageController.isRtl(language ?: Locale.current.language)
        CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
            // A new language rebuilds the screens, so every text is read again.
            key(language) {
                AppTheme(darkTheme = dark) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavHost(openDayCheck = openDayCheck, onDayCheckOpened = onDayCheckOpened)
                    }
                }
            }
        }
    }
}

private fun AppClock.isNightNow(): Boolean = ThemeMode.isNight(localTime(nowMillis()).first)
