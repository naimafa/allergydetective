package com.nfar.allergydetective

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nfar.allergydetective.ui.AppNavHost
import com.nfar.allergydetective.ui.theme.AppTheme

/**
 * Root composable for Android and iOS. Koin must be started before (see `startApp`).
 * [openDayCheck] is true when the app was opened from the evening reminder.
 * The surface paints the theme background, also while the first screen is still loading.
 */
@Composable
fun App(openDayCheck: Boolean = false, onDayCheckOpened: () -> Unit = {}) {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavHost(openDayCheck = openDayCheck, onDayCheckOpened = onDayCheckOpened)
        }
    }
}
