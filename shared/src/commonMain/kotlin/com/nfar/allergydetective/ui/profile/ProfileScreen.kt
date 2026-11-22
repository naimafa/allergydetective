package com.nfar.allergydetective.ui.profile

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
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
        // Folded by default (feedback 20.11.): one line with the selection, the list opens on tap.
        ProfileCard { KnownAllergiesSection(state, viewModel::onToggle, viewModel::onSave) }
        ProfileCard {
            SectionTitle(stringResource(Res.string.onboarding_purpose_title))
            Text(stringResource(Res.string.onboarding_purpose), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
