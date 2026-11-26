package com.nfar.allergydetective.ui.results

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.ingredient_search
import allergydetective.shared.generated.resources.experiment_title
import allergydetective.shared.generated.resources.experiment_col_before
import allergydetective.shared.generated.resources.experiment_col_without
import allergydetective.shared.generated.resources.experiment_row_days
import allergydetective.shared.generated.resources.experiment_row_complaints
import allergydetective.shared.generated.resources.experiment_row_strength
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
import allergydetective.shared.generated.resources.experiment_rules
import allergydetective.shared.generated.resources.experiment_eaten
import allergydetective.shared.generated.resources.experiment_too_few
import allergydetective.shared.generated.resources.experiment_less
import allergydetective.shared.generated.resources.experiment_similar
import allergydetective.shared.generated.resources.experiment_more
import allergydetective.shared.generated.resources.experiment_stop
import allergydetective.shared.generated.resources.experiment_new
import allergydetective.shared.generated.resources.experiment_groups
import allergydetective.shared.generated.resources.experiment_group_days
import allergydetective.shared.generated.resources.experiment_or_single
import allergydetective.shared.generated.resources.experiment_course
import allergydetective.shared.generated.resources.experiment_course_legend
import allergydetective.shared.generated.resources.experiment_day_n
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedCard
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.sp
import com.nfar.allergydetective.domain.experiment.TestDayStatus
import com.nfar.allergydetective.domain.experiment.WASHOUT_DAYS
import allergydetective.shared.generated.resources.experiment_not_yet
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.displayName
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
import com.nfar.allergydetective.ui.meal.ingredientExamples
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
            val name = state.group?.let { displayName(NamePrefix.PROPERTY, it.key) } ?: state.ingredient?.let { ingredientName(it) }
            if (experiment == null || result == null || name == null) {
                Text(stringResource(Res.string.experiment_intro), style = MaterialTheme.typography.bodyMedium)
                Button(onClick = viewModel::onOpenSetup, shape = CircleShape, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(Res.string.experiment_start))
                }
            } else {
                RunningOrDone(experiment, result, name, viewModel)
            }
        }
    }
    state.setup?.let { SetupDialog(it, state.groups, state.suggestions, state.today, viewModel) }
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
    // User 25.11.2026: "strukturierter" - a small table before | without, with the mean strength per day.
    val before = result.before
    val during = result.during
    val rows = listOf(
        Triple(stringResource(Res.string.experiment_row_days), "${before.days}", "${during.days}"),
        Triple(stringResource(Res.string.experiment_row_complaints), "${before.complaintDays}", "${during.complaintDays}"),
        Triple(stringResource(Res.string.experiment_row_strength), strength(before.meanStrength, before.days), strength(during.meanStrength, during.days)),
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row {
            Text("", modifier = Modifier.weight(1.4f))
            Text(stringResource(Res.string.experiment_col_before), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Text(stringResource(Res.string.experiment_col_without), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        }
        rows.forEach { (label, b, d) ->
            Row {
                Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1.4f))
                Text(b, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(d, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            }
        }
    }
    // User 26.11.2026: "Ohne 0" looked wrong while the first days had complaints - say which days do not count yet.
    if (!result.over) {
        Text(stringResource(Res.string.experiment_not_yet, WASHOUT_DAYS), style = MaterialTheme.typography.bodySmall, color = muted)
    }
    if (result.daily.isNotEmpty()) {
        Text(stringResource(Res.string.experiment_course), style = MaterialTheme.typography.titleSmall)
        CourseChart(result, experiment.days)
        Text(stringResource(Res.string.experiment_course_legend), style = MaterialTheme.typography.bodySmall, color = muted)
    }
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
private fun SetupDialog(setup: ExperimentSetup, groups: List<TestGroup>, suggestions: List<IngredientInfo>, today: Long, viewModel: ExperimentViewModel) {
    val chosen = setup.chosen
    val chosenName = setup.chosenGroup?.let { displayName(NamePrefix.PROPERTY, it.key) } ?: chosen?.let { ingredientName(it) }
    val focus = chosenName?.let { stringResource(Res.string.experiment_focus, it) }
    AlertDialog(
        onDismissRequest = viewModel::onCancelSetup,
        title = { Text(stringResource(Res.string.experiment_start)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(Res.string.experiment_what), style = MaterialTheme.typography.titleSmall)
                // User 26.11.2026: a whole group as tiles, as leaving out milk while eating cheese says nothing.
                Text(stringResource(Res.string.experiment_groups), style = MaterialTheme.typography.bodySmall)
                groups.take(MAX_GROUP_TILES).chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { group ->
                            GroupTile(group, selected = setup.chosenGroup == group, onClick = { viewModel.onChooseGroup(group) }, modifier = Modifier.weight(1f))
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                Text(stringResource(Res.string.experiment_or_single), style = MaterialTheme.typography.bodySmall)
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
            Button(onClick = { focus?.let(viewModel::onBegin) }, enabled = chosenName != null, shape = CircleShape) { Text(stringResource(Res.string.experiment_begin)) }
        },
        dismissButton = { TextButton(onClick = viewModel::onCancelSetup) { Text(stringResource(Res.string.common_cancel)) } },
    )
}

@Composable
private fun IngredientLine(ingredient: IngredientInfo) {
    val name = ingredientName(ingredient)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IngredientBadge(ingredient, name)
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(name, style = MaterialTheme.typography.bodyLarge)
            ingredientExamples(ingredient)?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private const val MAX_GROUP_TILES = 6

@Composable
private fun GroupTile(group: TestGroup, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    OutlinedCard(
        onClick = onClick,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant),
        colors = CardDefaults.outlinedCardColors(containerColor = if (selected) colors.primaryContainer else colors.surface),
        modifier = modifier.heightIn(min = 48.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(group.emoji, fontSize = 26.sp)
            Text(displayName(NamePrefix.PROPERTY, group.key), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(Res.string.experiment_group_days, group.daysEaten, ExperimentViewModel.SUGGESTION_DAYS),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

/**
 * User 26.11.2026 (sketch "Symptomverlauf"): one bar per day of the test with the strongest complaint (0-5), the
 * dashed line is the mean before. Real values, no smoothed curve. Days to come stay empty.
 */
@Composable
private fun CourseChart(result: ExperimentResult, plannedDays: Int) {
    val colors = MaterialTheme.colorScheme
    val counted = colors.primary
    val faint = colors.onSurfaceVariant.copy(alpha = 0.35f)
    val eaten = colors.error
    val grid = colors.outlineVariant
    val beforeMean = if (result.before.days == 0) null else result.before.meanStrength
    val byNumber = result.daily.associateBy { it.number }
    Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
        val maxStrength = 5f
        val slot = size.width / plannedDays
        val barWidth = slot * 0.6f
        val dot = 3.dp.toPx()
        drawLine(grid, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
        for (n in 1..plannedDays) {
            val day = byNumber[n] ?: continue
            val x = slot * (n - 1) + (slot - barWidth) / 2
            val strength = day.strength
            if (strength == null) {
                drawCircle(faint, radius = dot, center = Offset(x + barWidth / 2, size.height - dot * 2))
                continue
            }
            val color = when (day.status) {
                TestDayStatus.COUNTED -> counted
                TestDayStatus.EATEN -> eaten
                else -> faint
            }
            val h = (strength / maxStrength) * (size.height - dot * 2)
            if (h <= 0f) {
                drawRect(color, Offset(x, size.height - dot), Size(barWidth, dot))
            } else {
                drawRect(color, Offset(x, size.height - h), Size(barWidth, h))
            }
        }
        beforeMean?.let { mean ->
            val y = size.height - (mean.toFloat() / maxStrength) * (size.height - dot * 2)
            drawLine(
                colors.onSurface,
                Offset(0f, y),
                Offset(size.width, y),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
            )
        }
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(Res.string.experiment_day_n, 1), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(stringResource(Res.string.experiment_day_n, plannedDays), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
    }
}

/** Mean strength with one decimal, "–" without counted days. */
private fun strength(mean: Double, days: Int): String =
    if (days == 0) "–" else ((mean * 10).toInt() / 10.0).toString().replace('.', ',')
