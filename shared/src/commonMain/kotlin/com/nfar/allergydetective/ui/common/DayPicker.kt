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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        (0..QUICK_DAYS).forEach { option ->
            AppFilterChip(
                selected = daysAgo == option,
                onClick = { onSelect(option) },
                label = { Text(dayLabel(option) + if (option in marked) " ✓" else "") },
            )
        }
        AppFilterChip(
            selected = daysAgo > QUICK_DAYS,
            onClick = { showCalendar = true },
            label = { Text(if (daysAgo > QUICK_DAYS) shortDate(today - daysAgo) + if (daysAgo in marked) " ✓" else "" else stringResource(Res.string.day_pick_date)) },
            leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) },
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
