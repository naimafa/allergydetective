package com.nfar.allergydetective.ui.history

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.history_check_sleep
import allergydetective.shared.generated.resources.history_check_sleep_quality
import allergydetective.shared.generated.resources.history_check_stress
import allergydetective.shared.generated.resources.history_check_title
import allergydetective.shared.generated.resources.history_check_wellbeing
import allergydetective.shared.generated.resources.history_delete
import allergydetective.shared.generated.resources.history_filter_all
import allergydetective.shared.generated.resources.history_filter_days
import allergydetective.shared.generated.resources.history_filter_food
import allergydetective.shared.generated.resources.history_filter_symptoms
import allergydetective.shared.generated.resources.history_sick_change
import allergydetective.shared.generated.resources.history_sick_marked
import allergydetective.shared.generated.resources.history_sick_period
import allergydetective.shared.generated.resources.sick_title
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Sick
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.domain.diary.DayCheckData
import com.nfar.allergydetective.domain.diary.SickPeriod
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.displayName
import com.nfar.allergydetective.ui.sick.sickPeriodText
import org.jetbrains.compose.resources.stringResource

/** Lifestyle factor "sick" (seed key), set in the day check. */
const val SICK_FACTOR_KEY = "sick"

/** "All · Food · Symptoms · Days" above the history (self-test 19.11.). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryFilterRow(selected: HistoryFilter, onSelect: (HistoryFilter) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HistoryFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = {
                    Text(
                        stringResource(
                            when (filter) {
                                HistoryFilter.ALL -> Res.string.history_filter_all
                                HistoryFilter.FOOD -> Res.string.history_filter_food
                                HistoryFilter.SYMPTOMS -> Res.string.history_filter_symptoms
                                HistoryFilter.DAYS -> Res.string.history_filter_days
                            },
                        ),
                    )
                },
            )
        }
    }
}

/**
 * Apricot band below the date of an ill day: the period (tap to change or delete it), or the
 * "sick" factor of the day check.
 */
@Composable
fun SickBand(period: SickPeriod?, onEdit: (SickPeriod) -> Unit) {
    val text = period?.let { stringResource(Res.string.history_sick_period, sickPeriodText(it)) }
        ?: stringResource(Res.string.history_sick_marked)
    val clickable = if (period != null) Modifier.clickable { onEdit(period) } else Modifier
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = clickable.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Icon(Icons.Outlined.Sick, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (period != null) Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

/** "Day check · Sleep 7 h · Stress 2/5 · Sport" in one row of the day card. */
@Composable
fun DayCheckRow(check: DayCheckData, factorKeys: Map<Long, String>) {
    val parts = buildList {
        check.wellbeing?.let { add(stringResource(Res.string.history_check_wellbeing, it)) }
        check.sleepHours?.let { add(stringResource(Res.string.history_check_sleep, formatHours(it))) }
        check.sleepQuality?.let { add(stringResource(Res.string.history_check_sleep_quality, it)) }
        check.stress?.let { add(stringResource(Res.string.history_check_stress, it)) }
        check.factorIds.sorted().mapNotNull(factorKeys::get).filter { it != SICK_FACTOR_KEY }
            .forEach { add(displayName(NamePrefix.FACTOR, it)) }
    }
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Icon(
            Icons.Outlined.Bedtime,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp, top = 2.dp).size(20.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(Res.string.history_check_title), style = MaterialTheme.typography.titleSmall)
            if (parts.isNotEmpty()) Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun formatHours(hours: Float): String =
    if (hours % 1f == 0f) hours.toInt().toString() else hours.toString()

/** Tapping the sick band: change the dates or delete the period. */
@Composable
fun SickPeriodActionsDialog(period: SickPeriod, onChange: () -> Unit, onDelete: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.sick_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(sickPeriodText(period), style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = onDelete) { Text(stringResource(Res.string.history_delete), color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { Button(onClick = onChange) { Text(stringResource(Res.string.history_sick_change)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } },
    )
}
