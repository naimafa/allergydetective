package com.nfar.allergydetective.ui.history

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.history_day_confirmed
import allergydetective.shared.generated.resources.history_day_open
import allergydetective.shared.generated.resources.history_delete
import allergydetective.shared.generated.resources.history_delete_question
import allergydetective.shared.generated.resources.history_delete_sick_question
import allergydetective.shared.generated.resources.history_edit
import allergydetective.shared.generated.resources.history_empty
import allergydetective.shared.generated.resources.history_intensity
import allergydetective.shared.generated.resources.history_meal
import allergydetective.shared.generated.resources.meal_prep_cooked
import allergydetective.shared.generated.resources.meal_prep_raw
import allergydetective.shared.generated.resources.meal_slot_breakfast
import allergydetective.shared.generated.resources.meal_slot_dinner
import allergydetective.shared.generated.resources.meal_slot_lunch
import allergydetective.shared.generated.resources.weekdays_short
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.StarOutline
import allergydetective.shared.generated.resources.history_save_favorite
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
import com.nfar.allergydetective.domain.diary.SickPeriod
import com.nfar.allergydetective.ui.sick.SickPeriodDialog
import com.nfar.allergydetective.ui.common.StatusBadge
import com.nfar.allergydetective.ui.common.StatusKind
import com.nfar.allergydetective.ui.sick.IllnessRelationChoice
import com.nfar.allergydetective.ui.sick.illnessRelationLabel
import com.nfar.allergydetective.domain.analysis.model.IllnessRelation
import allergydetective.shared.generated.resources.illness_label
import com.nfar.allergydetective.ui.sick.SickPeriodsViewModel
import allergydetective.shared.generated.resources.history_sick_add
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * History: one card per day, entries in diary order (self-test 18.11.). Since 19.11.: filter chips,
 * the day check of each day and periods of illness as a band that can be changed.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    onEditMeal: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = koinViewModel(),
    sickViewModel: SickPeriodsViewModel = koinViewModel(),
    clock: AppClock = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sickState by sickViewModel.state.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<HistoryEntry?>(null) }
    var sickActions by remember { mutableStateOf<SickPeriod?>(null) }
    // A period to change, or a new one (id 0).
    var sickEditing by remember { mutableStateOf<SickPeriod?>(null) }
    var relationFor by remember { mutableStateOf<HistoryEntry.SymptomEntry?>(null) }
    var favoriteFor by remember { mutableStateOf<Long?>(null) }
    var sickToDelete by remember { mutableStateOf<SickPeriod?>(null) }
    LaunchedEffect(Unit) {
        viewModel.refresh()
        sickViewModel.refresh()
    }

    val sickDays = state.days.map { it.epochDay }.filter { day -> sickState.periods.any { it.contains(day, sickState.today) } }.toSet()
    val visibleDays = state.visibleDays(sickDays)
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 16.dp)) {
                HistoryFilterRow(state.filter, viewModel::onFilterChange)
                TextButton(onClick = { sickEditing = SickPeriod(0, sickState.today, null) }) {
                    Text("+ " + stringResource(Res.string.history_sick_add))
                }
            }
        }
        if (!state.loading && visibleDays.isEmpty()) {
            item { Text(stringResource(Res.string.history_empty)) }
        }
        visibleDays.forEach { day ->
            // Phone test 19.11.: with many entries the date stays visible at the top while scrolling the day.
            stickyHeader(key = "date-${day.epochDay}") { DayHeader(day, clock) }
            item(key = "day-${day.epochDay}") {
                val period = sickState.periods.firstOrNull { it.contains(day.epochDay, sickState.today) }
                val markedSick = day.check?.factorIds.orEmpty().any { state.factorKeys[it] == SICK_FACTOR_KEY }
                DayCard(
                    day = day,
                    sick = period != null || markedSick,
                    clock = clock,
                    sickBand = { SickBand(period, onEdit = { sickActions = it }) },
                    onRelationClick = if (period != null || markedSick) { entry -> relationFor = entry } else null,
                    dayCheck = if (state.showsDayChecks) day.check?.let { { DayCheckRow(it, state.factorKeys) } } else null,
                    onEditMeal = onEditMeal,
                    onDelete = { toDelete = it },
                    onSaveFavorite = { favoriteFor = it },
                )
            }
        }
    }

    sickActions?.let { period ->
        SickPeriodActionsDialog(
            period = period,
            onChange = {
                sickActions = null
                sickEditing = period
            },
            onDelete = {
                sickActions = null
                sickToDelete = period
            },
            onDismiss = { sickActions = null },
        )
    }
    // Feedback 19.11.: every delete asks first.
    sickToDelete?.let { period ->
        AlertDialog(
            onDismissRequest = { sickToDelete = null },
            text = { Text(stringResource(Res.string.history_delete_sick_question)) },
            confirmButton = {
                Button(onClick = {
                    sickViewModel.onDelete(period.id)
                    sickToDelete = null
                }) { Text(stringResource(Res.string.history_delete)) }
            },
            dismissButton = { TextButton(onClick = { sickToDelete = null }) { Text(stringResource(Res.string.common_cancel)) } },
        )
    }
    favoriteFor?.let { mealId ->
        SaveFavoriteDialog(
            onSave = { name ->
                viewModel.onSaveAsFavorite(mealId, name)
                favoriteFor = null
            },
            onDismiss = { favoriteFor = null },
        )
    }
    relationFor?.let { entry ->
        AlertDialog(
            onDismissRequest = { relationFor = null },
            text = {
                IllnessRelationChoice(entry.illnessRelation) {
                    viewModel.onIllnessRelationChange(entry, it)
                    relationFor = null
                }
            },
            confirmButton = { TextButton(onClick = { relationFor = null }) { Text(stringResource(Res.string.common_cancel)) } },
        )
    }
    sickEditing?.let { period ->
        SickPeriodDialog(
            today = sickState.today,
            initial = period.takeIf { it.id != 0L },
            onConfirm = { start, end ->
                sickEditing = null
                if (period.id == 0L) sickViewModel.onAdd(start, end) else sickViewModel.onUpdate(period.id, start, end)
            },
            onDismiss = { sickEditing = null },
        )
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
private fun DayCard(
    day: HistoryDay,
    clock: AppClock,
    sick: Boolean,
    sickBand: @Composable () -> Unit,
    dayCheck: (@Composable () -> Unit)?,
    onEditMeal: (Long) -> Unit,
    onDelete: (HistoryEntry) -> Unit,
    /** Sick day: a symptom shows "part of the illness: ..." and opens the question. */
    onRelationClick: ((HistoryEntry.SymptomEntry) -> Unit)? = null,
    /** Meal not logged from a dish: star to save it as one (self-test 19.11.). */
    onSaveFavorite: (Long) -> Unit = {},
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            if (sick) sickBand()
            dayCheck?.let {
                it()
                if (day.entries.isNotEmpty()) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            day.entries.forEachIndexed { index, entry ->
                if (index > 0) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                EntryRow(
                    entry,
                    clock,
                    onEdit = (entry as? HistoryEntry.MealEntry)?.let { meal -> { onEditMeal(meal.id) } },
                    onDelete = { onDelete(entry) },
                    onRelation = (entry as? HistoryEntry.SymptomEntry)?.let { symptom -> onRelationClick?.let { click -> { click(symptom) } } },
                    onFavorite = (entry as? HistoryEntry.MealEntry)?.takeIf { it.dishName == null }?.let { meal -> { onSaveFavorite(meal.id) } },
                )
            }
        }
    }
}

