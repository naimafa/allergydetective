package com.nfar.allergydetective.ui.daycheck

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.day_autosave_hint
import allergydetective.shared.generated.resources.day_factors
import allergydetective.shared.generated.resources.day_finish
import allergydetective.shared.generated.resources.day_hours
import allergydetective.shared.generated.resources.day_no_symptoms_log
import allergydetective.shared.generated.resources.day_no_symptoms_question
import allergydetective.shared.generated.resources.day_no_symptoms_yes
import allergydetective.shared.generated.resources.day_open_past_hint
import allergydetective.shared.generated.resources.day_sick_banner
import allergydetective.shared.generated.resources.day_finished_info
import allergydetective.shared.generated.resources.day_reset
import allergydetective.shared.generated.resources.day_scale_hint
import allergydetective.shared.generated.resources.day_sick_hint
import allergydetective.shared.generated.resources.day_sleep
import allergydetective.shared.generated.resources.day_sleep_quality
import allergydetective.shared.generated.resources.day_sleep_quality_hint
import allergydetective.shared.generated.resources.day_stress
import allergydetective.shared.generated.resources.day_wellbeing
import allergydetective.shared.generated.resources.home_day_check
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import com.nfar.allergydetective.ui.common.AppFilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.ScreenScaffold
import com.nfar.allergydetective.ui.common.StepCard
import com.nfar.allergydetective.ui.common.ScaleChips
import com.nfar.allergydetective.ui.common.DayPicker
import com.nfar.allergydetective.ui.common.dayLabel
import com.nfar.allergydetective.ui.common.displayName
import com.nfar.allergydetective.ui.common.formatDate
import com.nfar.allergydetective.ui.common.formatDecimal
import com.nfar.allergydetective.ui.sick.SickPeriodDayCheckPart
import com.nfar.allergydetective.ui.sick.SickPeriodsViewModel
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.size
import com.nfar.allergydetective.ui.theme.AppColors
import allergydetective.shared.generated.resources.day_sleep_less
import allergydetective.shared.generated.resources.day_sleep_more
import org.koin.compose.viewmodel.koinViewModel

/**
 * Day check (UX 3.4). Self-test 19.11.: each topic in its own card, answers are saved right away,
 * one button "Finish day" at the bottom.
 */
