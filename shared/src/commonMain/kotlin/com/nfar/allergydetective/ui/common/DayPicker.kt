package com.nfar.allergydetective.ui.common

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.day_pick_date
import allergydetective.shared.generated.resources.weekdays_short
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

private const val DAY_MILLIS = 86_400_000L

/**
 * The same day row in every form (feedback 19.11.): "Today · Yesterday · 📅 Date …".
 * The date chip opens a calendar for up to [MAX_DAYS_BACK] days back and then shows the chosen day,
 * e.g. "Mo, 16.11.". [marked] days (days ago) get a check mark, e.g. confirmed days in the day check.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayPicker(
    daysAgo: Int,
    today: Long,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    marked: Set<Int> = emptySet(),
) {
    var showCalendar by remember { mutableStateOf(false) }
    // Phone test 19.11.: in the symptom dialog "Datum" was cut to "Dat". No check mark (the green fill
    // marks the day), small gaps, so all three chips fit also with a large system font.
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        (0..QUICK_DAYS).forEach { option ->
            AppFilterChip(
                selected = daysAgo == option,
                onClick = { onSelect(option) },
                label = { Text(dayLabel(option)) },
                trailingIcon = doneMark(option in marked, selected = daysAgo == option),
            )
        }
        AppFilterChip(
            selected = daysAgo > QUICK_DAYS,
            onClick = { showCalendar = true },
            label = { Text(if (daysAgo > QUICK_DAYS) shortDate(today - daysAgo) else stringResource(Res.string.day_pick_date)) },
            trailingIcon = doneMark(daysAgo > QUICK_DAYS && daysAgo in marked, selected = true),
            leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) },
            showCheck = false,
        )
    }
    if (showCalendar) {
        val first = today - MAX_DAYS_BACK
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (today - daysAgo) * DAY_MILLIS,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis / DAY_MILLIS in first..today
            },
        )
        DatePickerDialog(
            onDismissRequest = { showCalendar = false },
            confirmButton = {
                Button(onClick = {
                    state.selectedDateMillis?.let { onSelect((today - it / DAY_MILLIS).toInt()) }
                    showCalendar = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showCalendar = false }) { Text(stringResource(Res.string.common_cancel)) } },
        ) {
            DatePicker(state = state, showModeToggle = false)
        }
    }
}

/** Today and yesterday have their own chip; from the day before on the calendar chip (feedback 19.11.: so the date chip stays visible). */
private const val QUICK_DAYS = 1

@Composable
private fun shortDate(epochDay: Long): String {
    val date = LocalDate.fromEpochDays(epochDay)
    val weekday = stringArrayResource(Res.array.weekdays_short).getOrNull(date.dayOfWeek.ordinal).orEmpty()
    return "$weekday, ${date.day.toString().padStart(2, '0')}.${(date.month.ordinal + 1).toString().padStart(2, '0')}."
}

/** Finished day: the "done" green of the status badges (dark mode feedback 19.11.: use it consistently). */
private fun doneMark(done: Boolean, selected: Boolean): (@Composable () -> Unit)? = if (!done) {
    null
} else {
    {
        // White on a selected (green) chip.
        val scheme = MaterialTheme.colorScheme
        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = if (selected) scheme.onPrimary else scheme.primary, modifier = Modifier.size(18.dp))
    }
}
