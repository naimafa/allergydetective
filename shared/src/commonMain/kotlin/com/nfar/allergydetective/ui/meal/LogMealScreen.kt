package com.nfar.allergydetective.ui.meal

import allergydetective.shared.generated.resources.meal_save_as_favorite_hint
import allergydetective.shared.generated.resources.meal_also_called
import allergydetective.shared.generated.resources.meal_dish_contains
import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_save
import allergydetective.shared.generated.resources.custom_create_named
import allergydetective.shared.generated.resources.home_log_meal
import allergydetective.shared.generated.resources.meal_allergy_hint
import allergydetective.shared.generated.resources.meal_added
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
import allergydetective.shared.generated.resources.meal_step_what
import allergydetective.shared.generated.resources.meal_step_when
import allergydetective.shared.generated.resources.meal_step_which
import allergydetective.shared.generated.resources.meal_now
import allergydetective.shared.generated.resources.time_selected
import allergydetective.shared.generated.resources.common_done
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import com.nfar.allergydetective.ui.common.ExactTime
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.sp
import com.nfar.allergydetective.ui.common.dayLabel
import com.nfar.allergydetective.ui.common.formatClock
import com.nfar.allergydetective.ui.theme.AppColors
import allergydetective.shared.generated.resources.meal_voice
import allergydetective.shared.generated.resources.meal_add_more
import allergydetective.shared.generated.resources.meal_opened_existing
import allergydetective.shared.generated.resources.meal_portion_info
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Eco
import allergydetective.shared.generated.resources.meal_from
import allergydetective.shared.generated.resources.meal_ingredients_all
import allergydetective.shared.generated.resources.meal_merged_hint
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.TextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.nfar.allergydetective.ui.common.searchPillColors
import com.nfar.allergydetective.ui.common.ChipRowWithMore
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AssistChip
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import com.nfar.allergydetective.ui.common.AppFilterChip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.BoxWithConstraints
import allergydetective.shared.generated.resources.meal_dishes_chosen
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.ui.graphics.lerp
import allergydetective.shared.generated.resources.meal_single_ingredients
import allergydetective.shared.generated.resources.meal_dish_show_ingredients
import allergydetective.shared.generated.resources.meal_dish_hide_ingredients
import allergydetective.shared.generated.resources.meal_portion_help_dish
import allergydetective.shared.generated.resources.meal_dup_add_question
import allergydetective.shared.generated.resources.meal_dup_add_yes
import allergydetective.shared.generated.resources.meal_dup_add_no
import allergydetective.shared.generated.resources.meal_place_single
import allergydetective.shared.generated.resources.meal_multi_question
import allergydetective.shared.generated.resources.meal_multi_all
import allergydetective.shared.generated.resources.meal_multi_here
import allergydetective.shared.generated.resources.meal_remove_question
import allergydetective.shared.generated.resources.meal_remove_dish
import allergydetective.shared.generated.resources.common_cancel
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
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.domain.time.MealSlot
import com.nfar.allergydetective.platform.rememberSpeechInput
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.SavedSnackbarEffect
import com.nfar.allergydetective.ui.common.ScreenScaffold
import com.nfar.allergydetective.ui.common.StepCard
import com.nfar.allergydetective.ui.common.DayPicker
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
    IngredientQuestions(state, viewModel)
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
        title = stringResource(if (editMealId != null || state.openedExisting != null) Res.string.meal_edit_title else Res.string.home_log_meal),
        titleIcon = Icons.Filled.Restaurant,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            Button(
                onClick = viewModel::onSave,
                enabled = state.canSave,
                // Phone test 21.11.: the keyboard covered the button; it now sits right above the keyboard.
                shape = CircleShape,
                modifier = Modifier.navigationBarsPadding().imePadding().fillMaxWidth().padding(16.dp).heightIn(min = 56.dp),
            ) {
                // Design picture 23.11.2026: a check mark before the text.
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(22.dp))
                Text(
                    state.slot?.let { stringResource(Res.string.meal_save_slot, mealSlotLabel(it)) } ?: stringResource(Res.string.common_save),
                    modifier = Modifier.padding(start = 10.dp),
                )
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
    var searchFocused by remember { mutableStateOf(false) }
    val searchActive = searchFocused || state.query.isNotBlank()
    // Phone test 21.11.: with the keyboard open the search field sat at the bottom and the results had no room.
    // Starting a search scrolls step 2 to the top (the spacer below makes that possible).
    LaunchedEffect(searchFocused) {
        if (searchFocused) listState.animateScrollToItem(1)
    }
    // Save without a meal: show the "when" step with its hint.
    LaunchedEffect(state.slotMissing) {
        if (state.slotMissing) listState.animateScrollToItem(0)
    }
    // User 23.11.2026: favourites and recent ingredients stay visible after a choice (folding them away hid the
    // other dishes). "Weitere Zutat hinzufügen" opens the search.
    val searchFocus = remember { FocusRequester() }
    val onAddMore: () -> Unit = { searchFocus.requestFocus() }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 16.dp),
    ) {
        item { WhenStep(state, viewModel) }
        item {
            WhatStep(
                state = state,
                viewModel = viewModel,
                onCreateIngredient = onCreateIngredient,
                shortcuts = !searchActive,
                searchFocus = searchFocus,
                onSearchFocus = { searchFocused = it },
                onAddMore = onAddMore,
            )
        }
        // User 23.11.2026: saving as a dish in its own card, so it clearly means the whole meal, not one ingredient.
        if (state.mealItems.isNotEmpty()) item { SaveAsDishCard(state, viewModel) }
        // Room below the last step, so starting a search can scroll the search field to the top.
        if (searchActive) item { Spacer(Modifier.height(480.dp)) }
    }
}

