package com.nfar.allergydetective.platform

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    /** The phone's own language, kept so "like the phone" can go back to it. */
    private var default: Locale? = null

    actual val current: String
        @Composable get() = Locale.getDefault().toString()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val base = LocalConfiguration.current
        if (default == null) default = Locale.getDefault()
        val locale = if (value == null) default!! else Locale.forLanguageTag(value)
        Locale.setDefault(locale)
        val configuration = Configuration(base).apply { setLocale(locale) }
        // Strings read outside Compose (e.g. the reminder notification) follow too.
        val resources = LocalContext.current.resources
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalConfiguration.provides(configuration)
    }
}
