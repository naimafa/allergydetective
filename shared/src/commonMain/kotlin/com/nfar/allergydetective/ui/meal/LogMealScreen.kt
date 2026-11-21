package com.nfar.allergydetective.ui.meal

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_save
import allergydetective.shared.generated.resources.custom_create_named
import allergydetective.shared.generated.resources.home_log_meal
import allergydetective.shared.generated.resources.meal_allergy_hint
import allergydetective.shared.generated.resources.meal_added
import allergydetective.shared.generated.resources.meal_amount_empty
import allergydetective.shared.generated.resources.meal_edit_title
import allergydetective.shared.generated.resources.meal_existing_add
import allergydetective.shared.generated.resources.meal_existing_question
import allergydetective.shared.generated.resources.meal_existing_separate
import allergydetective.shared.generated.resources.meal_favorite_name
import allergydetective.shared.generated.resources.meal_favorites
import allergydetective.shared.generated.resources.meal_favorites_hint
import allergydetective.shared.generated.resources.meal_no_results
import allergydetective.shared.generated.resources.meal_portion_help
import allergydetective.shared.generated.resources.meal_portion_large
import allergydetective.shared.generated.resources.meal_portion_medium
import allergydetective.shared.generated.resources.meal_portion_small
import allergydetective.shared.generated.resources.meal_quick
import allergydetective.shared.generated.resources.meal_quick_hint
import allergydetective.shared.generated.resources.meal_remove
import allergydetective.shared.generated.resources.meal_save_as_favorite
import allergydetective.shared.generated.resources.meal_save_slot
import allergydetective.shared.generated.resources.meal_search
import allergydetective.shared.generated.resources.meal_slot_breakfast
import allergydetective.shared.generated.resources.meal_slot_dinner
import allergydetective.shared.generated.resources.meal_slot_drink
import allergydetective.shared.generated.resources.meal_slot_lunch
import allergydetective.shared.generated.resources.meal_slot_missing
import allergydetective.shared.generated.resources.meal_slot_snack
import allergydetective.shared.generated.resources.meal_step_amount
import allergydetective.shared.generated.resources.meal_step_what
import allergydetective.shared.generated.resources.meal_step_when
import allergydetective.shared.generated.resources.meal_voice
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AssistChip
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import com.nfar.allergydetective.ui.common.AppFilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.time.MealSlot
import com.nfar.allergydetective.platform.rememberSpeechInput
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.SavedSnackbarEffect
import com.nfar.allergydetective.ui.common.ScreenScaffold
import com.nfar.allergydetective.ui.common.StepCard
import com.nfar.allergydetective.ui.common.DayPicker
import com.nfar.allergydetective.ui.common.TimeLine
import com.nfar.allergydetective.ui.common.displayName
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Food log (UX 3.1), self-test 19.11.: three numbered steps as questions in their own cards,
 * so a new user sees what to do: 1 when, 2 what, 3 how much.
 */
@Composable
fun LogMealScreen(
    onBack: () -> Unit,
    /** A logged meal to edit (from the history), or null for a new one. */
    editMealId: Long? = null,
    /** A dish to start with ("Log" in the favourites). */
    favoriteId: Long? = null,
    /** Add for an earlier day from the history (feedback 19.11.). */
    daysAgo: Int? = null,
    viewModel: LogMealViewModel = koinViewModel(),
    customViewModel: CustomIngredientViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCustomDialog by remember { mutableStateOf(false) }
    LaunchedEffect(editMealId) { editMealId?.let(viewModel::startEditing) }
    LaunchedEffect(favoriteId) { favoriteId?.let(viewModel::startWithFavorite) }
    LaunchedEffect(daysAgo) { daysAgo?.let(viewModel::startForDay) }
    LaunchedEffect(state.finished) { if (state.finished) onBack() }
    SavedSnackbarEffect(state.savedMealId, snackbarHostState, viewModel::onUndo, viewModel::onSavedShown)
    state.addedTo?.let { slot ->
        val message = stringResource(Res.string.meal_added, mealSlotLabel(slot))
        LaunchedEffect(slot) {
            snackbarHostState.showSnackbar(message)
            viewModel.onSavedShown()
        }
    }
    if (state.existingMealId != null) {
        state.slot?.let { slot -> ExistingMealDialog(slot, viewModel) }
    }
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
        title = stringResource(if (editMealId != null) Res.string.meal_edit_title else Res.string.home_log_meal),
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            Button(
                onClick = viewModel::onSave,
                enabled = state.canSave,
                // Phone test 21.11.: the keyboard covered the button; it now sits right above the keyboard.
                modifier = Modifier.navigationBarsPadding().imePadding().fillMaxWidth().padding(16.dp).heightIn(min = 56.dp),
            ) {
                Text(state.slot?.let { stringResource(Res.string.meal_save_slot, mealSlotLabel(it)) } ?: stringResource(Res.string.common_save))
            }
        },
    ) { padding ->
        // The bottom bar moves up with the keyboard, so the list already ends above it.
        LogMealContent(state, viewModel, openCustomDialog, Modifier.padding(padding).consumeWindowInsets(padding))
    }
}

