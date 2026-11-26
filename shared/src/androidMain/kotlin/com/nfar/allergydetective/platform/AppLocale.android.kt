package com.nfar.allergydetective.platform

import android.content.res.Resources
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    actual val current: String
        @Composable get() = Locale.getDefault().toString()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val base = LocalConfiguration.current
        // User 26.11.2026: phone switched to Turkish, the app stayed German. The phone language was kept from the
        // first start; read it fresh from the system each time instead (Locale.getDefault() is overwritten below).
        val locale = if (value == null) Resources.getSystem().configuration.locales[0] else Locale.forLanguageTag(value)
        Locale.setDefault(locale)
        val configuration = Configuration(base).apply { setLocale(locale) }
        // Strings read outside Compose (e.g. the reminder notification) follow too.
        val resources = LocalContext.current.resources
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalConfiguration.provides(configuration)
    }
}
