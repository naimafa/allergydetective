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
import allergydetective.shared.generated.resources.symptom_intensity_range
import allergydetective.shared.generated.resources.symptom_long_press_hint
import allergydetective.shared.generated.resources.symptom_note
import allergydetective.shared.generated.resources.symptom_part_after_meal
import allergydetective.shared.generated.resources.symptom_part_afternoon
import allergydetective.shared.generated.resources.symptom_part_all_day
import allergydetective.shared.generated.resources.symptom_part_evening
import allergydetective.shared.generated.resources.symptom_part_midday
import allergydetective.shared.generated.resources.symptom_part_morning
import allergydetective.shared.generated.resources.symptom_when
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import com.nfar.allergydetective.ui.common.displayName
import com.nfar.allergydetective.ui.common.emergencyNumberFor
import androidx.compose.ui.text.intl.Locale
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun LogSymptomScreen(onBack: () -> Unit, viewModel: LogSymptomViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    SavedSnackbarEffect(state.savedEntryId, snackbarHostState, viewModel::onUndo, viewModel::onSavedShown)

    ScreenScaffold(
        title = stringResource(Res.string.home_log_symptom),
        onBack = onBack,
        snackbarHostState = snackbarHostState,
    ) { padding ->
        SymptomGrid(state, viewModel, Modifier.padding(padding))
    }

    state.emergencyNoticeFor?.let {
        EmergencyNotice(onContinue = viewModel::onEmergencyNoticeContinue, onDismiss = viewModel::onEmergencyNoticeDismiss)
    }
    state.draft?.let { SymptomOverlay(it, viewModel) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomGrid(state: LogSymptomState, viewModel: LogSymptomViewModel, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { Text(stringResource(Res.string.symptom_long_press_hint), style = MaterialTheme.typography.bodySmall) }
        if (state.favorites.isNotEmpty()) {
            item { SectionTitle(stringResource(Res.string.symptom_favorites)) }
            item { TileRow(state.favorites, viewModel) }
        }
        state.groups.forEach { (group, symptoms) ->
            item(key = group) { SectionTitle(displayName(NamePrefix.SYMPTOM_GROUP, group)) }
            item(key = "$group-tiles") { TileRow(symptoms, viewModel) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TileRow(symptoms: List<SymptomInfo>, viewModel: LogSymptomViewModel) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        symptoms.forEach { SymptomTile(it, viewModel) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SymptomTile(symptom: SymptomInfo, viewModel: LogSymptomViewModel) {
    val haptics = LocalHapticFeedback.current
    Surface(
        color = symptomGroupColor(symptom.groupKey, symptom.isRedFlag),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .heightIn(min = 56.dp)
            .combinedClickable(
                onClick = { viewModel.onSymptomClick(symptom) },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onSymptomLongClick(symptom)
                },
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(symptomEmoji(symptom.id), style = MaterialTheme.typography.titleMedium)
            Text(displayName(NamePrefix.SYMPTOM, symptom.key), style = MaterialTheme.typography.bodyLarge)
            if (symptom.isRedFlag) Text("⚠️", style = MaterialTheme.typography.bodyLarge)
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
private fun SymptomOverlay(draft: SymptomDraft, viewModel: LogSymptomViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::onDraftDismiss,
        title = { Text(symptomEmoji(draft.symptom.id) + " " + displayName(NamePrefix.SYMPTOM, draft.symptom.key)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(Res.string.symptom_how_strong), style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..LogSymptomViewModel.MAX_INTENSITY).forEach { value ->
                        FilterChip(
                            selected = draft.intensity == value,
                            onClick = { viewModel.onIntensityChange(value) },
                            label = { Text(value.toString()) },
                        )
                    }
                }
                Text(stringResource(Res.string.symptom_intensity_range), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(Res.string.symptom_when), style = MaterialTheme.typography.titleSmall)
                TimeChoiceRow(
                    options = viewModel.dayParts(),
                    selected = draft.dayPart,
                    quickLabel = { dayPartLabel(it) },
                    exact = draft.exactTime,
                    onQuickSelected = viewModel::onDayPartChange,
                    onExactSelected = viewModel::onExactTimeChange,
                )
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
