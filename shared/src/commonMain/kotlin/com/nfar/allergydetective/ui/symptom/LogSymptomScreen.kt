package com.nfar.allergydetective.ui.symptom

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.common_save
import allergydetective.shared.generated.resources.emergency_call
import allergydetective.shared.generated.resources.emergency_continue
import allergydetective.shared.generated.resources.emergency_text
import allergydetective.shared.generated.resources.emergency_title
import allergydetective.shared.generated.resources.home_log_symptom
import allergydetective.shared.generated.resources.meal_now
import allergydetective.shared.generated.resources.symptom_favorites
import allergydetective.shared.generated.resources.symptom_how_strong
import allergydetective.shared.generated.resources.symptom_intensity_1
import allergydetective.shared.generated.resources.symptom_intensity_2
import allergydetective.shared.generated.resources.symptom_intensity_3
import allergydetective.shared.generated.resources.symptom_intensity_4
import allergydetective.shared.generated.resources.symptom_intensity_5
import allergydetective.shared.generated.resources.symptom_intensity_range
import allergydetective.shared.generated.resources.symptom_intensity_selected
import allergydetective.shared.generated.resources.symptom_long_press_hint
import allergydetective.shared.generated.resources.symptom_note
import allergydetective.shared.generated.resources.symptom_part_after_meal
import allergydetective.shared.generated.resources.symptom_part_afternoon
import allergydetective.shared.generated.resources.symptom_part_all_day
import allergydetective.shared.generated.resources.symptom_part_evening
import allergydetective.shared.generated.resources.symptom_part_midday
import allergydetective.shared.generated.resources.symptom_part_morning
import allergydetective.shared.generated.resources.symptom_when
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import com.nfar.allergydetective.ui.common.AppFilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.domain.diary.SymptomInfo
import com.nfar.allergydetective.domain.time.SymptomDayPart
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.SavedSnackbarEffect
import com.nfar.allergydetective.ui.common.ScreenScaffold
import com.nfar.allergydetective.ui.common.SectionTitle
import com.nfar.allergydetective.ui.common.TimeChoiceRow
import com.nfar.allergydetective.ui.common.dayLabel
import allergydetective.shared.generated.resources.symptom_edit_title
import allergydetective.shared.generated.resources.symptom_for_day
import com.nfar.allergydetective.ui.common.ScaleChips
import com.nfar.allergydetective.ui.common.DayPicker
import com.nfar.allergydetective.ui.sick.IllnessRelationChoice
import com.nfar.allergydetective.ui.common.displayName
import com.nfar.allergydetective.ui.common.emergencyNumberFor
import androidx.compose.ui.text.intl.Locale
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun LogSymptomScreen(
    onBack: () -> Unit,
    /** A logged complaint to correct (from the history). */
    editEntryId: Long? = null,
    /** Add for an earlier day (from the history): 1 = yesterday, 2 = the day before. */
    daysAgo: Int? = null,
    viewModel: LogSymptomViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    SavedSnackbarEffect(state.savedEntryId, snackbarHostState, viewModel::onUndo, viewModel::onSavedShown)
    LaunchedEffect(editEntryId) { editEntryId?.let(viewModel::startEditing) }
    LaunchedEffect(daysAgo) { daysAgo?.let(viewModel::startForDay) }
    LaunchedEffect(state.finished) { if (state.finished) onBack() }

    ScreenScaffold(
        title = when {
            editEntryId != null -> stringResource(Res.string.symptom_edit_title)
            state.day > 0 -> stringResource(Res.string.symptom_for_day, dayLabel(state.day))
            else -> stringResource(Res.string.home_log_symptom)
        },
        onBack = onBack,
        snackbarHostState = snackbarHostState,
    ) { padding ->
        SymptomGrid(state, viewModel, Modifier.padding(padding))
    }

    state.emergencyNoticeFor?.let {
        EmergencyNotice(onContinue = viewModel::onEmergencyNoticeContinue, onDismiss = viewModel::onEmergencyNoticeDismiss)
    }
    state.draft?.let { SymptomOverlay(it, onSickDay = state.draftOnSickDay, day = state.day, today = state.today, viewModel = viewModel) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomGrid(state: LogSymptomState, viewModel: LogSymptomViewModel, modifier: Modifier) = BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    // Feedback 19.11.: compact tiles, names in one line. One text size for all tiles: the largest at
    // which every name fits (text width = half the screen minus padding, emoji circle and gaps).
    val textWidth = (maxWidth - 32.dp - TILE_GAP) / TILE_COLUMNS - 10.dp * 2 - EMOJI_SIZE - 8.dp - 4.dp
    val names = state.groups.flatMap { it.second }.map { displayName(NamePrefix.SYMPTOM, it.key) + if (it.isRedFlag) " ⚠️" else "" }
    val nameStyle = sharedNameStyle(names, textWidth)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { Text(stringResource(Res.string.symptom_long_press_hint), style = MaterialTheme.typography.bodySmall) }
        if (state.favorites.isNotEmpty()) {
            item { SectionTitle(stringResource(Res.string.symptom_favorites)) }
            item { TileRow(state.favorites, viewModel, nameStyle) }
        }
        state.groups.forEach { (group, symptoms) ->
            item(key = group) { GroupTitle(group) }
            item(key = "$group-tiles") { TileRow(symptoms, viewModel, nameStyle) }
        }
    }
}

