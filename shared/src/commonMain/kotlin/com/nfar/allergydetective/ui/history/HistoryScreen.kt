package com.nfar.allergydetective.ui.history

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.history_day_confirmed
import allergydetective.shared.generated.resources.history_day_open
import allergydetective.shared.generated.resources.history_delete
import allergydetective.shared.generated.resources.history_delete_question
import allergydetective.shared.generated.resources.history_empty
import allergydetective.shared.generated.resources.history_intensity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.domain.diary.DayCheckStatus
import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.SectionTitle
import com.nfar.allergydetective.ui.common.displayName
import com.nfar.allergydetective.ui.common.formatClock
import com.nfar.allergydetective.ui.common.formatDate
import androidx.compose.ui.text.intl.Locale
import com.nfar.allergydetective.ui.meal.ingredientName
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryScreen(modifier: Modifier = Modifier, viewModel: HistoryViewModel = koinViewModel(), clock: AppClock = koinInject()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<HistoryEntry?>(null) }
    LaunchedEffect(Unit) { viewModel.refresh() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!state.loading && state.days.isEmpty()) {
            item { Text(stringResource(Res.string.history_empty)) }
        }
        state.days.forEach { day ->
            item(key = "day-${day.epochDay}") {
                val date = clock.localDate(day.epochDay)
                val status = when (day.status) {
                    DayCheckStatus.NO_SYMPTOMS, DayCheckStatus.COMPLETE -> stringResource(Res.string.history_day_confirmed)
                    else -> stringResource(Res.string.history_day_open)
                }
                SectionTitle("${formatDate(date, Locale.current.language)} · $status")
            }
            day.entries.forEach { entry ->
                item(key = "${entry::class.simpleName}-${entry.id}") {
                    EntryRow(entry, clock, onDelete = { toDelete = entry })
                }
            }
            item(key = "divider-${day.epochDay}") { HorizontalDivider() }
        }
    }

    toDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            text = { Text(stringResource(Res.string.history_delete_question)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.onDelete(entry)
                    toDelete = null
                }) { Text(stringResource(Res.string.history_delete)) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text(stringResource(Res.string.common_cancel)) } },
        )
    }
}

@Composable
private fun EntryRow(entry: HistoryEntry, clock: AppClock, onDelete: () -> Unit) {
    val (hour, minute) = clock.localTime(entry.time)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(formatClock(hour, minute), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(end = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            when (entry) {
                is HistoryEntry.MealEntry -> Text(
                    "🍎 " + entry.items.map { (ingredient, portion) -> "${ingredientName(ingredient)} (${portion.name.first()})" }
                        .joinToString(", "),
                )
                is HistoryEntry.SymptomEntry -> {
                    Text("⚡ " + displayName(NamePrefix.SYMPTOM, entry.symptomKey) + " · " +
                        stringResource(Res.string.history_intensity, entry.intensity))
                    entry.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
        TextButton(onClick = onDelete) { Text(stringResource(Res.string.history_delete)) }
    }
}
