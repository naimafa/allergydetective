package com.nfar.allergydetective.ui.history

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.history_day_confirmed
import allergydetective.shared.generated.resources.history_day_open
import allergydetective.shared.generated.resources.history_delete
import allergydetective.shared.generated.resources.history_delete_question
import allergydetective.shared.generated.resources.history_empty
import allergydetective.shared.generated.resources.history_intensity
import allergydetective.shared.generated.resources.history_meal
import allergydetective.shared.generated.resources.meal_prep_cooked
import allergydetective.shared.generated.resources.meal_prep_raw
import allergydetective.shared.generated.resources.meal_slot_breakfast
import allergydetective.shared.generated.resources.meal_slot_dinner
import allergydetective.shared.generated.resources.meal_slot_lunch
import allergydetective.shared.generated.resources.weekdays_short
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.diary.DayCheckStatus
import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.domain.time.MealSlot
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.displayName
import com.nfar.allergydetective.ui.common.formatClock
import com.nfar.allergydetective.ui.common.formatDate
import com.nfar.allergydetective.ui.meal.ingredientName
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/** History (self-test 18.11.: tidied up): one card per day, entries in diary order. */
@Composable
fun HistoryScreen(modifier: Modifier = Modifier, viewModel: HistoryViewModel = koinViewModel(), clock: AppClock = koinInject()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<HistoryEntry?>(null) }
    LaunchedEffect(Unit) { viewModel.refresh() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!state.loading && state.days.isEmpty()) {
            item { Text(stringResource(Res.string.history_empty)) }
        }
        items(state.days, key = { "day-${it.epochDay}" }) { day ->
            DayCard(day, clock, onDelete = { toDelete = it })
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
private fun DayCard(day: HistoryDay, clock: AppClock, onDelete: (HistoryEntry) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            DayHeader(day, clock)
            day.entries.forEachIndexed { index, entry ->
                if (index > 0) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                EntryRow(entry, clock, onDelete = { onDelete(entry) })
            }
        }
    }
}

/** "Mi, 18.11.2026" and a small badge: confirmed or open. */
@Composable
private fun DayHeader(day: HistoryDay, clock: AppClock) {
    val date = clock.localDate(day.epochDay)
    val weekday = stringArrayResource(Res.array.weekdays_short).getOrNull(date.dayOfWeek.ordinal).orEmpty()
    val confirmed = day.status == DayCheckStatus.NO_SYMPTOMS || day.status == DayCheckStatus.COMPLETE
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
    ) {
        Text(
            "$weekday, ${formatDate(date, Locale.current.language)}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (confirmed) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (confirmed) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Text(
                if (confirmed) "✓ " + stringResource(Res.string.history_day_confirmed) else stringResource(Res.string.history_day_open),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

/** Time on the left, content in the middle, a small delete icon on the right. */
@Composable
private fun EntryRow(entry: HistoryEntry, clock: AppClock, onDelete: () -> Unit) {
    val (hour, minute) = clock.localTime(entry.time)
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, bottom = 4.dp)) {
        Text(
            formatClock(hour, minute),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(52.dp).padding(top = 2.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            when (entry) {
                is HistoryEntry.MealEntry -> MealContent(entry, hour, minute)
                is HistoryEntry.SymptomEntry -> SymptomContent(entry)
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = stringResource(Res.string.history_delete),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Title (breakfast / lunch / dinner, and the dish), then one line per ingredient. */
@Composable
private fun MealContent(entry: HistoryEntry.MealEntry, hour: Int, minute: Int) {
    // Logged as breakfast, lunch or dinner: the time is exactly the time of that slot.
    val slot = MealSlot.entries.firstOrNull { it != MealSlot.SNACK && it.hour == hour && it.minute == minute }
    val label = stringResource(
        when (slot) {
            MealSlot.BREAKFAST -> Res.string.meal_slot_breakfast
            MealSlot.LUNCH -> Res.string.meal_slot_lunch
            MealSlot.DINNER -> Res.string.meal_slot_dinner
            else -> Res.string.history_meal
        },
    )
    Text(listOfNotNull("🍽️ $label", entry.dishName).joinToString(" · "), style = MaterialTheme.typography.titleSmall)
    entry.items.forEach { (ingredient, portion) ->
        val preparation = when (entry.preparations[ingredient.id]) {
            Preparation.RAW -> stringResource(Res.string.meal_prep_raw)
            Preparation.COOKED -> stringResource(Res.string.meal_prep_cooked)
            null -> null
        }
        Text(
            listOfNotNull(ingredientName(ingredient), portion.name.first().toString(), preparation).joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SymptomContent(entry: HistoryEntry.SymptomEntry) {
    Text("⚡ " + displayName(NamePrefix.SYMPTOM, entry.symptomKey), style = MaterialTheme.typography.titleSmall)
    Text(stringResource(Res.string.history_intensity, entry.intensity), style = MaterialTheme.typography.bodyMedium)
    entry.note?.let { Text(it, style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)) }
}
