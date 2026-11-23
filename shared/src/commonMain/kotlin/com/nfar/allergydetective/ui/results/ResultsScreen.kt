package com.nfar.allergydetective.ui.results

import com.nfar.allergydetective.domain.analysis.SafeListThresholds
import allergydetective.shared.generated.resources.results_safe_hint
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.intl.Locale
import com.nfar.allergydetective.domain.reminder.SettingsStore
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.ui.common.formatDecimal
import org.koin.compose.koinInject
import allergydetective.shared.generated.resources.results_details
import allergydetective.shared.generated.resources.results_how_title
import allergydetective.shared.generated.resources.results_how
import allergydetective.shared.generated.resources.results_how_lift
import allergydetective.shared.generated.resources.results_how_limit
import allergydetective.shared.generated.resources.results_next_title
import allergydetective.shared.generated.resources.results_next_lifestyle
import allergydetective.shared.generated.resources.results_next_strong
import allergydetective.shared.generated.resources.results_next_suspicion
import allergydetective.shared.generated.resources.results_next_trace
import allergydetective.shared.generated.resources.results_focus_text
import allergydetective.shared.generated.resources.results_set_focus
import allergydetective.shared.generated.resources.results_focus_set
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ResultsScreen(onBack: () -> Unit, viewModel: ResultsViewModel = koinViewModel(), settings: SettingsStore = koinInject(), clock: AppClock = koinInject()) {
    val today = { clock.today() }
    var focusSet by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    focusSet?.let { focus ->
        val message = stringResource(Res.string.results_focus_set, focus)
        LaunchedEffect(focus) {
            snackbar.showSnackbar(message)
            focusSet = null
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    ScreenScaffold(
        title = stringResource(Res.string.home_results),
        onBack = onBack,
        snackbarHostState = snackbar,
    ) { padding ->
        if (state.loading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding).padding(32.dp))
        } else {
            ResultsContent(state, Modifier.padding(padding), onSetFocus = { focus ->
                // The focus starts a new stretch from today, so the next days show whether it helps.
                settings.saveGoal(settings.goal().copy(focus = focus, startDay = today()))
                focusSet = focus
            })
        }
    }
}

@Composable
private fun ResultsContent(state: ResultsState, modifier: Modifier, onSetFocus: (String) -> Unit) {
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
        items(state.items) { ResultCard(it, onSetFocus) }
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
                        // User note 21.11.2026: "gut vertragen" was unclear – in general, or from my own entries? Say it.
                        Text(
                            stringResource(Res.string.results_safe_hint, SafeListThresholds().minExposures),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        )
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

/**
 * Stronger evidence = warmer colour: strong = coral, suspicion = amber, trace = neutral.
 * User 23.11.2026: "Wie wurde das festgestellt mit Käse?" and "Maßnahmen – wie weiter machen, Verzicht".
 * A tap opens how the pattern was found and what can be done next; for a food the suspicion can become the
 * focus of the next stretch (profile goal) with one tap.
 */
@Composable
private fun ResultCard(item: ResultItem, onSetFocus: (String) -> Unit) {
    val container = when (item.level) {
        EvidenceLevel.STRONG_SUSPICION -> MaterialTheme.colorScheme.tertiaryContainer
        EvidenceLevel.SUSPICION -> AppColors.extra.digestive
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    var open by remember { mutableStateOf(false) }
    val name = labelText(item.label)
    // Phone test 23.11.: "Hartkäse gereift (Parmesan, Bergkäse, alter Gouda)" three times in the text was too long;
    // the explanation uses the name without the examples in brackets.
    val short = name.substringBefore(" (").ifBlank { name }
    Card(
        onClick = { open = !open },
        colors = CardDefaults.cardColors(containerColor = container, contentColor = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(levelText(item.level), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Icon(if (open) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown, contentDescription = stringResource(Res.string.results_details))
            }
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(Res.string.results_reason, item.withComplaints, item.exposures, item.otherWithComplaints, item.otherExposures), style = MaterialTheme.typography.bodyMedium)
            if (open) {
                Text(stringResource(Res.string.results_how_title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                Text(stringResource(Res.string.results_how, short, item.withComplaints, item.exposures, item.otherWithComplaints, item.otherExposures), style = MaterialTheme.typography.bodyMedium)
                // Without any complaints in the comparison the ratio is only an estimate ("2-mal" for 1 of 6 vs 0 of 12).
                if (item.lift >= 1.5 && item.otherWithComplaints > 0 && item.level >= EvidenceLevel.SUSPICION) {
                    Text(stringResource(Res.string.results_how_lift, formatDecimal(item.lift.toFloat(), Locale.current.language)), style = MaterialTheme.typography.bodyMedium)
                }
                Text(stringResource(Res.string.results_how_limit), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(Res.string.results_next_title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                val food = item.label is CandidateLabel.Ingredient || item.label is CandidateLabel.Property
                Text(
                    stringResource(
                        when {
                            !food -> Res.string.results_next_lifestyle
                            item.level == EvidenceLevel.STRONG_SUSPICION -> Res.string.results_next_strong
                            item.level == EvidenceLevel.SUSPICION -> Res.string.results_next_suspicion
                            else -> Res.string.results_next_trace
                        },
                        short,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (food && item.level >= EvidenceLevel.SUSPICION) {
                    val focus = stringResource(Res.string.results_focus_text, short)
                    OutlinedButton(
                        onClick = { onSetFocus(focus) },
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp).heightIn(min = 48.dp),
                    ) { Text(stringResource(Res.string.results_set_focus, focus)) }
                }
            }
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
