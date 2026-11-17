package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.home_title
import allergydetective.shared.generated.resources.onboarding_done
import allergydetective.shared.generated.resources.onboarding_next
import allergydetective.shared.generated.resources.onboarding_purpose
import allergydetective.shared.generated.resources.onboarding_purpose_title
import allergydetective.shared.generated.resources.onboarding_welcome
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** First start (TRD 3.1): purpose of the app, then known allergies. */
@Composable
fun OnboardingScreen(onFinished: () -> Unit, viewModel: KnownAllergiesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var page by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.onSavedHandled()
            onFinished()
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(Res.string.home_title), style = MaterialTheme.typography.headlineMedium)
        if (page == 0) {
            Text(stringResource(Res.string.onboarding_welcome), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(Res.string.onboarding_purpose_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(Res.string.onboarding_purpose), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = { page = 1 }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(stringResource(Res.string.onboarding_next))
            }
        } else {
            KnownAllergiesContent(state, viewModel::onToggle)
            Button(
                onClick = viewModel::onSave,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) { Text(stringResource(Res.string.onboarding_done)) }
        }
    }
}
