package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.language_hint
import allergydetective.shared.generated.resources.language_system
import allergydetective.shared.generated.resources.language_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.ui.common.AppFilterChip
import com.nfar.allergydetective.ui.common.SectionTitle
import com.nfar.allergydetective.ui.theme.LanguageController
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.intl.Locale
import org.koin.compose.koinInject

/**
 * Profile: app language (user note 21.11.2026: at least German, English, French and Arabic, changeable here,
 * independent of the phone). Each language is written in itself, so it can be found whatever is shown now.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguageSection(controller: LanguageController = koinInject()) {
    val language by controller.language.collectAsState()
    // User 23.11.2026: with "Wie Handy" the language that is actually shown is marked too.
    val shown = Locale.current.language.takeIf { it in LanguageController.SUPPORTED } ?: FALLBACK_LANGUAGE
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(Res.string.language_title))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppFilterChip(
                selected = language == null,
                onClick = { controller.onLanguageChange(null) },
                label = { Text(stringResource(Res.string.language_system)) },
                showCheck = false,
            )
            LanguageController.SUPPORTED.forEach { code ->
                AppFilterChip(
                    selected = language == code || (language == null && code == shown),
                    onClick = { controller.onLanguageChange(code) },
                    label = { Text(OWN_NAMES.getValue(code)) },
                    showCheck = false,
                )
            }
        }
        Text(stringResource(Res.string.language_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Every language in its own name. */
/** The language shown when the phone has none of ours (the default resources are English). */
private const val FALLBACK_LANGUAGE = "en"

private val OWN_NAMES = mapOf("de" to "Deutsch", "en" to "English", "fr" to "Français", "ar" to "العربية")
