package com.nfar.allergydetective.ui.daycheck

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.day_autosave_hint
import allergydetective.shared.generated.resources.day_confirmed_mark
import allergydetective.shared.generated.resources.day_factors
import allergydetective.shared.generated.resources.day_finish
import allergydetective.shared.generated.resources.day_hours
import allergydetective.shared.generated.resources.day_no_symptoms_log
import allergydetective.shared.generated.resources.day_no_symptoms_question
import allergydetective.shared.generated.resources.day_no_symptoms_yes
import allergydetective.shared.generated.resources.day_open_past_hint
import allergydetective.shared.generated.resources.day_scale_hint
import allergydetective.shared.generated.resources.day_sick_hint
import allergydetective.shared.generated.resources.day_sleep
import allergydetective.shared.generated.resources.day_sleep_quality
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
import androidx.compose.material3.FilterChip
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
import com.nfar.allergydetective.ui.common.dayLabel
import com.nfar.allergydetective.ui.common.displayName
import com.nfar.allergydetective.ui.common.formatDate
import com.nfar.allergydetective.ui.common.formatDecimal
import com.nfar.allergydetective.ui.sick.SickPeriodDayCheckPart
import com.nfar.allergydetective.ui.sick.SickPeriodsViewModel
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
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
    viewModel: DayCheckViewModel = koinViewModel(),
    sickViewModel: SickPeriodsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onDone() }
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
            Column(modifier = Modifier.navigationBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(Res.string.day_autosave_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = viewModel::onFinish,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                ) { Text(stringResource(Res.string.day_finish)) }
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
    val check = state.check
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.days.forEach { day ->
                    val label = dayLabel((state.today - day).toInt())
                    FilterChip(
                        selected = check.epochDay == day,
                        onClick = { viewModel.onDaySelected(day) },
                        label = { Text(if (day in state.confirmedDays) stringResource(Res.string.day_confirmed_mark, label) else label) },
                    )
                }
            }
            // The date in full, so it is clear which day gets confirmed.
            Text(formatDate(LocalDate.fromEpochDays(check.epochDay), Locale.current.language), style = MaterialTheme.typography.titleLarge)
            val openPast = state.openPastDays.filter { it != check.epochDay }
            if (openPast.isNotEmpty()) {
                val labels = openPast.map { dayLabel((state.today - it).toInt()) }.joinToString(", ")
                Text(stringResource(Res.string.day_open_past_hint, labels), style = MaterialTheme.typography.bodySmall)
            }
        }

        StepCard(title = stringResource(Res.string.day_wellbeing)) {
            ScaleRow(check.wellbeing, viewModel::onWellbeingChange, labels = WELLBEING_FACES)
        }

        StepCard(title = stringResource(Res.string.day_sleep)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.onSleepHoursChange(-DayCheckViewModel.SLEEP_STEP_HOURS) }) { Text("−") }
                Text(
                    stringResource(Res.string.day_hours, formatDecimal(check.sleepHours ?: DayCheckViewModel.DEFAULT_SLEEP_HOURS, Locale.current.language)),
                    style = MaterialTheme.typography.titleMedium,
                )
                OutlinedButton(onClick = { viewModel.onSleepHoursChange(DayCheckViewModel.SLEEP_STEP_HOURS) }) { Text("+") }
            }
            Text(stringResource(Res.string.day_sleep_quality), style = MaterialTheme.typography.bodyMedium)
            ScaleRow(check.sleepQuality, viewModel::onSleepQualityChange, labels = (1..5).map { "★".repeat(it) })
        }

        StepCard(title = stringResource(Res.string.day_stress)) {
            ScaleRow(check.stress, viewModel::onStressChange)
            Text(stringResource(Res.string.day_scale_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        StepCard(title = stringResource(Res.string.day_factors)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.factors.forEach { factor ->
                    FilterChip(
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
private fun ScaleRow(value: Int?, onChange: (Int) -> Unit, labels: List<String> = (1..5).map { it.toString() }) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..5).forEach { option ->
            FilterChip(selected = value == option, onClick = { onChange(option) }, label = { Text(labels[option - 1]) })
        }
    }
}
