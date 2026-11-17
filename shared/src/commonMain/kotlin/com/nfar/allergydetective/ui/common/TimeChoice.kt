package com.nfar.allergydetective.ui.common

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.day_today
import allergydetective.shared.generated.resources.day_two_days_ago
import allergydetective.shared.generated.resources.day_yesterday
import allergydetective.shared.generated.resources.time_other
import allergydetective.shared.generated.resources.time_pick
import allergydetective.shared.generated.resources.time_selected
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.domain.time.AppClock
import org.jetbrains.compose.resources.stringResource

/** A time picked in the dialog: local day relative to today plus wall-clock time. */
data class ExactTime(val daysAgo: Int, val hour: Int, val minute: Int)

/** Days that can be picked: today and up to two days back, like the day check. */
const val MAX_DAYS_BACK = 2

/** Timestamp of the chosen time: the picked time (never in the future) or "now minus [minutesAgo]". */
fun AppClock.timeOf(exact: ExactTime?, minutesAgo: Int): Long {
    val now = nowMillis()
    return exact?.let { millisAt(today() - it.daysAgo, it.hour, it.minute).coerceAtMost(now) }
        ?: (now - minutesAgo * 60_000L)
}

/**
 * Row of quick time chips plus "Other time …" (TRD 3.2: log afterwards).
 * [quickLabel] names a chip, [exact] is the picked time when it is active.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> TimeChoiceRow(
    options: List<T>,
    selected: T?,
    quickLabel: @Composable (T) -> String,
    exact: ExactTime?,
    onQuickSelected: (T) -> Unit,
    onExactSelected: (ExactTime) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = exact == null && selected == option,
                onClick = { onQuickSelected(option) },
                label = { Text(quickLabel(option)) },
            )
        }
        FilterChip(
            selected = exact != null,
            onClick = { showDialog = true },
            label = {
                Text(
                    exact?.let { stringResource(Res.string.time_selected, dayLabel(it.daysAgo), formatClock(it.hour, it.minute)) }
                        ?: stringResource(Res.string.time_other),
                )
            },
        )
    }
    if (showDialog) {
        ExactTimeDialog(
            initial = exact,
            onConfirm = {
                showDialog = false
                onExactSelected(it)
            },
            onDismiss = { showDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ExactTimeDialog(initial: ExactTime?, onConfirm: (ExactTime) -> Unit, onDismiss: () -> Unit) {
    var daysAgo by remember { mutableIntStateOf(initial?.daysAgo ?: 0) }
    val timeState = rememberTimePickerState(initialHour = initial?.hour ?: 12, initialMinute = initial?.minute ?: 0, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.time_pick)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0..MAX_DAYS_BACK).forEach { option ->
                        FilterChip(selected = daysAgo == option, onClick = { daysAgo = option }, label = { Text(dayLabel(option)) })
                    }
                }
                TimeInput(state = timeState)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(ExactTime(daysAgo, timeState.hour, timeState.minute)) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } },
    )
}

@Composable
fun dayLabel(daysAgo: Int): String = stringResource(
    when (daysAgo) {
        0 -> Res.string.day_today
        1 -> Res.string.day_yesterday
        else -> Res.string.day_two_days_ago
    },
)

fun formatClock(hour: Int, minute: Int): String =
    hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')