/** bodyMedium (15 sp) down to 12 sp in steps of 0.5 sp: the largest size at which every name fits in one line. */
@Composable
private fun sharedNameStyle(names: List<String>, textWidth: Dp): TextStyle {
    val measurer = rememberTextMeasurer()
    val widthPx = with(LocalDensity.current) { textWidth.toPx() }
    val base = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp)
    return remember(names, widthPx, base) {
        var size = base.fontSize.value
        while (size > MIN_NAME_SP) {
            val candidate = base.copy(fontSize = size.sp)
            if (names.all { measurer.measure(it, candidate, maxLines = 1, softWrap = false).size.width <= widthPx }) return@remember candidate
            size -= 0.5f
        }
        base.copy(fontSize = MIN_NAME_SP.sp)
    }
}

private const val MIN_NAME_SP = 12f

/** Group name with a dot in the group colour, so tiles and heading belong together visibly. */
@Composable
private fun GroupTitle(group: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Box(
            Modifier.size(12.dp).background(symptomGroupColor(group), CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        )
        Text(
            displayName(NamePrefix.SYMPTOM_GROUP, group),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/**
 * Self-test 18.11.: tiles had different widths and looked restless.
 * Now a fixed grid: two equal columns, all tiles the same height.
 */
@Composable
private fun TileRow(symptoms: List<SymptomInfo>, viewModel: LogSymptomViewModel, nameStyle: TextStyle) {
    Column(verticalArrangement = Arrangement.spacedBy(TILE_GAP)) {
        symptoms.chunked(TILE_COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(TILE_GAP)) {
                row.forEach { SymptomTile(it, viewModel, nameStyle, Modifier.weight(1f)) }
                repeat(TILE_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private const val TILE_COLUMNS = 2
private val TILE_GAP = 8.dp
private val EMOJI_SIZE = 30.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SymptomTile(symptom: SymptomInfo, viewModel: LogSymptomViewModel, nameStyle: TextStyle, modifier: Modifier) {
    val haptics = LocalHapticFeedback.current
    Surface(
        color = symptomGroupColor(symptom.groupKey, symptom.isRedFlag),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.large,
        border = if (symptom.isRedFlag) BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary) else null,
        modifier = modifier
            .height(60.dp)
            .combinedClickable(
                onClick = { viewModel.onSymptomClick(symptom) },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onSymptomLongClick(symptom)
                },
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // A light circle behind the emoji lifts it off the pastel surface.
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), modifier = Modifier.size(EMOJI_SIZE)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(symptomEmoji(symptom.id), style = MaterialTheme.typography.titleSmall)
                }
            }
            Text(
                displayName(NamePrefix.SYMPTOM, symptom.key) + if (symptom.isRedFlag) " ⚠️" else "",
                style = nameStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** CONTENT_Symptome 3: shown before the overlay for swelling and shortness of breath. */
@Composable
private fun EmergencyNotice(onContinue: () -> Unit, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val emergencyNumber = emergencyNumberFor(Locale.current.region)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.emergency_title)) },
        text = { Text(stringResource(Res.string.emergency_text)) },
        confirmButton = {
            Button(onClick = { uriHandler.openUri("tel:$emergencyNumber") }) {
                Text(stringResource(Res.string.emergency_call, emergencyNumber))
            }
        },
        dismissButton = {
            TextButton(onClick = onContinue) { Text(stringResource(Res.string.emergency_continue)) }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomOverlay(draft: SymptomDraft, onSickDay: Boolean, day: Int, today: Long, viewModel: LogSymptomViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::onDraftDismiss,
        title = { Text(symptomEmoji(draft.symptom.id) + " " + displayName(NamePrefix.SYMPTOM, draft.symptom.key)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(Res.string.symptom_how_strong), style = MaterialTheme.typography.titleSmall)
                ScaleChips(draft.intensity, viewModel::onIntensityChange, labels = (1..LogSymptomViewModel.MAX_INTENSITY).map { it.toString() })
                // Feedback 20.11.: the chosen value in words, the range below.
                Text(
                    stringResource(Res.string.symptom_intensity_selected, draft.intensity, intensityWord(draft.intensity)),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(stringResource(Res.string.symptom_intensity_range), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(Res.string.symptom_when), style = MaterialTheme.typography.titleSmall)
                // Feedback 19.11.: the same day row in every form.
                DayPicker(day, today, viewModel::onDayChange)
                TimeChoiceRow(
                    options = viewModel.dayParts(),
                    selected = draft.dayPart,
                    quickLabel = { dayPartLabel(it) },
                    exact = draft.exactTime,
                    onQuickSelected = viewModel::onDayPartChange,
                    onExactSelected = viewModel::onExactTimeChange,
                )
                SymptomDurationChoice(draft.duration, viewModel::onDurationChange)
                if (onSickDay) IllnessRelationChoice(draft.illnessRelation, viewModel::onIllnessRelationChange)
                OutlinedTextField(
                    value = draft.note,
                    onValueChange = viewModel::onNoteChange,
                    label = { Text(stringResource(Res.string.symptom_note)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { Button(onClick = viewModel::onSaveDraft) { Text(stringResource(Res.string.common_save)) } },
        dismissButton = { TextButton(onClick = viewModel::onDraftDismiss) { Text(stringResource(Res.string.common_cancel)) } },
    )
}

/** 1 = kaum spürbar … 5 = sehr stark. */
@Composable
fun intensityWord(intensity: Int): String = stringResource(
    when (intensity) {
        1 -> Res.string.symptom_intensity_1
        2 -> Res.string.symptom_intensity_2
        3 -> Res.string.symptom_intensity_3
        4 -> Res.string.symptom_intensity_4
        else -> Res.string.symptom_intensity_5
    },
)

@Composable
private fun dayPartLabel(part: SymptomDayPart): String = stringResource(
    when (part) {
        SymptomDayPart.NOW -> Res.string.meal_now
        SymptomDayPart.MORNING -> Res.string.symptom_part_morning
        SymptomDayPart.MIDDAY -> Res.string.symptom_part_midday
        SymptomDayPart.AFTERNOON -> Res.string.symptom_part_afternoon
        SymptomDayPart.EVENING -> Res.string.symptom_part_evening
        SymptomDayPart.ALL_DAY -> Res.string.symptom_part_all_day
        SymptomDayPart.AFTER_MEAL -> Res.string.symptom_part_after_meal
    },
)
