package com.nfar.allergydetective.ui.history

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.history_check_title
import allergydetective.shared.generated.resources.history_delete
import allergydetective.shared.generated.resources.history_edit
import allergydetective.shared.generated.resources.history_intensity
import allergydetective.shared.generated.resources.history_meal
import allergydetective.shared.generated.resources.history_save_favorite
import allergydetective.shared.generated.resources.meal_portion_large
import allergydetective.shared.generated.resources.meal_portion_medium
import allergydetective.shared.generated.resources.meal_portion_small
import allergydetective.shared.generated.resources.meal_prep_cooked
import allergydetective.shared.generated.resources.meal_prep_raw
import allergydetective.shared.generated.resources.meal_slot_breakfast
import allergydetective.shared.generated.resources.meal_slot_dinner
import allergydetective.shared.generated.resources.meal_slot_lunch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.diary.DayCheckData
import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.domain.time.MealSlot
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.displayName
import com.nfar.allergydetective.ui.common.formatClock
import com.nfar.allergydetective.ui.meal.ingredientName
import com.nfar.allergydetective.ui.sick.IllnessRelationChoice
import com.nfar.allergydetective.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource

/*
 * History rows (design of 19.11.): one white row per entry with a coloured icon circle (palette:
 * food = sage, symptoms = apricot, day check = lavender), title, one line of details and an arrow.
 * The actions live in a sheet that opens on tap.
 */

/** One row: icon circle, title, one line, arrow when it can be opened. */
@Composable
fun HistoryRow(
    icon: ImageVector,
    circle: Color,
    tint: Color,
    title: String,
    subtitle: String?,
    onClick: (() -> Unit)?,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = (if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(12.dp),
        ) {
            Surface(shape = CircleShape, color = circle, contentColor = tint, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp)) }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (onClick != null) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** "Mittagessen · Linsensuppe", "Mahlzeit" for other times. */
@Composable
fun mealTitle(entry: HistoryEntry.MealEntry, clock: AppClock): String {
    val (hour, minute) = clock.localTime(entry.time)
    val label = stringResource(
        when (MealSlot.at(hour, minute)) {
            MealSlot.BREAKFAST -> Res.string.meal_slot_breakfast
            MealSlot.LUNCH -> Res.string.meal_slot_lunch
            MealSlot.DINNER -> Res.string.meal_slot_dinner
            else -> Res.string.history_meal
        },
    )
    return listOfNotNull(label, entry.dishName).joinToString(" · ")
}

private fun timeOf(entry: HistoryEntry, clock: AppClock): String =
    clock.localTime(entry.time).let { (hour, minute) -> formatClock(hour, minute) }

@Composable
fun EntryRow(entry: HistoryEntry, clock: AppClock, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    when (entry) {
        is HistoryEntry.MealEntry -> HistoryRow(
            icon = Icons.Outlined.Restaurant,
            circle = scheme.primaryContainer,
            tint = scheme.primary,
            title = mealTitle(entry, clock),
            // The time matters for the analysis windows; the ingredients follow in one line.
            subtitle = (listOf(timeOf(entry, clock)) + entry.items.map { ingredientName(it.first) }).joinToString(" · "),
            onClick = onClick,
        )
        is HistoryEntry.SymptomEntry -> HistoryRow(
            icon = Icons.Outlined.Bolt,
            circle = scheme.tertiaryContainer,
            tint = scheme.tertiary,
            title = displayName(NamePrefix.SYMPTOM, entry.symptomKey),
            subtitle = timeOf(entry, clock) + " · " + stringResource(Res.string.history_intensity, entry.intensity),
            onClick = onClick,
        )
    }
}

/** The day check as a row: wellbeing, sleep, stress, factors in one line; opens the day check when it can still be changed. */
@Composable
fun DayCheckEntryRow(check: DayCheckData, factorKeys: Map<Long, String>, onClick: (() -> Unit)?) {
    val extra = AppColors.extra
    HistoryRow(
        icon = Icons.Outlined.Bedtime,
        circle = extra.clues,
        tint = extra.cluesAccent,
        title = stringResource(Res.string.history_check_title),
        subtitle = dayCheckSummary(check, factorKeys).takeIf { it.isNotEmpty() },
        onClick = onClick,
    )
}

/** Tap on a row: everything about the entry and what can be done with it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrySheet(
    entry: HistoryEntry,
    clock: AppClock,
    sickDay: Boolean,
    onEdit: (Long) -> Unit,
    onSaveFavorite: (Long) -> Unit,
    onRelation: (HistoryEntry.SymptomEntry, com.nfar.allergydetective.domain.analysis.model.IllnessRelation) -> Unit,
    onDelete: (HistoryEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (entry) {
                is HistoryEntry.MealEntry -> {
                    Text(mealTitle(entry, clock), style = MaterialTheme.typography.titleLarge)
                    Text(timeOf(entry, clock), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    entry.items.forEach { (ingredient, portion) ->
                        val preparation = when (entry.preparations[ingredient.id]) {
                            Preparation.RAW -> stringResource(Res.string.meal_prep_raw)
                            Preparation.COOKED -> stringResource(Res.string.meal_prep_cooked)
                            null -> null
                        }
                        Text(listOfNotNull(ingredientName(ingredient), portionLabel(portion), preparation).joinToString(" · "), style = MaterialTheme.typography.bodyLarge)
                    }
                    FilledTonalButton(onClick = { onEdit(entry.id) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(stringResource(Res.string.history_edit), modifier = Modifier.padding(start = 8.dp))
                    }
                    if (entry.dishName == null) {
                        FilledTonalButton(onClick = { onSaveFavorite(entry.id) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.StarOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(stringResource(Res.string.history_save_favorite), modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
                is HistoryEntry.SymptomEntry -> {
                    Text(displayName(NamePrefix.SYMPTOM, entry.symptomKey), style = MaterialTheme.typography.titleLarge)
                    Text(
                        timeOf(entry, clock) + " · " + stringResource(Res.string.history_intensity, entry.intensity),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    entry.note?.let { Text(it, style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic)) }
                    if (sickDay) IllnessRelationChoice(entry.illnessRelation) { onRelation(entry, it) }
                }
            }
            TextButton(onClick = { onDelete(entry) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                Text(stringResource(Res.string.history_delete), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun portionLabel(portion: Portion): String = stringResource(
    when (portion) {
        Portion.SMALL -> Res.string.meal_portion_small
        Portion.MEDIUM -> Res.string.meal_portion_medium
        Portion.LARGE -> Res.string.meal_portion_large
    },
)