/** "There is already a dinner on this day. Add the ingredients to it?" (self-test 19.11.) */
@Composable
private fun ExistingMealDialog(slot: MealSlot, viewModel: LogMealViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::onExistingDismissed,
        text = { Text(stringResource(Res.string.meal_existing_question, mealSlotLabel(slot))) },
        confirmButton = { Button(onClick = viewModel::onAddToExisting) { Text(stringResource(Res.string.meal_existing_add)) } },
        dismissButton = { TextButton(onClick = viewModel::onSaveSeparately) { Text(stringResource(Res.string.meal_existing_separate)) } },
    )
}

@Composable
private fun LogMealContent(
    state: LogMealState,
    viewModel: LogMealViewModel,
    onCreateIngredient: () -> Unit,
    modifier: Modifier,
) {
    val listState = rememberLazyListState()
    // Save without a meal: show the "when" step with its hint.
    LaunchedEffect(state.slotMissing) {
        if (state.slotMissing) listState.animateScrollToItem(0)
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        item { WhenStep(state, viewModel) }
        item { WhatStep(state, viewModel, onCreateIngredient) }
        item { AmountStep(state, viewModel) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WhenStep(state: LogMealState, viewModel: LogMealViewModel) {
    StepCard(number = 1, title = stringResource(Res.string.meal_step_when), highlighted = state.slotMissing) {
        // Feedback 19.11.: the same day row in every form.
        DayPicker(state.day, state.today, viewModel::onDayChange)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MealSlot.entries.forEach { slot ->
                AppFilterChip(
                    selected = state.slot == slot,
                    onClick = { viewModel.onSlotChange(slot) },
                    label = { Text(mealSlotLabel(slot)) },
                )
            }
        }
        if (state.slotMissing) {
            Text(stringResource(Res.string.meal_slot_missing), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        state.shownTime?.let { TimeLine(it, viewModel::onExactTimeChange) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WhatStep(state: LogMealState, viewModel: LogMealViewModel, onCreateIngredient: () -> Unit) {
    val selectedIds = state.selected.map { it.ingredient.id }.toSet()
    val keyboard = LocalSoftwareKeyboardController.current
    val voiceLabel = stringResource(Res.string.meal_voice)
    // Phone test 21.11.: favourites and quick picks filled the screen, the search results disappeared below them.
    // While the search field is in use only the results show; otherwise each shortcut group is one scrollable row.
    var searchFocused by remember { mutableStateOf(false) }
    val searching = searchFocused || state.query.isNotBlank()
    StepCard(number = 2, title = stringResource(Res.string.meal_step_what)) {
        val speech = rememberSpeechInput(Locale.current.toLanguageTag()) { text -> text?.let(viewModel::onVoiceResult) }
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text(stringResource(Res.string.meal_search)) },
            singleLine = true,
            trailingIcon = speech?.let { start ->
                {
                    IconButton(onClick = start) { Icon(Icons.Outlined.Mic, contentDescription = voiceLabel) }
                }
            },
            modifier = Modifier.fillMaxWidth().onFocusChanged { searchFocused = it.isFocused },
        )
        if (state.dishResults.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.dishResults.forEach { dish ->
                    AssistChip(
                        onClick = { viewModel.onFavoriteClick(dish) },
                        label = { Text(dish.name) },
                        leadingIcon = { Icon(Icons.Outlined.StarOutline, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    )
                }
            }
        }
        state.results.forEach { ingredient ->
            Text(
                text = ingredientName(ingredient),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable {
                        viewModel.onToggleIngredient(ingredient)
                        // Search is done; without the keyboard the choice is visible right away (self-test 18.11.).
                        keyboard?.hide()
                    }
                    .padding(vertical = 12.dp),
            )
        }
        if (state.query.isNotBlank() && state.results.isEmpty()) {
            Text(stringResource(Res.string.meal_no_results), style = MaterialTheme.typography.bodyMedium)
        }
        if (state.query.isNotBlank() && !state.nameExists) {
            TextButton(onClick = onCreateIngredient) {
                Text(stringResource(Res.string.custom_create_named, state.query.trim()))
            }
        }
        if (!searching && state.favorites.isNotEmpty()) {
            ChipGroupTitle(stringResource(Res.string.meal_favorites), stringResource(Res.string.meal_favorites_hint))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.favorites, key = { it.id }) { favorite ->
                    AppFilterChip(
                        selected = state.favoriteId == favorite.id,
                        onClick = { viewModel.onFavoriteClick(favorite) },
                        label = { Text(favorite.name, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 220.dp)) },
                        leadingIcon = { Icon(Icons.Outlined.StarOutline, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    )
                }
            }
        }
        if (!searching && state.recent.isNotEmpty()) {
            ChipGroupTitle(stringResource(Res.string.meal_quick), stringResource(Res.string.meal_quick_hint))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.recent, key = { it.id }) { ingredient ->
                    AppFilterChip(
                        selected = ingredient.id in selectedIds,
                        onClick = { viewModel.onToggleIngredient(ingredient) },
                        label = { Text(ingredientName(ingredient), maxLines = 1) },
                    )
                }
            }
        }
        if (state.containsKnownAllergy) {
            Text(stringResource(Res.string.meal_allergy_hint), color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AmountStep(state: LogMealState, viewModel: LogMealViewModel) {
    StepCard(number = 3, title = stringResource(Res.string.meal_step_amount)) {
        if (state.selected.isEmpty()) {
            Text(stringResource(Res.string.meal_amount_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@StepCard
        }
        Text(stringResource(Res.string.meal_portion_help), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        // Newest first: what was just added appears on top.
        state.selected.asReversed().forEachIndexed { index, item ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
            SelectedRow(item, item.ingredient.id in state.needsPreparation, viewModel)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = state.saveAsFavorite, onCheckedChange = viewModel::onSaveAsFavoriteChange)
            Text(stringResource(Res.string.meal_save_as_favorite))
        }
        if (state.saveAsFavorite) {
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

@Composable
private fun ChipGroupTitle(title: String, hint: String) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectedRow(item: SelectedIngredient, needsPreparation: Boolean, viewModel: LogMealViewModel) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(ingredientName(item.ingredient), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            TextButton(onClick = { viewModel.onToggleIngredient(item.ingredient) }) {
                Text(stringResource(Res.string.meal_remove))
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Portion.entries.forEach { portion ->
                AppFilterChip(
                    selected = item.portion == portion,
                    onClick = { viewModel.onPortionChange(item.ingredient.id, portion) },
                    label = { Text(portionLabel(portion)) },
                )
            }
        }
        if (needsPreparation) {
            PreparationChoice(item.preparation) { viewModel.onPreparationChange(item.ingredient.id, it) }
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

@Composable
fun mealSlotLabel(slot: MealSlot): String = stringResource(
    when (slot) {
        MealSlot.BREAKFAST -> Res.string.meal_slot_breakfast
        MealSlot.LUNCH -> Res.string.meal_slot_lunch
        MealSlot.DINNER -> Res.string.meal_slot_dinner
        MealSlot.SNACK -> Res.string.meal_slot_snack
        MealSlot.DRINK -> Res.string.meal_slot_drink
    },
)
