package com.nfar.allergydetective.ui.sick

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.common_save
import allergydetective.shared.generated.resources.history_delete
import allergydetective.shared.generated.resources.sick_add
import allergydetective.shared.generated.resources.sick_banner
import allergydetective.shared.generated.resources.sick_day_in_period
import allergydetective.shared.generated.resources.sick_empty
import allergydetective.shared.generated.resources.sick_hint
import allergydetective.shared.generated.resources.sick_longer
import allergydetective.shared.generated.resources.sick_ongoing
import allergydetective.shared.generated.resources.sick_range
import allergydetective.shared.generated.resources.sick_recovered
import allergydetective.shared.generated.resources.sick_since
import allergydetective.shared.generated.resources.sick_still_ill
import allergydetective.shared.generated.resources.sick_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.domain.diary.SickPeriod
import com.nfar.allergydetective.ui.common.SectionTitle
import com.nfar.allergydetective.ui.common.formatDate
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

private const val DAY_MILLIS = 86_400_000L

@Composable
private fun dayText(epochDay: Long): String = formatDate(LocalDate.fromEpochDays(epochDay), Locale.current.language)

/** "From … to …" in a calendar, or "still ill". Days in the future cannot be picked. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SickPeriodDialog(
    today: Long,
    onConfirm: (startDay: Long, endDay: Long?) -> Unit,
    onDismiss: () -> Unit,
    /** A period to change (from the history), or null for a new one. */
    initial: SickPeriod? = null,
) {
    val todayMillis = today * DAY_MILLIS
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = (initial?.startDay ?: today) * DAY_MILLIS,
        initialSelectedEndDateMillis = initial?.endDay?.times(DAY_MILLIS),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMillis
        },
    )
    var stillIll by remember { mutableStateOf(initial == null || initial.endDay == null) }
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val start = state.selectedStartDateMillis?.div(DAY_MILLIS) ?: return@Button
                    val end = if (stillIll) null else state.selectedEndDateMillis?.div(DAY_MILLIS) ?: start
                    onConfirm(start, end)
                },
                enabled = state.selectedStartDateMillis != null,
            ) { Text(stringResource(Res.string.common_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } },
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                Checkbox(checked = stillIll, onCheckedChange = { stillIll = it })
                Text(stringResource(Res.string.sick_still_ill))
            }
            DateRangePicker(
                state = state,
                title = { Text(stringResource(Res.string.sick_title), modifier = Modifier.padding(start = 24.dp, top = 8.dp)) },
                showModeToggle = false,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** "12.11.2026 – 15.11.2026", "since 12.11.2026 · ongoing" or a single day. */
@Composable
fun sickPeriodText(period: SickPeriod): String = when (val end = period.endDay) {
    null -> stringResource(Res.string.sick_since, dayText(period.startDay)) + " · " + stringResource(Res.string.sick_ongoing)
    period.startDay -> dayText(period.startDay)
    else -> stringResource(Res.string.sick_range, dayText(period.startDay), dayText(end))
}

/** Home screen: shown while a period is open. */
@Composable
fun SickBanner(ongoing: SickPeriod, onRecovered: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(Res.string.sick_banner, dayText(ongoing.startDay)), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(Res.string.sick_hint), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onRecovered) { Text(stringResource(Res.string.sick_recovered)) }
        }
    }
}

/** Day check: link to enter a period, and a note when the chosen day lies in one. */
@Composable
fun SickPeriodDayCheckPart(day: Long, viewModel: SickPeriodsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    if (state.isSick(day)) {
        Text(stringResource(Res.string.sick_day_in_period), style = MaterialTheme.typography.bodySmall)
    }
    // Feedback 20.11.: a button with a calendar, so it does not look like plain text.
    OutlinedButton(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
    ) {
        Icon(Icons.Outlined.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(stringResource(Res.string.sick_longer), modifier = Modifier.padding(start = 8.dp))
    }
    if (showDialog) {
        SickPeriodDialog(
            today = state.today,
            onConfirm = { start, end ->
                showDialog = false
                viewModel.onAdd(start, end)
            },
            onDismiss = { showDialog = false },
        )
    }
}

/** Profile: all periods with delete, and "+ enter period". */
@Composable
fun SickPeriodSection(viewModel: SickPeriodsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(Res.string.sick_title))
        Text(stringResource(Res.string.sick_hint), style = MaterialTheme.typography.bodySmall)
        if (state.periods.isEmpty()) {
            Text(stringResource(Res.string.sick_empty), style = MaterialTheme.typography.bodyMedium)
        }
        state.periods.forEach { period ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(sickPeriodText(period), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = { viewModel.onDelete(period.id) }) { Text(stringResource(Res.string.history_delete)) }
            }
        }
        OutlinedButton(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(stringResource(Res.string.sick_add))
        }
    }
    if (showDialog) {
        SickPeriodDialog(
            today = state.today,
            onConfirm = { start, end ->
                showDialog = false
                viewModel.onAdd(start, end)
            },
            onDismiss = { showDialog = false },
        )
    }
}
