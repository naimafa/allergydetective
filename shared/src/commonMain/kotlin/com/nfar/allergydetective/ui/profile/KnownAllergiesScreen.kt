package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.allergies_section_title
import allergydetective.shared.generated.resources.common_save
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.ui.common.ScreenScaffold
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Known allergies on their own page (user 23.11.2026: like "Datenschutz", a line with an arrow in the profile
 * that opens the whole list). Saving goes back to the profile.
 */
@Composable
fun KnownAllergiesScreen(onBack: () -> Unit, viewModel: KnownAllergiesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.onSavedHandled()
            onBack()
        }
    }
    ScreenScaffold(
        title = stringResource(Res.string.allergies_section_title),
        onBack = onBack,
        snackbarHostState = remember { SnackbarHostState() },
        bottomBar = {
            Button(
                onClick = viewModel::onSave,
                // User 23.11.2026: strong green only when something changed.
                enabled = !state.loading && state.changed,
                shape = CircleShape,
                modifier = Modifier.navigationBarsPadding().fillMaxWidth().padding(16.dp).heightIn(min = 56.dp),
            ) { Text(stringResource(Res.string.common_save)) }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KnownAllergiesContent(state, viewModel::onToggle)
        }
    }
}
