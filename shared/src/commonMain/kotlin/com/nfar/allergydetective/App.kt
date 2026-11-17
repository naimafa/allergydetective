package com.nfar.allergydetective

import androidx.compose.runtime.Composable
import com.nfar.allergydetective.ui.AppNavHost
import com.nfar.allergydetective.ui.theme.AppTheme

/**
 * Root composable for Android and iOS. Koin must be started before (see `startApp`).
 * [openDayCheck] is true when the app was opened from the evening reminder.
 */
@Composable
fun App(openDayCheck: Boolean = false, onDayCheckOpened: () -> Unit = {}) {
    AppTheme {
        AppNavHost(openDayCheck = openDayCheck, onDayCheckOpened = onDayCheckOpened)
    }
}