/**
 * Design picture 22.11.2026: step 1 asks only "Welche Mahlzeit?" with the five kinds in two even rows
 * (user: "Welche Mahlzeit" sounds better than "Wann hast du gegessen"). Day and time moved into the pill at the top right.
 * Design picture 23.11.2026: each kind with its icon, in the fixed order of the day.
 */
@Composable
private fun WhenStep(state: LogMealState, viewModel: LogMealViewModel) {
    StepCard(number = 1, title = stringResource(Res.string.meal_step_which), highlighted = state.slotMissing) {
        MealSlotGrid(listOf(MealSlot.entries.take(2), MealSlot.entries.drop(2)), state.slot, viewModel::onSlotTap)
        WhenPill(state, viewModel)
        if (state.slotMissing) {
            Text(stringResource(Res.string.meal_slot_missing), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        state.openedExisting?.let { slot ->
            Text(stringResource(Res.string.meal_opened_existing, mealSlotLabel(slot)), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** "Jetzt" until a meal is chosen, then e.g. "Heute, 08:00" or "Gestern, 19:00": when the meal will be saved. */
@Composable
private fun whenLabel(state: LogMealState): String {
    val time = state.shownTime
    if (time == null && state.day == 0) return stringResource(Res.string.meal_now)
    val day = dayLabel(state.day)
    return time?.let { stringResource(Res.string.time_selected, day, formatClock(it.hour, it.minute)) } ?: day
}

/** The pill at the top right (design picture 22.11.2026); a tap opens day and time. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhenPill(state: LogMealState, viewModel: LogMealViewModel) {
    var open by remember { mutableStateOf(false) }
    val home = AppColors.extra.home
    // User 23.11.2026: at the top right the time was easy to miss. It now sits in step 1 under the meal it belongs
    // to, as an outlined pill with a pencil.
    Surface(
        onClick = { open = true },
        shape = CircleShape,
        color = home.page,
        contentColor = home.ink,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.heightIn(min = 40.dp).padding(horizontal = 14.dp)) {
            Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(whenLabel(state), style = MaterialTheme.typography.labelLarge, maxLines = 1, modifier = Modifier.padding(start = 8.dp, end = 8.dp))
            Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
    if (open) {
        // User 24.11.2026: the sheet jumped and the time field vanished under the keyboard: one fixed height,
        // and the content lifts with the keyboard.
        ModalBottomSheet(onDismissRequest = { open = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            Column(
                modifier = Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(Res.string.meal_step_when), style = MaterialTheme.typography.titleLarge)
                // Feedback 19.11.: the same day row in every form.
                DayPicker(state.day, state.today, viewModel::onDayChange)
                // User 23.11.2026: too many taps to change the time (pill → "Ändern" → dialog → OK → Fertig). The time
                // is now set right here and taken over with "Fertig".
                // Before a meal is chosen there is no stored time yet: the field starts at "now".
                val shown = remember(state.shownTime) { state.shownTime ?: viewModel.nowTime() }
                val timeState = rememberTimePickerState(initialHour = shown.hour, initialMinute = shown.minute, is24Hour = true)
                TimeInput(state = timeState, modifier = Modifier.align(Alignment.CenterHorizontally))
                Button(
                    onClick = {
                        if ((timeState.hour != shown.hour || timeState.minute != shown.minute)) {
                            viewModel.onExactTimeChange(ExactTime(state.day, timeState.hour, timeState.minute))
                        }
                        open = false
                    },
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Text(stringResource(Res.string.common_done))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WhatStep(
    state: LogMealState,
    viewModel: LogMealViewModel,
    onCreateIngredient: () -> Unit,
    /** Favourites and quick picks are shown: not while searching (only the results then), nor once folded away. */
    shortcuts: Boolean,
    searchFocus: FocusRequester,
    onSearchFocus: (Boolean) -> Unit,
    onAddMore: (() -> Unit)?,
) {
    val selectedIds = state.selected.map { it.ingredient.id }.toSet()
    val keyboard = LocalSoftwareKeyboardController.current
    val voiceLabel = stringResource(Res.string.meal_voice)
    // Phone test 21.11.: favourites and quick picks filled the screen, the search results disappeared below them.
    // While searching only the results show; otherwise each shortcut group is one scrollable row.
    StepCard(number = 2, title = stringResource(Res.string.meal_step_what), sidePadding = 10.dp) {
        val speech = rememberSpeechInput(Locale.current.toLanguageTag()) { text -> text?.let(viewModel::onVoiceResult) }
        // User note 22.11.2026: a soft search pill with magnifier and microphone, as in the design picture.
        TextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = { Text(stringResource(Res.string.meal_search), maxLines = 1, overflow = TextOverflow.Ellipsis) },
            singleLine = true,
            shape = CircleShape,
            colors = searchPillColors(),
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = speech?.let { start ->
                {
                    IconButton(onClick = start) { Icon(Icons.Outlined.Mic, contentDescription = voiceLabel) }
                }
            },
            modifier = Modifier.fillMaxWidth().focusRequester(searchFocus).onFocusChanged { onSearchFocus(it.isFocused) },
        )
        // Phone test 21.11.: one letter matched almost every saved dish (by name or ingredient) and the big chips
        // pushed the ingredients off the screen. Dishes from two letters on, at most three, as compact rows.
        // User 23.11.2026: searching "Olivenöl" must show the oil first, not "Linsensuppe" only because it contains it.
        // Dishes found by their own name come first, then the ingredients, then dishes found by an ingredient.
        val dishes = if (state.query.trim().length >= 2) state.dishResults.take(3) else emptyList()
        val reasons = dishes.associate { it.id to viewModel.dishReason(it, state.query) }
        val pickDish: (FavoriteInfo) -> Unit = { dish ->
            viewModel.onFavoriteClick(dish)
            keyboard?.hide()
        }
        dishes.filter { reasons[it.id] == null }.forEach { DishResultRow(it, null, pickDish) }
        state.results.forEach { ingredient ->
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable {
                        viewModel.onToggleIngredient(ingredient)
                        // Search is done; without the keyboard the choice is visible right away (self-test 18.11.).
                        keyboard?.hide()
                    }
                    .padding(vertical = 10.dp),
            ) {
                Text(ingredientName(ingredient))
                // Found through another name (e.g. "Topfen" for Quark): say so, like "contains …" for dishes.
                viewModel.synonymReason(ingredient.id, state.query)?.let { synonym ->
                    Text(stringResource(Res.string.meal_also_called, synonym), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        dishes.filter { reasons[it.id] != null }.forEach { DishResultRow(it, reasons[it.id], pickDish) }
        if (state.query.isNotBlank() && state.results.isEmpty() && dishes.isEmpty()) {
            Text(stringResource(Res.string.meal_no_results), style = MaterialTheme.typography.bodyMedium)
        }
        if (state.query.isNotBlank() && !state.nameExists) {
            TextButton(onClick = onCreateIngredient) {
                Text(stringResource(Res.string.custom_create_named, state.query.trim()))
            }
        }
        if (shortcuts && state.favorites.isNotEmpty()) {
            ChipGroupTitle(stringResource(Res.string.meal_favorites), stringResource(Res.string.meal_favorites_hint))
            // User note 21.11.2026: coming from the favourites tab, the chosen dish could sit far right, out of view.
            // The row scrolls to it.
            val favoritesRow = rememberLazyListState()
            LaunchedEffect(state.favoriteId) {
                val index = state.favorites.indexOfFirst { it.id == state.favoriteId }
                if (index >= 0) favoritesRow.animateScrollToItem(index)
            }
            ChipRowWithMore(favoritesRow) {
                items(state.favorites, key = { it.id }) { favorite ->
                    AppFilterChip(
                        // User 23.11.2026: every chosen dish is marked, by its colour only (no check mark).
                        selected = state.dishes.any { it.favoriteId == favorite.id },
                        showCheck = false,
                        onClick = { viewModel.onFavoriteClick(favorite) },
                        label = { Text(favorite.name, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 220.dp)) },
                        // Design picture 22.11.2026: a star after the name marks a saved dish.
                        trailingIcon = { Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    )
                }
            }
        }
        if (shortcuts && state.recent.isNotEmpty()) {
            ChipGroupTitle(stringResource(Res.string.meal_quick), stringResource(Res.string.meal_quick_hint))
            ChipRowWithMore {
                items(state.recent, key = { it.id }) { ingredient ->
                    AppFilterChip(
                        selected = ingredient.id in selectedIds,
                        showCheck = false,
                        onClick = { viewModel.onToggleIngredient(ingredient) },
                        label = { Text(ingredientName(ingredient), maxLines = 1) },
                        // Design picture 22.11.2026: "+" shows that a tap adds the ingredient.
                        trailingIcon = if (ingredient.id in selectedIds) null else {
                            { Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        },
                    )
                }
            }
        }
        ChosenIngredients(state, viewModel, onAddMore)
        if (state.containsKnownAllergy) {
            Text(stringResource(Res.string.meal_allergy_hint), color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
/**
 * User 24.11.2026: chosen dishes on top, each its own card with one portion for the whole dish (folds open to check
 * the ingredients), below them the single ingredients with their own portions.
 */
@Composable
private fun ColumnScope.ChosenIngredients(state: LogMealState, viewModel: LogMealViewModel, onAddMore: (() -> Unit)?) {
    val dishes = state.dishes.filter { state.dishInfo(it) != null }
    if (dishes.isEmpty() && state.selected.isEmpty()) return
    PortionLabel(hasDishes = dishes.isNotEmpty())
    // Newest first: what was just added appears on top.
    if (dishes.isNotEmpty()) {
        Text(stringResource(Res.string.meal_dishes_chosen, dishes.size), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 2.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            dishes.asReversed().forEach { dish -> DishCard(state, dish, viewModel) }
        }
    }
    if (state.selected.isNotEmpty()) {
        if (dishes.isNotEmpty()) {
            Text(stringResource(Res.string.meal_single_ingredients), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 6.dp))
        }
        Column {
            state.selected.asReversed().forEachIndexed { index, item ->
                if (index > 0) HorizontalDivider(color = AppColors.extra.home.cardBorder)
                SelectedRow(item, item.ingredient.id in state.needsPreparation, viewModel)
            }
        }
    }
    onAddMore?.let { add ->
        OutlinedButton(
            onClick = add,
            shape = CircleShape,
            border = BorderStroke(1.dp, AppColors.extra.home.ink),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.extra.home.ink),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(stringResource(Res.string.meal_add_more), modifier = Modifier.padding(start = 8.dp))
        }
    }
}

/**
 * A chosen dish (user 24.11.2026): name with the arrow and S/M/L for the whole dish next to it, then the bin. Folded open
 * it lists the ingredients, only with a bin each (no sizes). A narrow screen puts S/M/L below the name.
 */
@Composable
private fun DishCard(state: LogMealState, dish: ChosenDish, viewModel: LogMealViewModel) {
    val home = AppColors.extra.home
    val info = state.dishInfo(dish) ?: return
    var open by remember(dish.favoriteId) { mutableStateOf(false) }
    val ingredients = state.dishItems(dish).mapNotNull { item -> viewModel.ingredientInfo(item.ingredientId)?.let { it to item } }
    val action = stringResource(if (open) Res.string.meal_dish_hide_ingredients else Res.string.meal_dish_show_ingredients)
    val removeLabel = stringResource(Res.string.meal_remove_dish)
    val removeIngredient = stringResource(Res.string.meal_remove)
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = lerp(home.page, home.badge, 0.25f),
        contentColor = home.ink,
        border = BorderStroke(1.dp, home.cardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            val nameArea: @Composable (Modifier) -> Unit = { modifier ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = modifier.heightIn(min = 42.dp).clickable { open = !open }.semantics { contentDescription = "${info.name}. $action" },
                ) {
                    Text("🍲", fontSize = 18.sp)
                    // Phone test 24.11.: long names broke inside the word ("Linsens-uppe"); now hyphenated at syllables.
                    Text(info.name, style = MaterialTheme.typography.bodyMedium.merge(TextStyle(hyphens = Hyphens.Auto, lineBreak = LineBreak.Paragraph)), modifier = Modifier.weight(1f, fill = false).padding(start = 6.dp))
                    Icon(if (open) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown, contentDescription = null, modifier = Modifier.padding(start = 2.dp).size(26.dp))
                }
            }
            val bin: @Composable () -> Unit = {
                IconButton(onClick = { viewModel.onRemoveDish(dish.favoriteId) }, modifier = Modifier.size(44.dp).semantics { contentDescription = "$removeLabel: ${info.name}" }) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
            BoxWithConstraints(modifier = Modifier.padding(start = 12.dp, end = 4.dp)) {
                // Name, S/M/L and bin in one line when the name keeps at least ~90 dp.
                if (maxWidth >= 96.dp + 44.dp + 100.dp + 32.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        nameArea(Modifier.weight(1f))
                        PortionSwitch(dish.portion, { viewModel.onDishPortionChange(dish.favoriteId, it) })
                        bin()
                    }
                } else {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            nameArea(Modifier.weight(1f))
                            bin()
                        }
                        PortionSwitch(dish.portion, { viewModel.onDishPortionChange(dish.favoriteId, it) }, Modifier.padding(bottom = 6.dp))
                    }
                }
            }
            if (open) {
                ingredients.forEach { (ingredient, item) ->
                    val name = ingredientName(ingredient)
                    HorizontalDivider(color = home.cardBorder, modifier = Modifier.padding(horizontal = 12.dp))
                    Column(modifier = Modifier.padding(start = 20.dp, end = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(vertical = 4.dp))
                            IconButton(
                                onClick = { viewModel.onRemoveIngredient(ingredient.id, dish.favoriteId) },
                                modifier = Modifier.size(40.dp).semantics { contentDescription = "$removeIngredient: $name" },
                            ) { Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(20.dp)) }
                        }
                        if (ingredient.id in state.needsPreparation) {
                            Box(modifier = Modifier.padding(bottom = 4.dp)) {
                                PreparationChoice(dish.preparations[ingredient.id] ?: item.preparation) { viewModel.onDishPreparationChange(dish.favoriteId, ingredient.id, it) }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** "Zutaten der gesamten Mahlzeit" with the S/M/L explanation behind the info icon. */
@Composable
private fun PortionLabel(hasDishes: Boolean) {
    var showPortionHelp by remember { mutableStateOf(false) }
    val infoLabel = stringResource(Res.string.meal_portion_info)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { showPortionHelp = !showPortionHelp }
                .semantics { contentDescription = infoLabel }
                .padding(top = 4.dp),
        ) {
            // User 23.11.: a darker green than the soft sage, so the label reads well on the cream card.
            Icon(Icons.Outlined.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 6.dp).size(20.dp))
            Text(stringResource(Res.string.meal_ingredients_all), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f, fill = false))
            Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 6.dp).size(18.dp))
        }
        if (showPortionHelp) {
            Text(stringResource(Res.string.meal_portion_help), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (hasDishes) {
                Text(stringResource(Res.string.meal_portion_help_dish), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** The two questions of user 24.11.2026: adding an ingredient a dish already has, and removing one that sits in several places. */
@Composable
private fun IngredientQuestions(state: LogMealState, viewModel: LogMealViewModel) {
    state.pendingAdd?.let { pending ->
        val name = ingredientName(pending.ingredient)
        val dishNames = pending.dishIds.mapNotNull { id -> state.favorites.firstOrNull { it.id == id }?.name }.joinToString(", ")
        AlertDialog(
            onDismissRequest = viewModel::onDeclineAdd,
            text = { Text(stringResource(Res.string.meal_dup_add_question, name, dishNames)) },
            confirmButton = { Button(onClick = viewModel::onConfirmAddAnyway) { Text(stringResource(Res.string.meal_dup_add_yes)) } },
            dismissButton = { TextButton(onClick = viewModel::onDeclineAdd) { Text(stringResource(Res.string.meal_dup_add_no)) } },
        )
    }
    state.pendingRemove?.let { pending ->
        val name = ingredientName(pending.ingredient)
        val single = stringResource(Res.string.meal_place_single)
        val places = pending.places.map { place ->
            when (place) {
                Place.Single -> single
                is Place.InDish -> state.favorites.firstOrNull { it.id == place.favoriteId }?.name.orEmpty()
            }
        }
        if (places.size == 1) {
            AlertDialog(
                onDismissRequest = viewModel::onRemoveCancelled,
                text = { Text(stringResource(Res.string.meal_remove_question, name)) },
                confirmButton = { Button(onClick = viewModel::onRemoveEverywhere) { Text(stringResource(Res.string.meal_remove)) } },
                dismissButton = { TextButton(onClick = viewModel::onRemoveCancelled) { Text(stringResource(Res.string.common_cancel)) } },
            )
            return
        }
        AlertDialog(
            onDismissRequest = viewModel::onRemoveCancelled,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(Res.string.meal_multi_question, name, places.size))
                    places.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                }
            },
            confirmButton = {
                Column(horizontalAlignment = Alignment.End) {
                    Button(onClick = viewModel::onRemoveEverywhere) { Text(stringResource(Res.string.meal_multi_all)) }
                    TextButton(onClick = viewModel::onRemoveHereOnly) { Text(stringResource(Res.string.meal_multi_here)) }
                    TextButton(onClick = viewModel::onRemoveCancelled) { Text(stringResource(Res.string.common_cancel)) }
                }
            },
        )
    }
}

@Composable
private fun SaveAsDishCard(state: LogMealState, viewModel: LogMealViewModel) {
    val home = AppColors.extra.home
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = home.card, contentColor = home.ink),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, home.cardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 12.dp)) {
            // User note 21.11.2026: "Als Favorit speichern" read as if only one ingredient were saved. The text names
            // the whole meal as a dish, the hint says what happens.
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.clickable { viewModel.onSaveAsFavoriteChange(!state.saveAsFavorite) }) {
                Checkbox(checked = state.saveAsFavorite, onCheckedChange = viewModel::onSaveAsFavoriteChange)
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(stringResource(Res.string.meal_save_as_favorite), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(Res.string.meal_save_as_favorite_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (state.saveAsFavorite) {
                OutlinedTextField(
                    value = state.favoriteName,
                    onValueChange = viewModel::onFavoriteNameChange,
                    label = { Text(stringResource(Res.string.meal_favorite_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 8.dp),
                )
            }
        }
    }
}

/** User note 22.11.2026: a short label over each chip row as in the design picture; the hint stays for screen readers. */
@Composable
private fun ChipGroupTitle(title: String, hint: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            // Closer to its chip row: the gap of the card would otherwise sit between label and chips.
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height - 6.dp.roundToPx()) { placeable.place(0, 0) }
            }
            .semantics { contentDescription = "$title. $hint" },
    )
}

@Composable
private fun SelectedRow(item: SelectedIngredient, needsPreparation: Boolean, viewModel: LogMealViewModel) {
    IngredientRow(
        ingredient = item.ingredient,
        name = ingredientName(item.ingredient),
        portion = item.portion,
        onPortion = { viewModel.onPortionChange(item.ingredient.id, it) },
        onRemove = { viewModel.onRemoveIngredient(item.ingredient.id, null) },
        preparation = if (needsPreparation) {
            { PreparationChoice(item.preparation) { viewModel.onPreparationChange(item.ingredient.id, it) } }
        } else {
            null
        },
    )
}

@Composable
fun portionLabel(portion: Portion): String = stringResource(
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


/** A dish in the search results; [reason] is the ingredient it was found by ("enthält Olivenöl"), if any. */
@Composable
private fun DishResultRow(dish: FavoriteInfo, reason: String?, onClick: (FavoriteInfo) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable { onClick(dish) }
            .padding(vertical = 10.dp),
    ) {
        Icon(Icons.Outlined.StarOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(dish.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
            reason?.let {
                Text(stringResource(Res.string.meal_dish_contains, it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}
