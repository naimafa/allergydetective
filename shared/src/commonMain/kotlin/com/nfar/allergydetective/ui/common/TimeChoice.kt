package com.nfar.allergydetective.ui.common

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.day_days_ago
import allergydetective.shared.generated.resources.day_today
import allergydetective.shared.generated.resources.day_two_days_ago
import allergydetective.shared.generated.resources.day_yesterday
import allergydetective.shared.generated.resources.time_change
import allergydetective.shared.generated.resources.time_other
import allergydetective.shared.generated.resources.time_pick
import allergydetective.shared.generated.resources.time_selected
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import com.nfar.allergydetective.ui.common.AppFilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.domain.time.AppClock
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** A time picked in the dialog: local day relative to today plus wall-clock time. */
data class ExactTime(val daysAgo: Int, val hour: Int, val minute: Int)

/** Days that can be picked in every form and the day check: today and up to five days back (feedback 19.11.). */
const val MAX_DAYS_BACK = 5

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
            AppFilterChip(
                selected = exact == null && selected == option,
                onClick = { onQuickSelected(option) },
                label = { Text(quickLabel(option)) },
            )
        }
        AppFilterChip(
            selected = exact != null,
            onClick = { showDialog = true },
            label = {
                Text(
                    exact?.let { formatClock(it.hour, it.minute) }
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

/**
 * "Today, 19:00 · Change" below the meal chips (self-test 19.11.): every meal shows the time that is
 * stored and can be changed, the same way for all of them.
 */
@Composable
fun TimeLine(time: ExactTime, onChange: (ExactTime) -> Unit, modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 12.dp),
        ) {
            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Text(
                formatClock(time.hour, time.minute),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { showDialog = true }) { Text(stringResource(Res.string.time_change)) }
        }
    }
    if (showDialog) {
        ExactTimeDialog(
            initial = time,
            onConfirm = {
                showDialog = false
                onChange(it)
            },
            onDismiss = { showDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ExactTimeDialog(initial: ExactTime?, onConfirm: (ExactTime) -> Unit, onDismiss: () -> Unit) {
    // The day comes from the day row of the form (DayPicker); the dialog only asks for the time.
    val daysAgo = initial?.daysAgo ?: 0
    val timeState = rememberTimePickerState(initialHour = initial?.hour ?: 12, initialMinute = initial?.minute ?: 0, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.time_pick)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
fun dayLabel(daysAgo: Int): String = when (daysAgo) {
    0 -> stringResource(Res.string.day_today)
    1 -> stringResource(Res.string.day_yesterday)
    2 -> stringResource(Res.string.day_two_days_ago)
    else -> pluralStringResource(Res.plurals.day_days_ago, daysAgo, daysAgo)
}

fun formatClock(hour: Int, minute: Int): String =
    hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')
