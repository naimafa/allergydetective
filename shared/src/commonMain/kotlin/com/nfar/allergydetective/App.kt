package com.nfar.allergydetective

import androidx.compose.runtime.Composable
import com.nfar.allergydetective.ui.AppNavHost
import com.nfar.allergydetective.ui.theme.AppTheme

/** Root composable for Android and iOS. Koin must be started before (see `startApp`). */
@Composable
fun App() {
    AppTheme {
        AppNavHost()
    }
}
