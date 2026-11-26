package com.nfar.allergydetective.ui.results

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.ingredient_search
import allergydetective.shared.generated.resources.experiment_title
import allergydetective.shared.generated.resources.experiment_intro
import allergydetective.shared.generated.resources.experiment_start
import allergydetective.shared.generated.resources.experiment_what
import allergydetective.shared.generated.resources.experiment_suggestions
import allergydetective.shared.generated.resources.experiment_how_long
import allergydetective.shared.generated.resources.experiment_days_n
import allergydetective.shared.generated.resources.experiment_since
import allergydetective.shared.generated.resources.experiment_before_days
import allergydetective.shared.generated.resources.experiment_before_few
import allergydetective.shared.generated.resources.experiment_begin
import allergydetective.shared.generated.resources.experiment_focus
import allergydetective.shared.generated.resources.experiment_running
import allergydetective.shared.generated.resources.experiment_over
import allergydetective.shared.generated.resources.experiment_before
import allergydetective.shared.generated.resources.experiment_during
import allergydetective.shared.generated.resources.experiment_rules
import allergydetective.shared.generated.resources.experiment_eaten
import allergydetective.shared.generated.resources.experiment_too_few
import allergydetective.shared.generated.resources.experiment_less
import allergydetective.shared.generated.resources.experiment_similar
import allergydetective.shared.generated.resources.experiment_more
import allergydetective.shared.generated.resources.experiment_stop
import allergydetective.shared.generated.resources.experiment_new
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.experiment.Experiment
import com.nfar.allergydetective.domain.experiment.ExperimentResult
import com.nfar.allergydetective.domain.experiment.ExperimentVerdict
import com.nfar.allergydetective.domain.experiment.MIN_COUNTED_DAYS
import com.nfar.allergydetective.ui.common.DayPicker
import com.nfar.allergydetective.ui.common.AppFilterChip
import com.nfar.allergydetective.ui.meal.IngredientBadge
import com.nfar.allergydetective.ui.meal.ingredientName
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Weglass-Test (user 25.11.2026) at the top of "Muster entdecken": start one, follow it day by day, and read the
 * comparison of the days before with the days without the food.
 */
@Composable
fun ExperimentCard(viewModel: ExperimentViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }
    if (state.loading) return
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.experiment_title), style = MaterialTheme.typography.titleMedium)
            val experiment = state.experiment
            val result = state.result
            val ingredient = state.ingredient
            if (experiment == null || result == null || ingredient == null) {
                Text(stringResource(Res.string.experiment_intro), style = MaterialTheme.typography.bodyMedium)
                Button(onClick = viewModel::onOpenSetup, shape = CircleShape, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(Res.string.experiment_start))
                }
            } else {
                RunningOrDone(experiment, result, ingredientName(ingredient), viewModel)
            }
        }
    }
    state.setup?.let { SetupDialog(it, state.suggestions, state.today, viewModel) }
}

@Composable
private fun RunningOrDone(experiment: Experiment, result: ExperimentResult, name: String, viewModel: ExperimentViewModel) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    if (result.over) {
        Text(stringResource(Res.string.experiment_over, name), style = MaterialTheme.typography.titleSmall)
    } else {
        Text(stringResource(Res.string.experiment_running, name, result.dayNumber, experiment.days), style = MaterialTheme.typography.titleSmall)
        LinearProgressIndicator(progress = { result.dayNumber.toFloat() / experiment.days }, modifier = Modifier.fillMaxWidth())
    }
    Text(stringResource(Res.string.experiment_before, result.before.complaintDays, result.before.days), style = MaterialTheme.typography.bodyMedium)
    Text(stringResource(Res.string.experiment_during, name, result.during.complaintDays, result.during.days), style = MaterialTheme.typography.bodyMedium)
    if (result.eatenDays > 0) {
        Text(stringResource(Res.string.experiment_eaten, name, result.eatenDays), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
    val verdict = when (result.verdict) {
        ExperimentVerdict.TOO_FEW_DAYS -> stringResource(Res.string.experiment_too_few)
        ExperimentVerdict.LESS -> stringResource(Res.string.experiment_less, name)
        ExperimentVerdict.SIMILAR -> stringResource(Res.string.experiment_similar, name)
        ExperimentVerdict.MORE -> stringResource(Res.string.experiment_more, name)
    }
    Text(verdict, style = MaterialTheme.typography.bodyMedium)
    Text(stringResource(Res.string.experiment_rules), style = MaterialTheme.typography.bodySmall, color = muted)
    if (result.over) {
        Button(onClick = viewModel::onNew, shape = CircleShape, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(Res.string.experiment_new)) }
    } else {
        OutlinedButton(onClick = viewModel::onStop, shape = CircleShape, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(Res.string.experiment_stop)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetupDialog(setup: ExperimentSetup, suggestions: List<IngredientInfo>, today: Long, viewModel: ExperimentViewModel) {
    val chosen = setup.chosen
    val focus = chosen?.let { stringResource(Res.string.experiment_focus, ingredientName(it)) }
    AlertDialog(
        onDismissRequest = viewModel::onCancelSetup,
        title = { Text(stringResource(Res.string.experiment_start)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(Res.string.experiment_what), style = MaterialTheme.typography.titleSmall)
                chosen?.let { IngredientLine(it) }
                if (chosen == null && suggestions.isNotEmpty()) {
                    Text(stringResource(Res.string.experiment_suggestions), style = MaterialTheme.typography.bodySmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        suggestions.forEach { s -> AppFilterChip(selected = false, onClick = { viewModel.onChoose(s) }, label = { Text(ingredientName(s)) }) }
                    }
                }
                OutlinedTextField(
                    value = setup.query,
                    onValueChange = viewModel::onQuery,
                    label = { Text(stringResource(Res.string.ingredient_search)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                setup.results.forEach { result ->
                    TextButton(onClick = { viewModel.onChoose(result) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { IngredientLine(result) }
                }
                Text(stringResource(Res.string.experiment_how_long), style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Experiment.DURATIONS.forEach { days ->
                        AppFilterChip(selected = setup.days == days, onClick = { viewModel.onDays(days) }, label = { Text(stringResource(Res.string.experiment_days_n, days)) })
                    }
                }
                Text(stringResource(Res.string.experiment_since), style = MaterialTheme.typography.titleSmall)
                // User 25.11.2026: any day of the last two weeks, with the same day row as the forms.
                DayPicker(
                    daysAgo = setup.sinceDaysAgo,
                    today = today,
                    onSelect = viewModel::onSince,
                    maxDaysBack = ExperimentViewModel.MAX_START_DAYS_BACK,
                )
                Text(
                    stringResource(Res.string.experiment_before_days, setup.beforeDays),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (setup.beforeDays < MIN_COUNTED_DAYS) {
                    Text(stringResource(Res.string.experiment_before_few), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = { focus?.let(viewModel::onBegin) }, enabled = chosen != null, shape = CircleShape) { Text(stringResource(Res.string.experiment_begin)) }
        },
        dismissButton = { TextButton(onClick = viewModel::onCancelSetup) { Text(stringResource(Res.string.common_cancel)) } },
    )
}

@Composable
private fun IngredientLine(ingredient: IngredientInfo) {
    val name = ingredientName(ingredient)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IngredientBadge(ingredient, name)
        Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 10.dp))
    }
}
