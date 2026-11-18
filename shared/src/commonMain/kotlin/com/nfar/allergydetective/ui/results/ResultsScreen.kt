package com.nfar.allergydetective.ui.results

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.home_results
import allergydetective.shared.generated.resources.results_allergy_notice
import allergydetective.shared.generated.resources.results_bad_sleep
import allergydetective.shared.generated.resources.results_disclaimer
import allergydetective.shared.generated.resources.results_group
import allergydetective.shared.generated.resources.results_groups_title
import allergydetective.shared.generated.resources.results_high_stress
import allergydetective.shared.generated.resources.results_level_strong
import allergydetective.shared.generated.resources.results_level_suspicion
import allergydetective.shared.generated.resources.results_level_trace
import allergydetective.shared.generated.resources.results_local
import allergydetective.shared.generated.resources.results_nothing_yet
import allergydetective.shared.generated.resources.results_reason
import allergydetective.shared.generated.resources.results_safe_title
import allergydetective.shared.generated.resources.results_too_early
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nfar.allergydetective.ui.theme.AppColors
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.domain.analysis.model.EvidenceLevel
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.ScreenScaffold
import com.nfar.allergydetective.ui.common.SectionTitle
import com.nfar.allergydetective.ui.common.displayName
import com.nfar.allergydetective.ui.meal.ingredientName
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ResultsScreen(onBack: () -> Unit, viewModel: ResultsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ScreenScaffold(
        title = stringResource(Res.string.home_results),
        onBack = onBack,
        snackbarHostState = remember { SnackbarHostState() },
    ) { padding ->
        if (state.loading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding).padding(32.dp))
        } else {
            ResultsContent(state, Modifier.padding(padding))
        }
    }
}

@Composable
private fun ResultsContent(state: ResultsState, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.confirmedDays < ResultsViewModel.FIRST_EVALUATION_DAYS) {
            item {
                InfoCard(
                    text = stringResource(Res.string.results_too_early, state.confirmedDays, ResultsViewModel.FIRST_EVALUATION_DAYS),
                    container = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
            }
        }
        if (state.knownAllergyIngredients.isNotEmpty()) {
            item {
                InfoCard(
                    text = stringResource(Res.string.results_allergy_notice, state.knownAllergyIngredients.map { labelText(it) }.joinToString(", ")),
                    container = AppColors.extra.redFlag,
                )
            }
        }
        if (state.items.isEmpty()) {
            item { Text(stringResource(Res.string.results_nothing_yet), style = MaterialTheme.typography.bodyLarge) }
        }
        items(state.items) { ResultCard(it) }
        if (state.groups.isNotEmpty()) {
            item { SectionTitle(stringResource(Res.string.results_groups_title)) }
            items(state.groups) { group ->
                InfoCard(
                    text = stringResource(Res.string.results_group, group.map { labelText(it) }.joinToString(" + ")),
                    container = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
            }
        }
        if (state.safeIngredients.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(Res.string.results_safe_title, state.safeIngredients.size), style = MaterialTheme.typography.titleMedium)
                        Text(state.safeIngredients.map { labelText(it) }.joinToString(", "))
                    }
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.results_disclaimer), style = MaterialTheme.typography.bodySmall)
                Text(
                    stringResource(Res.string.results_local),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoCard(text: String, container: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = container), modifier = Modifier.fillMaxWidth()) {
        Text(text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

/** Stronger evidence = warmer colour: strong = coral, suspicion = amber, trace = neutral. */
@Composable
private fun ResultCard(item: ResultItem) {
    val container = when (item.level) {
        EvidenceLevel.STRONG_SUSPICION -> MaterialTheme.colorScheme.tertiaryContainer
        EvidenceLevel.SUSPICION -> AppColors.extra.digestive
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = container, contentColor = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(levelText(item.level), style = MaterialTheme.typography.labelLarge)
            Text(labelText(item.label), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(Res.string.results_reason, item.withComplaints, item.exposures, item.otherWithComplaints, item.otherExposures), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun levelText(level: EvidenceLevel): String = stringResource(
    when (level) {
        EvidenceLevel.STRONG_SUSPICION -> Res.string.results_level_strong
        EvidenceLevel.SUSPICION -> Res.string.results_level_suspicion
        else -> Res.string.results_level_trace
    },
)

@Composable
private fun labelText(label: CandidateLabel): String = when (label) {
    is CandidateLabel.Ingredient -> ingredientName(label.ingredient)
    is CandidateLabel.Property -> displayName(NamePrefix.PROPERTY, label.key)
    is CandidateLabel.Factor -> displayName(NamePrefix.FACTOR, label.key)
    CandidateLabel.BadSleep -> stringResource(Res.string.results_bad_sleep)
    CandidateLabel.HighStress -> stringResource(Res.string.results_high_stress)
}
