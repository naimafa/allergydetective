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
import allergydetective.shared.generated.resources.experiment_gluten_hint
import allergydetective.shared.generated.resources.experiment_diet_title
import allergydetective.shared.generated.resources.experiment_diet_less
import allergydetective.shared.generated.resources.experiment_diet_more
import allergydetective.shared.generated.resources.experiment_diet_hint
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.displayName
import androidx.compose.foundation.layout.Arrangement
import allergydetective.shared.generated.resources.experiment_legend_eaten
import allergydetective.shared.generated.resources.experiment_legend_not_counted
import allergydetective.shared.generated.resources.experiment_mean_line
import allergydetective.shared.generated.resources.experiment_result_from_day
import allergydetective.shared.generated.resources.experiment_result_at_end
import allergydetective.shared.generated.resources.experiment_before_too_few
import allergydetective.shared.generated.resources.experiment_step1_hint
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import allergydetective.shared.generated.resources.experiment_how
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import allergydetective.shared.generated.resources.home_progress_badge
import androidx.compose.material3.Surface
import allergydetective.shared.generated.resources.reintro_running
import allergydetective.shared.generated.resources.reintro_col
import allergydetective.shared.generated.resources.reintro_hint
import allergydetective.shared.generated.resources.reintro_title_not_eaten
import allergydetective.shared.generated.resources.reintro_not_eaten
import allergydetective.shared.generated.resources.reintro_title_back
import allergydetective.shared.generated.resources.reintro_back
import allergydetective.shared.generated.resources.reintro_title_not_back
import allergydetective.shared.generated.resources.reintro_not_back
import allergydetective.shared.generated.resources.reintro_start
import allergydetective.shared.generated.resources.reintro_pill_ok
import allergydetective.shared.generated.resources.reintro_pill_doctor
import allergydetective.shared.generated.resources.experiment_title_too_few
import allergydetective.shared.generated.resources.experiment_title_less
import allergydetective.shared.generated.resources.experiment_title_similar
import allergydetective.shared.generated.resources.experiment_title_more
import allergydetective.shared.generated.resources.experiment_box_complaints
import allergydetective.shared.generated.resources.experiment_mean_of_five
import allergydetective.shared.generated.resources.experiment_next_step
import allergydetective.shared.generated.resources.experiment_step_leave_out
import allergydetective.shared.generated.resources.experiment_step_compare
import allergydetective.shared.generated.resources.experiment_step_reintro
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingFlat
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.nfar.allergydetective.domain.experiment.MIN_REINTRO_EATEN
import com.nfar.allergydetective.domain.experiment.PeriodStats
import com.nfar.allergydetective.domain.experiment.ReintroVerdict
import com.nfar.allergydetective.ui.theme.AppColors
import com.nfar.allergydetective.ui.theme.AppFonts
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
    val home = AppColors.extra.home
    // User 26.11.2026: in the style of the sketch - cream card, Playfair title, before in coral, without in green.
    Card(
        colors = CardDefaults.cardColors(containerColor = home.card, contentColor = home.ink),
        border = BorderStroke(1.dp, home.cardBorder),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(Res.string.experiment_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = AppFonts.playfair, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
                color = home.ink,
            )
            val experiment = state.experiment
            val result = state.result
            val name = state.group?.let { displayName(NamePrefix.PROPERTY, it.key) } ?: state.ingredient?.let { ingredientName(it) }
            if (experiment == null || result == null || name == null) {
                Text(stringResource(Res.string.experiment_intro), style = MaterialTheme.typography.bodyMedium)
                Button(onClick = viewModel::onOpenSetup, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
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
    val home = AppColors.extra.home
    val muted = home.inkSoft
    val reintro = result.reintro
    // Phase line with progress: the reintroduction once it runs, else the test.
    when {
        reintro != null -> PhaseLine("${stringResource(Res.string.reintro_col)} $name", reintro.dayNumber, Experiment.REINTRO_DAYS, !reintro.over)
        result.over -> PhaseLine(stringResource(Res.string.experiment_over, name), 0, 0, false)
        else -> PhaseLine(stringResource(Res.string.experiment_focus, name), result.dayNumber, experiment.days, true)
    }
    // User 26.11.2026: waiting is a status of its own, right under the progress, and says when the result comes.
    if (reintro == null && result.verdict == ExperimentVerdict.TOO_FEW_DAYS) WaitingStatus(experiment, result)
    // Two boxes and the bars: before | without (and, in the third phase, without | with again).
    if (reintro == null) {
        CompareBoxes(stringResource(Res.string.experiment_col_before), result.before, home.heart, stringResource(Res.string.experiment_col_without), result.during, home.accent)
    } else {
        CompareBoxes(stringResource(Res.string.experiment_col_without), result.during, home.accent, stringResource(Res.string.reintro_col), reintro.stats, home.heart)
    }
    if (!result.over) {
        Text(stringResource(Res.string.experiment_not_yet, WASHOUT_DAYS), style = MaterialTheme.typography.bodySmall, color = muted)
    }
    if (result.daily.isNotEmpty() && reintro == null) {
        Text(stringResource(Res.string.experiment_course), style = MaterialTheme.typography.titleSmall)
        CourseChart(result, experiment.days)
        ChartLegend()
    }
    // User 26.11.2026: something else eaten much more or less often may be the reason, not the left-out food.
    if (result.dietChanges.isNotEmpty() && reintro == null) {
        Text(stringResource(Res.string.experiment_diet_title), style = MaterialTheme.typography.titleSmall)
        result.dietChanges.forEach { change ->
            val group = ExperimentViewModel.GROUPS.firstOrNull { it.propertyId == change.propertyId } ?: return@forEach
            Text(
                stringResource(
                    if (change.duringShare < change.beforeShare) Res.string.experiment_diet_less else Res.string.experiment_diet_more,
                    displayName(NamePrefix.PROPERTY, group.key),
                    change.beforeDays, change.beforeTotal, change.duringDays, change.duringTotal,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(stringResource(Res.string.experiment_diet_hint), style = MaterialTheme.typography.bodySmall, color = muted)
    }
    if (result.eatenDays > 0 && reintro == null) {
        Text(stringResource(Res.string.experiment_eaten, name, result.eatenDays), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
    if (reintro == null) {
        when (result.verdict) {
            ExperimentVerdict.TOO_FEW_DAYS -> Unit
            ExperimentVerdict.LESS -> VerdictBox(Icons.AutoMirrored.Outlined.TrendingDown, stringResource(Res.string.experiment_title_less), stringResource(Res.string.experiment_less, name))
            ExperimentVerdict.SIMILAR -> VerdictBox(Icons.AutoMirrored.Outlined.TrendingFlat, stringResource(Res.string.experiment_title_similar), stringResource(Res.string.experiment_similar, name))
            ExperimentVerdict.MORE -> VerdictBox(Icons.AutoMirrored.Outlined.TrendingUp, stringResource(Res.string.experiment_title_more), stringResource(Res.string.experiment_more, name))
        }
    } else {
        when (reintro.verdict) {
            ReintroVerdict.TOO_FEW_DAYS -> VerdictBox(Icons.Outlined.HourglassEmpty, stringResource(Res.string.experiment_title_too_few), stringResource(Res.string.reintro_hint, name))
            ReintroVerdict.NOT_EATEN -> VerdictBox(Icons.Outlined.Restaurant, stringResource(Res.string.reintro_title_not_eaten), stringResource(Res.string.reintro_not_eaten, name, MIN_REINTRO_EATEN))
            ReintroVerdict.BACK -> VerdictBox(Icons.AutoMirrored.Outlined.TrendingUp, stringResource(Res.string.reintro_title_back), stringResource(Res.string.reintro_back, name))
            ReintroVerdict.NOT_BACK -> VerdictBox(Icons.AutoMirrored.Outlined.TrendingFlat, stringResource(Res.string.reintro_title_not_back), stringResource(Res.string.reintro_not_back, name))
        }
    }
    NextSteps(result, name)
    RulesToggle()
    val canReintro = result.over && reintro == null && result.verdict == ExperimentVerdict.LESS && result.reintroAllowed
    if (canReintro) {
        Button(onClick = viewModel::onStartReintro, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text(stringResource(Res.string.reintro_start, name))
        }
    }
    when {
        reintro != null && !reintro.over -> OutlinedButton(onClick = viewModel::onNew, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(stringResource(Res.string.experiment_stop))
        }
        result.over -> {
            val modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            if (canReintro) {
                OutlinedButton(onClick = viewModel::onNew, shape = CircleShape, modifier = modifier) { Text(stringResource(Res.string.experiment_new)) }
            } else {
                Button(onClick = viewModel::onNew, shape = CircleShape, modifier = modifier) { Text(stringResource(Res.string.experiment_new)) }
            }
        }
        else -> OutlinedButton(onClick = viewModel::onStop, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(stringResource(Res.string.experiment_stop))
        }
    }
}

@Composable
private fun PhaseLine(text: String, day: Int, total: Int, running: Boolean) {
    val home = AppColors.extra.home
    // Like "Dein Fortschritt" on the start screen: the day as a pill on the right, so the line does not wrap.
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = home.ink, modifier = Modifier.weight(1f))
        if (running) {
            Surface(shape = CircleShape, color = home.badge, contentColor = home.accent) {
                Text(
                    stringResource(Res.string.home_progress_badge, day, total),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
    if (running) {
        LinearProgressIndicator(
            progress = { day.toFloat() / total },
            color = home.accent,
            trackColor = home.progressTrack,
            strokeCap = StrokeCap.Round,
            modifier = Modifier.fillMaxWidth().height(8.dp),
        )
    }
}

/** Sketch 26.11.2026: two boxes with a coloured header, then one bar each for the mean strength (0-5). */
@Composable
private fun CompareBoxes(leftLabel: String, left: PeriodStats, leftColor: Color, rightLabel: String, right: PeriodStats, rightColor: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        StatBox(leftLabel, left, leftColor, Modifier.weight(1f).fillMaxHeight())
        StatBox(rightLabel, right, rightColor, Modifier.weight(1f).fillMaxHeight())
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MeanBar(leftLabel, left, leftColor)
        MeanBar(rightLabel, right, rightColor)
    }
}

@Composable
private fun StatBox(label: String, stats: PeriodStats, color: Color, modifier: Modifier) {
    val home = AppColors.extra.home
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = home.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().background(color.copy(alpha = 0.28f)).padding(horizontal = 8.dp, vertical = 6.dp),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
            Text(
                stringResource(Res.string.experiment_days_n, stats.days),
                // Inter, not Playfair: its old-style figures made the 0 look like an o.
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
                color = home.ink,
            )
            Text(
                stringResource(Res.string.experiment_box_complaints, stats.complaintDays),
                style = MaterialTheme.typography.bodySmall,
                color = home.inkSoft,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MeanBar(label: String, stats: PeriodStats, color: Color) {
    val home = AppColors.extra.home
    val value = if (stats.days == 0) null else stats.meanStrength
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = home.inkSoft, modifier = Modifier.weight(1f))
            Text(if (stats.days == 0) "–" else stringResource(Res.string.experiment_mean_of_five, strength(stats.meanStrength, stats.days)), style = MaterialTheme.typography.labelLarge, color = home.ink)
        }
        Box(modifier = Modifier.fillMaxWidth().height(14.dp).clip(CircleShape).background(home.progressTrack)) {
            if (value != null && value > 0) {
                Box(Modifier.fillMaxWidth((value / 5.0).toFloat().coerceIn(0.03f, 1f)).fillMaxHeight().clip(CircleShape).background(color))
            }
        }
    }
}

/** Sage box with an icon in a circle, like "Erste Verbesserung" in the sketch. */
@Composable
private fun VerdictBox(icon: ImageVector, title: String, text: String) {
    val home = AppColors.extra.home
    // Phone test 26.11.2026: the icon next to the title, so the text gets the full width.
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(home.todayCard).padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp).clip(CircleShape).background(home.accent.copy(alpha = 0.15f))) {
                Icon(icon, contentDescription = null, tint = home.accent, modifier = Modifier.size(22.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = AppFonts.playfair, fontWeight = FontWeight.SemiBold),
                color = home.ink,
            )
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = home.ink)
    }
}

/**
 * "Nächster Schritt" from the sketch: leave out, compare, eat again. The third step says whether the app may guide it
 * (user 26.11.2026: never for allergens or known allergies).
 */
@Composable
private fun NextSteps(result: ExperimentResult, name: String) {
    val home = AppColors.extra.home
    val reintro = result.reintro
    val current = when {
        reintro != null -> 3
        result.over -> 2
        else -> 1
    }
    Text(
        stringResource(Res.string.experiment_next_step),
        style = MaterialTheme.typography.titleLarge.copy(fontFamily = AppFonts.playfair, fontWeight = FontWeight.SemiBold),
        color = home.ink,
    )
    val steps = listOf(
        stringResource(Res.string.experiment_step_leave_out, name),
        stringResource(Res.string.experiment_step_compare),
        stringResource(Res.string.experiment_step_reintro, name),
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        steps.forEachIndexed { i, step ->
            val number = i + 1
            val done = number < current || (number == 3 && reintro?.over == true)
            val active = number == current && !done
            // User 26.11.2026: the current step stands out (sage row, bold, what to do now); the next ones stay faint.
            val rowModifier = if (active) {
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(home.todayCard).padding(horizontal = 10.dp, vertical = 10.dp)
            } else {
                Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = rowModifier) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(if (active) 32.dp else 26.dp).clip(CircleShape)
                        .background(if (done || active) home.accent else home.progressTrack),
                ) {
                    if (done) {
                        Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("$number", style = MaterialTheme.typography.labelLarge, color = if (active) Color.White else home.inkSoft)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        step,
                        style = if (active) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
                        color = when {
                            active -> home.ink
                            done -> home.inkSoft
                            else -> home.inkSoft.copy(alpha = 0.7f)
                        },
                    )
                    val hint = when {
                        !active -> null
                        number == 1 -> stringResource(Res.string.experiment_step1_hint)
                        number == 3 && reintro != null -> stringResource(Res.string.home_progress_badge, reintro.dayNumber, Experiment.REINTRO_DAYS)
                        else -> null
                    }
                    hint?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = home.accent) }
                }
            }
        }
    }
    val pill = if (result.reintroAllowed) stringResource(Res.string.reintro_pill_ok) else stringResource(Res.string.reintro_pill_doctor, name)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(home.heart.copy(alpha = 0.14f)).padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = home.heart, modifier = Modifier.size(22.dp))
        Text(pill, style = MaterialTheme.typography.bodySmall, color = home.ink)
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
                // Leaving out gluten before a coeliac test can make the test miss it (user 26.11.2026).
                if (setup.chosenGroup?.key == "gluten") {
                    Text(stringResource(Res.string.experiment_gluten_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
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
 * dashed line is the mean before. Real values, no smoothed curve. Days to come stay empty. User 26.11.2026: the days
 * before are drawn too, left of the start line, so the difference shows at a glance.
 */
@Composable
private fun CourseChart(result: ExperimentResult, plannedDays: Int) {
    val colors = MaterialTheme.colorScheme
    val counted = AppColors.extra.home.accent
    val faint = colors.onSurfaceVariant.copy(alpha = 0.35f)
    val eaten = colors.error
    val grid = colors.outlineVariant
    val beforeMean = if (result.before.days == 0) null else result.before.meanStrength
    val textMeasurer = rememberTextMeasurer()
    val meanLabel = stringResource(Res.string.experiment_mean_line)
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = colors.onSurface)
    val cardColor = AppColors.extra.home.card
    val before = AppColors.extra.home.heart
    val beforeCount = result.beforeDaily.size
    val byNumber = (result.beforeDaily + result.daily).associateBy { it.number }
    Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
        val maxStrength = 5f
        val slot = size.width / (beforeCount + plannedDays)
        val barWidth = slot * 0.6f
        val dot = 3.dp.toPx()
        drawLine(grid, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
        for (n in -beforeCount..plannedDays) {
            if (n == 0) continue
            val day = byNumber[n] ?: continue
            val index = if (n < 0) n + beforeCount else n - 1 + beforeCount
            val x = slot * index + (slot - barWidth) / 2
            val strength = day.strength
            if (strength == null) {
                drawCircle(faint, radius = dot, center = Offset(x + barWidth / 2, size.height - dot * 2))
                continue
            }
            val color = when (day.status) {
                TestDayStatus.COUNTED -> if (n < 0) before else counted
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
        if (beforeCount > 0) {
            val startX = slot * beforeCount
            drawLine(colors.onSurfaceVariant, Offset(startX, 0f), Offset(startX, size.height), strokeWidth = 1.5.dp.toPx())
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
            // User 26.11.2026: say on the line what it is.
            val label = textMeasurer.measure(meanLabel, labelStyle)
            val labelY = if (y - label.size.height - 2.dp.toPx() >= 0f) y - label.size.height - 2.dp.toPx() else y + 2.dp.toPx()
            drawRect(cardColor, Offset(size.width - label.size.width - 6.dp.toPx(), labelY), Size(label.size.width + 6.dp.toPx(), label.size.height.toFloat()))
            drawText(label, topLeft = Offset(size.width - label.size.width - 3.dp.toPx(), labelY))
        }
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        if (beforeCount > 0) {
            Text(stringResource(Res.string.experiment_col_before), style = MaterialTheme.typography.labelSmall, color = before, modifier = Modifier.weight(beforeCount.toFloat()))
        }
        Text(stringResource(Res.string.experiment_day_n, 1), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, modifier = Modifier.weight(plannedDays.toFloat()))
        Text(stringResource(Res.string.experiment_day_n, plannedDays), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
    }
}

/** Mean strength with one decimal, "–" without counted days. */
private fun strength(mean: Double, days: Int): String =
    if (days == 0) "–" else ((mean * 10).toInt() / 10.0).toString().replace('.', ',')

/** The rules folded away (phone test 26.11.2026: the paragraph was long); open on a tap. */
@Composable
private fun RulesToggle() {
    val home = AppColors.extra.home
    var open by remember { mutableStateOf(false) }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { open = !open },
        ) {
            Text(stringResource(Res.string.experiment_how), style = MaterialTheme.typography.titleSmall, color = home.accent, modifier = Modifier.weight(1f))
            Icon(if (open) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = home.accent)
        }
        if (open) Text(stringResource(Res.string.experiment_rules), style = MaterialTheme.typography.bodySmall, color = home.inkSoft)
    }
}

/**
 * The day of the test from which the comparison has enough days, or null when the test is too short for it or the
 * days before are too few (they cannot grow any more).
 */
private fun resultDay(experiment: Experiment, result: ExperimentResult): Int? {
    if (result.before.days < MIN_COUNTED_DAYS) return null
    val missing = MIN_COUNTED_DAYS - result.during.days
    // Today is counted once closed; the first days never count.
    val day = maxOf(result.dayNumber, WASHOUT_DAYS + 1) + missing
    return day.takeIf { it <= experiment.days + 1 }
}

@Composable
private fun WaitingStatus(experiment: Experiment, result: ExperimentResult) {
    val home = AppColors.extra.home
    val day = if (result.over) null else resultDay(experiment, result)
    val headline = when {
        result.before.days < MIN_COUNTED_DAYS -> stringResource(Res.string.experiment_before_too_few, result.before.days)
        day != null && day > experiment.days -> stringResource(Res.string.experiment_result_at_end)
        day != null -> stringResource(Res.string.experiment_result_from_day, day)
        else -> stringResource(Res.string.experiment_title_too_few)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(home.heart.copy(alpha = 0.16f))
            .border(1.dp, home.heart.copy(alpha = 0.35f), RoundedCornerShape(16.dp)).padding(12.dp),
    ) {
        Icon(Icons.Outlined.HourglassEmpty, contentDescription = null, tint = home.heart, modifier = Modifier.size(26.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(headline, style = MaterialTheme.typography.titleMedium, color = home.ink)
            Text(stringResource(Res.string.experiment_too_few), style = MaterialTheme.typography.bodySmall, color = home.inkSoft)
        }
    }
}

/** User 26.11.2026: the legend as colour swatches, not as a sentence. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChartLegend() {
    val home = AppColors.extra.home
    val faint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LegendItem(home.heart, stringResource(Res.string.experiment_col_before))
        LegendItem(home.accent, stringResource(Res.string.experiment_col_without))
        LegendItem(MaterialTheme.colorScheme.error, stringResource(Res.string.experiment_legend_eaten))
        LegendItem(faint, stringResource(Res.string.experiment_legend_not_counted))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall, color = AppColors.extra.home.inkSoft)
    }
}
