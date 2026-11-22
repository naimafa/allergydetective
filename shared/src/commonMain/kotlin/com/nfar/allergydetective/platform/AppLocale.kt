package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

/**
 * Overrides the language of the app's resources (pattern from the Compose Multiplatform docs, "Changing the
 * locale"): [provides] with a language tag switches strings, plurals and formats; null goes back to the phone.
 */
expect object LocalAppLocale {
    val current: String @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}
