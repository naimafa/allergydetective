package com.nfar.allergydetective.ui.meal

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_save
import allergydetective.shared.generated.resources.custom_create_named
import allergydetective.shared.generated.resources.home_log_meal
import allergydetective.shared.generated.resources.meal_allergy_hint
import allergydetective.shared.generated.resources.meal_favorite_name
import allergydetective.shared.generated.resources.meal_favorites
import allergydetective.shared.generated.resources.meal_save_as_favorite
import allergydetective.shared.generated.resources.meal_hours_ago
import allergydetective.shared.generated.resources.meal_no_results
import allergydetective.shared.generated.resources.meal_now
import allergydetective.shared.generated.resources.meal_portion_help
import allergydetective.shared.generated.resources.meal_portion_large
import allergydetective.shared.generated.resources.meal_portion_medium
import allergydetective.shared.generated.resources.meal_portion_small
import allergydetective.shared.generated.resources.meal_recent
import allergydetective.shared.generated.resources.meal_remove
import allergydetective.shared.generated.resources.meal_search
import allergydetective.shared.generated.resources.meal_selected
import allergydetective.shared.generated.resources.meal_when
import allergydetective.shared.generated.resources.meal_voice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.SavedSnackbarEffect
import com.nfar.allergydetective.ui.common.ScreenScaffold
import com.nfar.allergydetective.ui.common.SectionTitle
import com.nfar.allergydetective.ui.common.TimeChoiceRow
import com.nfar.allergydetective.platform.rememberSpeechInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.intl.Locale
import com.nfar.allergydetective.ui.common.displayName
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LogMealScreen(
    onBack: () -> Unit,
    viewModel: LogMealViewModel = koinViewModel(),
    customViewModel: CustomIngredientViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCustomDialog by remember { mutableStateOf(false) }
    SavedSnackbarEffect(state.savedMealId, snackbarHostState, viewModel::onUndo, viewModel::onSavedShown)
    if (showCustomDialog) {
        CustomIngredientDialog(
            viewModel = customViewModel,
            onCreated = {
                showCustomDialog = false
                viewModel.onCustomIngredientCreated(it)
            },
            onDismiss = { showCustomDialog = false },
        )
    }
    val openCustomDialog: () -> Unit = {
        customViewModel.start(state.query)
        showCustomDialog = true
    }

    ScreenScaffold(
        title = stringResource(Res.string.home_log_meal),
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            Button(
                onClick = viewModel::onSave,
                enabled = state.canSave,
                modifier = Modifier.navigationBarsPadding().fillMaxWidth().padding(16.dp).heightIn(min = 56.dp),
            ) { Text(stringResource(Res.string.common_save)) }
        },
    ) { padding ->
        LogMealContent(state, viewModel, openCustomDialog, Modifier.padding(padding))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LogMealContent(
    state: LogMealState,
    viewModel: LogMealViewModel,
    onCreateIngredient: () -> Unit,
    modifier: Modifier,
) {
    val selectedIds = state.selected.map { it.ingredient.id }.toSet()
    val voiceLabel = stringResource(Res.string.meal_voice)
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        item { SectionTitle(stringResource(Res.string.meal_when)) }
        item {
            TimeChoiceRow(
                options = LogMealViewModel.HOURS_AGO_OPTIONS,
                selected = state.hoursAgo,
                quickLabel = { hours ->
                    if (hours == 0) stringResource(Res.string.meal_now)
                    else pluralStringResource(Res.plurals.meal_hours_ago, hours, hours)
                },
                exact = state.exactTime,
                onQuickSelected = viewModel::onHoursAgoChange,
                onExactSelected = viewModel::onExactTimeChange,
            )
        }
        if (state.favorites.isNotEmpty()) {
            item { SectionTitle(stringResource(Res.string.meal_favorites)) }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.favorites.forEach { favorite ->
                        FilterChip(
                            selected = state.favoriteId == favorite.id,
                            onClick = { viewModel.onFavoriteClick(favorite) },
                            label = { Text(favorite.name) },
                        )
                    }
                }
            }
        }
        if (state.recent.isNotEmpty()) {
            item { SectionTitle(stringResource(Res.string.meal_recent)) }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.recent.forEach { ingredient ->
                        FilterChip(
                            selected = ingredient.id in selectedIds,
                            onClick = { viewModel.onToggleIngredient(ingredient) },
                            label = { Text(ingredientName(ingredient)) },
                        )
                    }
                }
            }
        }
        item {
            val speech = rememberSpeechInput(Locale.current.toLanguageTag()) { text -> text?.let(viewModel::onVoiceResult) }
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text(stringResource(Res.string.meal_search)) },
                singleLine = true,
                trailingIcon = speech?.let { start ->
                    {
                        TextButton(onClick = start) {
                            Text("🎤", modifier = Modifier.semantics { contentDescription = voiceLabel })
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        items(state.results, key = { "result-${it.id}" }) { ingredient ->
            Text(
                text = ingredientName(ingredient),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable { viewModel.onToggleIngredient(ingredient) }
                    .padding(vertical = 12.dp),
            )
        }
        if (state.query.isNotBlank() && state.results.isEmpty()) {
            item { Text(stringResource(Res.string.meal_no_results), style = MaterialTheme.typography.bodyMedium) }
        }
        if (state.query.isNotBlank()) {
            item {
                TextButton(onClick = onCreateIngredient) {
                    Text(stringResource(Res.string.custom_create_named, state.query.trim()))
                }
            }
        }
        if (state.selected.isNotEmpty()) {
            item { SectionTitle(stringResource(Res.string.meal_selected)) }
            item { Text(stringResource(Res.string.meal_portion_help), style = MaterialTheme.typography.bodySmall) }
            items(state.selected, key = { "selected-${it.ingredient.id}" }) { item ->
                SelectedRow(item, viewModel)
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.saveAsFavorite, onCheckedChange = viewModel::onSaveAsFavoriteChange)
                    Text(stringResource(Res.string.meal_save_as_favorite))
                }
            }
            if (state.saveAsFavorite) {
                item {
                    OutlinedTextField(
                        value = state.favoriteName,
                        onValueChange = viewModel::onFavoriteNameChange,
                        label = { Text(stringResource(Res.string.meal_favorite_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        if (state.containsKnownAllergy) {
            item {
                Text(
                    stringResource(Res.string.meal_allergy_hint),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectedRow(item: SelectedIngredient, viewModel: LogMealViewModel) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(ingredientName(item.ingredient), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            TextButton(onClick = { viewModel.onToggleIngredient(item.ingredient) }) {
                Text(stringResource(Res.string.meal_remove))
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Portion.entries.forEach { portion ->
                FilterChip(
                    selected = item.portion == portion,
                    onClick = { viewModel.onPortionChange(item.ingredient.id, portion) },
                    label = { Text(portionLabel(portion)) },
                )
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

@Composable
fun ingredientName(ingredient: IngredientInfo): String =
    ingredient.key?.let { displayName(NamePrefix.INGREDIENT, it) } ?: ingredient.customName.orEmpty()