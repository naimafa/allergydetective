package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.allergies_none_short
import allergydetective.shared.generated.resources.allergies_section_title
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.displayName
import allergydetective.shared.generated.resources.profile_group_health
import androidx.compose.material.icons.outlined.HealthAndSafety
import com.nfar.allergydetective.ui.theme.AppColors
import com.nfar.allergydetective.ui.sick.SickPeriodSection
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.onboarding_purpose
import allergydetective.shared.generated.resources.onboarding_purpose_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.ui.common.SectionTitle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Tab "Profil": settings ([extraContent], e.g. reminder, theme, export) on top, then the
 * known allergies and the purpose notice. [extraContent] is filled by
 * later features such as the reminder settings.
 */
@Composable
fun ProfileScreen(
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KnownAllergiesViewModel = koinViewModel(),
    extraContent: @Composable () -> Unit = {},
    /** Cards after the known allergies, e.g. privacy and the intro (user 23.11.2026: allergies before privacy). */
    lowerContent: @Composable () -> Unit = {},
    /** Opens the known allergies on their own page. */
    onOpenAllergies: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Back from the allergies page: show what was saved there.
    LaunchedEffect(Unit) { viewModel.refresh() }
    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.onSavedHandled()
            onSaved()
        }
    }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Settings first: the allergy list is long and hid everything below it (feedback 20.11.).
        extraContent()
        // User 23.11.2026 (profile "überladen"): illness and allergies together in one card "Meine Gesundheit".
        ProfileCard(ProfileTint.SAGE) {
            SectionTitle(stringResource(Res.string.profile_group_health), Icons.Outlined.HealthAndSafety)
            GroupPart { SickPeriodSection(koinViewModel(), inGroup = true) }
            // User 23.11.2026: like "Datenschutz" a line with an arrow; the list is on its own page.
            val names = state.properties.filter { it.id in state.selected }.map { displayName(NamePrefix.PROPERTY, it.key) }
            GroupPart { ProfileRow(stringResource(Res.string.allergies_section_title), names.joinToString(", ").ifEmpty { stringResource(Res.string.allergies_none_short) }, onClick = onOpenAllergies) }
        }
        lowerContent()
    }
}