/** "Mi, 18.11.2026" and a small badge: confirmed or open. Sticky above the day card, on the screen background. */
@Composable
private fun DayHeader(day: HistoryDay, clock: AppClock) {
    val date = clock.localDate(day.epochDay)
    val weekday = stringArrayResource(Res.array.weekdays_short).getOrNull(date.dayOfWeek.ordinal).orEmpty()
    val confirmed = day.status == DayCheckStatus.NO_SYMPTOMS || day.status == DayCheckStatus.COMPLETE
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(start = 4.dp, end = 4.dp, top = 16.dp, bottom = 8.dp),
    ) {
        Text(
            "$weekday, ${formatDate(date, Locale.current.language)}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        StatusBadge(
            if (confirmed) "✓ " + stringResource(Res.string.history_day_confirmed) else stringResource(Res.string.history_day_open),
            if (confirmed) StatusKind.DONE else StatusKind.OPEN,
        )
    }
}

/**
 * Time on the left, content in the middle, small edit (meals, self-test 19.11.) and delete icons on the right.
 * Tapping a meal opens it for editing, too.
 */
@Composable
private fun EntryRow(
    entry: HistoryEntry,
    clock: AppClock,
    onEdit: (() -> Unit)?,
    onDelete: () -> Unit,
    onRelation: (() -> Unit)? = null,
    onFavorite: (() -> Unit)? = null,
) {
    val (hour, minute) = clock.localTime(entry.time)
    val rowModifier = if (onEdit != null) Modifier.clickable(onClick = onEdit) else Modifier
    Row(verticalAlignment = Alignment.Top, modifier = rowModifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, bottom = 4.dp)) {
        Text(
            formatClock(hour, minute),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(52.dp).padding(top = 2.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            when (entry) {
                is HistoryEntry.MealEntry -> MealContent(entry, hour, minute)
                is HistoryEntry.SymptomEntry -> SymptomContent(entry, onRelation)
            }
        }
        if (onFavorite != null) {
            IconButton(onClick = onFavorite) {
                Icon(Icons.Outlined.StarOutline, contentDescription = stringResource(Res.string.history_save_favorite), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (onEdit != null) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = stringResource(Res.string.history_edit), tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val slot = MealSlot.at(hour, minute)
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
private fun SymptomContent(entry: HistoryEntry.SymptomEntry, onRelation: (() -> Unit)?) {
    Text("⚡ " + displayName(NamePrefix.SYMPTOM, entry.symptomKey), style = MaterialTheme.typography.titleSmall)
    Text(stringResource(Res.string.history_intensity, entry.intensity), style = MaterialTheme.typography.bodyMedium)
    entry.note?.let { Text(it, style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)) }
    if (onRelation != null) {
        val relation = illnessRelationLabel(entry.illnessRelation ?: IllnessRelation.UNKNOWN)
        Text(
            stringResource(Res.string.illness_label, relation),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onRelation).padding(vertical = 6.dp),
        )
    }
}