@Composable
fun DayCheckScreen(
    onDone: () -> Unit,
    /** "I had some: log a symptom" from the no-complaints question. */
    onLogSymptom: () -> Unit = {},
    /** Day to open, e.g. from the history; null = today. */
    day: Long? = null,
    /** A sick day was finished: open the symptom form for that day (days before today). */
    onSickDayFinished: (daysAgo: Int) -> Unit = { onDone() },
    viewModel: DayCheckViewModel = koinViewModel(),
    sickViewModel: SickPeriodsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) {
            if (state.openSymptoms) onSickDayFinished((state.today - state.check.epochDay).toInt()) else onDone()
        }
    }
    LaunchedEffect(day) { day?.let(viewModel::onDaySelected) }
    // Entering the screen again, e.g. after logging a symptom.
    LaunchedEffect(Unit) { viewModel.onResume() }

    if (state.askNoSymptoms) {
        AlertDialog(
            onDismissRequest = viewModel::onNoSymptomsDismissed,
            text = { Text(stringResource(Res.string.day_no_symptoms_question)) },
            confirmButton = { Button(onClick = viewModel::onConfirmNoSymptoms) { Text(stringResource(Res.string.day_no_symptoms_yes)) } },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onNoSymptomsDismissed()
                    onLogSymptom()
                }) { Text(stringResource(Res.string.day_no_symptoms_log)) }
            },
        )
    }

    ScreenScaffold(
        title = stringResource(Res.string.home_day_check),
        onBack = onDone,
        snackbarHostState = remember { SnackbarHostState() },
        bottomBar = {
            // User 23.11.2026 ("durcheinander"): the three-line hint above the button covered the cards; it moved to
            // the top of the page. The button looks like "Mahlzeit speichern".
            Button(
                onClick = viewModel::onFinish,
                enabled = !state.loading,
                shape = CircleShape,
                modifier = Modifier.navigationBarsPadding().fillMaxWidth().padding(16.dp).heightIn(min = 56.dp),
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(22.dp))
                Text(stringResource(Res.string.day_finish), modifier = Modifier.padding(start = 10.dp))
            }
        },
    ) { padding ->
        if (!state.loading) DayCheckContent(state, viewModel, sickViewModel, Modifier.padding(padding))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayCheckContent(
    state: DayCheckState,
    viewModel: DayCheckViewModel,
    sickViewModel: SickPeriodsViewModel,
    modifier: Modifier,
) {
    val sickState by sickViewModel.state.collectAsStateWithLifecycle()
    val check = state.check
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Feedback 19.11.: the same day row as in the forms; finished days carry a check mark.
            DayPicker(
                daysAgo = (state.today - check.epochDay).toInt(),
                today = state.today,
                onSelect = { viewModel.onDaySelected(state.today - it) },
                marked = state.confirmedDays.map { (state.today - it).toInt() }.toSet(),
            )
            // The date in full, so it is clear which day gets confirmed (user 23.11.2026: smaller, it stood alone).
            Text(
                formatDate(LocalDate.fromEpochDays(check.epochDay), Locale.current.language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val openPast = state.openPastDays.filter { it != check.epochDay }
            if (openPast.isNotEmpty()) {
                val labels = openPast.map { dayLabel((state.today - it).toInt()) }.joinToString(", ")
                Text(stringResource(Res.string.day_open_past_hint, labels), style = MaterialTheme.typography.bodySmall)
            }
            Text(stringResource(Res.string.day_autosave_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // User 24.11.2026: entered sick days were hard to see here. A clear banner says so, and a finished day can be reopened.
        val sickFactor = state.factors.any { it.key == SICK_FACTOR_KEY && it.id in check.factorIds }
        if (sickState.isSick(check.epochDay) || sickFactor) {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer) {
                Text(stringResource(Res.string.day_sick_banner), style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth().padding(14.dp))
            }
        }
        if (check.isConfirmed) {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(Res.string.day_finished_info), style = MaterialTheme.typography.titleSmall)
                    OutlinedButton(onClick = viewModel::onReset, modifier = Modifier.heightIn(min = 44.dp)) { Text(stringResource(Res.string.day_reset)) }
                }
            }
        }

        // User 23.11.2026: numbered steps like "Essen erfassen", so the order is clear.
        StepCard(number = 1, title = stringResource(Res.string.day_wellbeing)) {
            ScaleRow(check.wellbeing, viewModel::onWellbeingChange, labels = WELLBEING_FACES)
        }

        StepCard(number = 2, title = stringResource(Res.string.day_sleep)) {
            // One outlined pill "−  7 h  +" instead of three loose parts.
            Surface(
                shape = CircleShape,
                color = AppColors.extra.home.page,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.onSleepHoursChange(-DayCheckViewModel.SLEEP_STEP_HOURS) }) {
                        Icon(Icons.Outlined.Remove, contentDescription = stringResource(Res.string.day_sleep_less))
                    }
                    Text(
                        stringResource(Res.string.day_hours, formatDecimal(check.sleepHours ?: DayCheckViewModel.DEFAULT_SLEEP_HOURS, Locale.current.language)),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    IconButton(onClick = { viewModel.onSleepHoursChange(DayCheckViewModel.SLEEP_STEP_HOURS) }) {
                        Icon(Icons.Outlined.Add, contentDescription = stringResource(Res.string.day_sleep_more))
                    }
                }
            }
            Text(stringResource(Res.string.day_sleep_quality), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 4.dp))
            // Phone test 20.11.: "1 ★" did not fit at font scale 1.25 and the star was cut off; the legend below explains the scale.
            ScaleRow(check.sleepQuality, viewModel::onSleepQualityChange)
            // Feedback 20.11.: everyone should read the scale the same way.
            Text(stringResource(Res.string.day_sleep_quality_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        StepCard(number = 3, title = stringResource(Res.string.day_stress)) {
            ScaleRow(check.stress, viewModel::onStressChange)
            Text(stringResource(Res.string.day_scale_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        StepCard(number = 4, title = stringResource(Res.string.day_factors)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.factors.forEach { factor ->
                    AppFilterChip(
                        selected = factor.id in check.factorIds,
                        onClick = { viewModel.onFactorToggle(factor.id) },
                        label = { Text(displayName(NamePrefix.FACTOR, factor.key)) },
                    )
                }
            }
            if (state.factors.any { it.key == SICK_FACTOR_KEY && it.id in check.factorIds }) {
                Text(stringResource(Res.string.day_sick_hint), style = MaterialTheme.typography.bodySmall)
            }
            SickPeriodDayCheckPart(check.epochDay, sickViewModel)
        }
    }
}

/** Lifestyle factor that takes the day out of the analysis (self-test 18.11.). */
private const val SICK_FACTOR_KEY = "sick"

/** Faces for wellbeing 1–5 (UX 3.4). */
private val WELLBEING_FACES = listOf("😣", "🙁", "😐", "🙂", "😊")

/** Five tap points, 1 = low, 5 = high (UX 6). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScaleRow(value: Int?, onChange: (Int) -> Unit, labels: List<String> = (1..5).map { it.toString() }) =
    ScaleChips(value, onChange, labels)
