package com.nfar.allergydetective.ui.symptom

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.common_save
import allergydetective.shared.generated.resources.emergency_call
import allergydetective.shared.generated.resources.emergency_continue
import allergydetective.shared.generated.resources.emergency_text
import allergydetective.shared.generated.resources.emergency_title
import allergydetective.shared.generated.resources.home_log_symptom
import allergydetective.shared.generated.resources.meal_hours_ago
import allergydetective.shared.generated.resources.meal_now
import allergydetective.shared.generated.resources.symptom_favorites
import allergydetective.shared.generated.resources.symptom_how_strong
import allergydetective.shared.generated.resources.symptom_intensity_range
import allergydetective.shared.generated.resources.symptom_long_press_hint
import allergydetective.shared.generated.resources.symptom_minutes_ago
import allergydetective.shared.generated.resources.symptom_note
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.domain.diary.SymptomInfo
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.SavedSnackbarEffect
import com.nfar.allergydetective.ui.common.ScreenScaffold
import com.nfar.allergydetective.ui.common.SectionTitle
import com.nfar.allergydetective.ui.common.displayName
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Phone number of the emergency call button. 112 works across the EU. */
private const val EMERGENCY_NUMBER = "112"

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
    Surface(
        color = if (symptom.isRedFlag) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .combinedClickable(
                onClick = { viewModel.onSymptomClick(symptom) },
                onLongClick = { viewModel.onSymptomLongClick(symptom) },
            ),
    ) {
        Text(
            displayName(NamePrefix.SYMPTOM, symptom.key),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/** CONTENT_Symptome 3: shown before the overlay for swelling and shortness of breath. */
@Composable
private fun EmergencyNotice(onContinue: () -> Unit, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.emergency_title)) },
        text = { Text(stringResource(Res.string.emergency_text)) },
        confirmButton = {
            Button(onClick = { uriHandler.openUri("tel:$EMERGENCY_NUMBER") }) {
                Text(stringResource(Res.string.emergency_call, EMERGENCY_NUMBER))
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
        title = { Text(displayName(NamePrefix.SYMPTOM, draft.symptom.key)) },
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
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LogSymptomViewModel.MINUTES_AGO_OPTIONS.forEach { minutes ->
                        FilterChip(
                            selected = draft.minutesAgo == minutes,
                            onClick = { viewModel.onMinutesAgoChange(minutes) },
                            label = { Text(minutesAgoLabel(minutes)) },
                        )
                    }
                }
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
private fun minutesAgoLabel(minutes: Int): String = when {
    minutes == 0 -> stringResource(Res.string.meal_now)
    minutes % 60 == 0 -> pluralStringResource(Res.plurals.meal_hours_ago, minutes / 60, minutes / 60)
    else -> pluralStringResource(Res.plurals.symptom_minutes_ago, minutes, minutes)
}
