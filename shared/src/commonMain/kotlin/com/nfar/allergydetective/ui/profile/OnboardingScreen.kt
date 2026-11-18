package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.home_title
import allergydetective.shared.generated.resources.onboarding_back
import allergydetective.shared.generated.resources.onboarding_dishes_later
import allergydetective.shared.generated.resources.onboarding_dishes_start
import allergydetective.shared.generated.resources.onboarding_dishes_text
import allergydetective.shared.generated.resources.onboarding_dishes_title
import allergydetective.shared.generated.resources.onboarding_how_check
import allergydetective.shared.generated.resources.onboarding_how_meal
import allergydetective.shared.generated.resources.onboarding_how_symptom
import allergydetective.shared.generated.resources.onboarding_how_title
import allergydetective.shared.generated.resources.onboarding_next
import allergydetective.shared.generated.resources.onboarding_patience_text
import allergydetective.shared.generated.resources.onboarding_patience_title
import allergydetective.shared.generated.resources.onboarding_purpose
import allergydetective.shared.generated.resources.onboarding_purpose_title
import allergydetective.shared.generated.resources.onboarding_welcome
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val PAGE_WELCOME = 0
private const val PAGE_HOW = 1
private const val PAGE_PATIENCE = 2
private const val PAGE_ALLERGIES = 3
private const val PAGE_DISHES = 4
private const val PAGE_COUNT = 5

/**
 * First start (TRD 3.1, self-test 18.11.): a few short pages that introduce the app,
 * the known allergies, and at the end the tip to save frequent dishes.
 * [onFinished] gets true when the user wants to add dishes right away.
 */
@Composable
fun OnboardingScreen(onFinished: (openDishes: Boolean) -> Unit, viewModel: KnownAllergiesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var page by remember { mutableIntStateOf(PAGE_WELCOME) }
    // Saving the allergies completes the onboarding; the dishes tip follows.
    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.onSavedHandled()
            page = PAGE_DISHES
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(Res.string.home_title), style = MaterialTheme.typography.headlineMedium)
        PageDots(page)
        when (page) {
            PAGE_WELCOME -> {
                Text(stringResource(Res.string.onboarding_welcome), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(Res.string.onboarding_purpose_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(Res.string.onboarding_purpose), style = MaterialTheme.typography.bodyMedium)
            }
            PAGE_HOW -> {
                Text(stringResource(Res.string.onboarding_how_title), style = MaterialTheme.typography.titleLarge)
                Step("🍽️", Res.string.onboarding_how_meal)
                Step("🤒", Res.string.onboarding_how_symptom)
                Step("🌙", Res.string.onboarding_how_check)
            }
            PAGE_PATIENCE -> {
                Text(stringResource(Res.string.onboarding_patience_title), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(Res.string.onboarding_patience_text), style = MaterialTheme.typography.bodyLarge)
            }
            PAGE_ALLERGIES -> KnownAllergiesContent(state, viewModel::onToggle)
            PAGE_DISHES -> {
                Text(stringResource(Res.string.onboarding_dishes_title), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(Res.string.onboarding_dishes_text), style = MaterialTheme.typography.bodyLarge)
            }
        }
        when (page) {
            PAGE_ALLERGIES -> Button(
                onClick = viewModel::onSave,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) { Text(stringResource(Res.string.onboarding_next)) }
            PAGE_DISHES -> {
                Button(onClick = { onFinished(true) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                    Text(stringResource(Res.string.onboarding_dishes_start))
                }
                OutlinedButton(onClick = { onFinished(false) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(stringResource(Res.string.onboarding_dishes_later))
                }
            }
            else -> Button(onClick = { page++ }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(stringResource(Res.string.onboarding_next))
            }
        }
        // Back only before the allergies are saved; afterwards the onboarding is complete.
        if (page in (PAGE_WELCOME + 1)..PAGE_ALLERGIES) {
            TextButton(onClick = { page-- }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.onboarding_back)) }
        }
    }
}

@Composable
private fun Step(emoji: String, text: StringResource) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Text(emoji, style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(text), style = MaterialTheme.typography.bodyLarge)
    }
}

/** Shows where the user is in the intro. */
@Composable
private fun PageDots(page: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(PAGE_COUNT) { index ->
            val color = if (index == page) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            Box(Modifier.size(10.dp).background(color, CircleShape))
        }
    }
}
