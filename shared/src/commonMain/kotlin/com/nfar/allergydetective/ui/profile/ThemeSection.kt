package com.nfar.allergydetective.ui.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contrast
import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.theme_dark
import allergydetective.shared.generated.resources.theme_hint
import allergydetective.shared.generated.resources.theme_light
import allergydetective.shared.generated.resources.theme_auto
import allergydetective.shared.generated.resources.theme_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.nfar.allergydetective.ui.common.ChoiceRow
import com.nfar.allergydetective.ui.common.ChoicePill
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.domain.reminder.ThemeMode
import com.nfar.allergydetective.ui.common.SectionTitle
import com.nfar.allergydetective.ui.theme.ThemeController
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Profile: "Automatic · Light · Dark" (feedback 19.11.: dark mode for the night). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeSection(controller: ThemeController = koinInject(), inGroup: Boolean = false) {
    val mode by controller.mode.collectAsState()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (inGroup) GroupSubtitle(stringResource(Res.string.theme_title)) else SectionTitle(stringResource(Res.string.theme_title), Icons.Outlined.Contrast)
        // User note 22.11.2026: the three options in one line.
        val labels = ThemeMode.entries.map { option ->
            stringResource(
                when (option) {
                    ThemeMode.AUTO -> Res.string.theme_auto
                    ThemeMode.LIGHT -> Res.string.theme_light
                    ThemeMode.DARK -> Res.string.theme_dark
                },
            )
        }
        ChoiceRow(labels, ThemeMode.entries.indexOf(mode), { controller.onModeChange(ThemeMode.entries[it]) })
        Text(stringResource(Res.string.theme_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
