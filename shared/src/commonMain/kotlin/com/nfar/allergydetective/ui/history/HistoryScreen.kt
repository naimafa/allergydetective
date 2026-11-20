package com.nfar.allergydetective.ui.history

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.history_day_confirmed
import allergydetective.shared.generated.resources.history_day_open
import allergydetective.shared.generated.resources.history_delete
import allergydetective.shared.generated.resources.history_delete_question
import allergydetective.shared.generated.resources.history_delete_sick_question
import allergydetective.shared.generated.resources.history_empty
import allergydetective.shared.generated.resources.history_sick_add
import allergydetective.shared.generated.resources.history_sick_badge
import allergydetective.shared.generated.resources.weekdays_short
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.domain.diary.DayCheckStatus
import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.diary.SickPeriod
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.ui.common.StatusBadge
import com.nfar.allergydetective.ui.common.StatusKind
import com.nfar.allergydetective.ui.common.formatDate
import com.nfar.allergydetective.ui.daycheck.DayCheckViewModel
import com.nfar.allergydetective.ui.sick.SickPeriodDialog
import com.nfar.allergydetective.ui.sick.SickPeriodsViewModel
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * History: entries per day in diary order (self-test 18.11.), filter chips, day checks and periods
 * of illness (19.11.). Design of 19.11.: the date sticks to the top, every entry is one row with a
 * coloured icon, and a tap opens a sheet with its details and actions.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    onEditMeal: (Long) -> Unit = {},
    /** Opens the day check for this day (only today and the two days before can be changed). */
    onOpenDayCheck: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = koinViewModel(),
    sickViewModel: SickPeriodsViewModel = koinViewModel(),
    clock: AppClock = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sickState by sickViewModel.state.collectAsStateWithLifecycle()
    var opened by remember { mutableStateOf<HistoryEntry?>(null) }
    var toDelete by remember { mutableStateOf<HistoryEntry?>(null) }
    var sickActions by remember { mutableStateOf<SickPeriod?>(null) }
    // A period to change, or a new one (id 0).
    var sickEditing by remember { mutableStateOf<SickPeriod?>(null) }
    var favoriteFor by remember { mutableStateOf<Long?>(null) }
    var sickToDelete by remember { mutableStateOf<SickPeriod?>(null) }
    LaunchedEffect(Unit) {
        viewModel.refresh()
        sickViewModel.refresh()
    }

    val sickDays = state.days.map { it.epochDay }.filter { day -> sickState.periods.any { it.contains(day, sickState.today) } }.toSet()
    val visibleDays = state.visibleDays(sickDays)
    fun isSick(day: HistoryDay) = day.epochDay in sickDays || day.check?.factorIds.orEmpty().any { state.factorKeys[it] == SICK_FACTOR_KEY }
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
        // Phone test 19.11.: the band of a period of illness appears once, on its newest listed day.
        val bandDays = sickState.periods.mapNotNull { period ->
            visibleDays.map { it.epochDay }.filter { period.contains(it, sickState.today) }.maxOrNull()
        }.toSet()
        visibleDays.forEach { day ->
            val period = sickState.periods.firstOrNull { it.contains(day.epochDay, sickState.today) }
            // Phone test 19.11.: with many entries the date stays visible at the top while scrolling the day.
            stickyHeader(key = "date-${day.epochDay}") { DayHeader(day, clock, sick = isSick(day)) }
            item(key = "day-${day.epochDay}") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if ((period != null && day.epochDay in bandDays) || (period == null && isSick(day))) {
                        SickBand(period, onEdit = { sickActions = it })
                    }
                    if (state.showsDayChecks) {
                        day.check?.let { check ->
                            val changeable = sickState.today - day.epochDay in 0..DayCheckViewModel.DAYS_BACK.toLong()
                            DayCheckEntryRow(check, state.factorKeys, onClick = if (changeable) ({ onOpenDayCheck(day.epochDay) }) else null)
                        }
                    }
                    day.entries.forEach { entry -> EntryRow(entry, clock, onClick = { opened = entry }) }
                }
            }
        }
    }

    opened?.let { entry ->
        val day = state.days.firstOrNull { day -> day.entries.any { it.id == entry.id && it::class == entry::class } }
        EntrySheet(
            entry = entry,
            clock = clock,
            sickDay = day?.let(::isSick) == true,
            onEdit = {
                opened = null
                onEditMeal(it)
            },
            onSaveFavorite = {
                opened = null
                favoriteFor = it
            },
            onRelation = { symptom, relation ->
                viewModel.onIllnessRelationChange(symptom, relation)
                opened = symptom.copy(illnessRelation = relation)
            },
            onDelete = {
                opened = null
                toDelete = it
            },
            onDismiss = { opened = null },
        )
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
        ConfirmDelete(stringResource(Res.string.history_delete_sick_question), onConfirm = { sickViewModel.onDelete(period.id) }, onDone = { sickToDelete = null })
    }
    toDelete?.let { entry ->
        ConfirmDelete(stringResource(Res.string.history_delete_question), onConfirm = { viewModel.onDelete(entry) }, onDone = { toDelete = null })
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
}

@Composable
private fun ConfirmDelete(question: String, onConfirm: () -> Unit, onDone: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDone,
        text = { Text(question) },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDone()
            }) { Text(stringResource(Res.string.history_delete)) }
        },
        dismissButton = { TextButton(onClick = onDone) { Text(stringResource(Res.string.common_cancel)) } },
    )
}

/** "Mi, 18.11.2026", "Krank" on ill days and done / open. Sticky above the day, on the screen background. */
@Composable
private fun DayHeader(day: HistoryDay, clock: AppClock, sick: Boolean) {
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
        if (sick) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(end = 6.dp),
            ) {
                Text(
                    stringResource(Res.string.history_sick_badge),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                )
            }
        }
        StatusBadge(
            if (confirmed) "✓ " + stringResource(Res.string.history_day_confirmed) else stringResource(Res.string.history_day_open),
            if (confirmed) StatusKind.DONE else StatusKind.OPEN,
        )
    }
}
