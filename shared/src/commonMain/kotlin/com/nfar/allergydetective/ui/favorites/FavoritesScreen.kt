package com.nfar.allergydetective.ui.favorites

import allergydetective.shared.generated.resources.favorites_drag
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.common_save
import allergydetective.shared.generated.resources.custom_create_named
import allergydetective.shared.generated.resources.favorites_add
import allergydetective.shared.generated.resources.favorites_edit
import allergydetective.shared.generated.resources.favorites_edit_title
import allergydetective.shared.generated.resources.favorites_editor_title
import allergydetective.shared.generated.resources.favorites_empty
import allergydetective.shared.generated.resources.favorites_ingredients
import allergydetective.shared.generated.resources.favorites_name
import allergydetective.shared.generated.resources.favorites_replace
import allergydetective.shared.generated.resources.favorites_same_name
import allergydetective.shared.generated.resources.history_delete
import allergydetective.shared.generated.resources.favorites_count
import allergydetective.shared.generated.resources.favorites_delete_question
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.TextField
import androidx.compose.material3.ButtonDefaults
import com.nfar.allergydetective.ui.common.searchPillColors
import com.nfar.allergydetective.ui.theme.AppColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.foundation.layout.size
import allergydetective.shared.generated.resources.favorites_log
import allergydetective.shared.generated.resources.favorites_no_match
import allergydetective.shared.generated.resources.favorites_search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.ui.meal.CustomIngredientDialog
import org.jetbrains.compose.resources.pluralStringResource
import com.nfar.allergydetective.ui.meal.CustomIngredientViewModel
import com.nfar.allergydetective.ui.meal.IngredientPicker
import com.nfar.allergydetective.ui.meal.PreparationChoice
import com.nfar.allergydetective.ui.meal.ingredientName
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoritesScreen(
    /** "Log it": opens the food log with this dish (self-test 19.11.). */
    onLogDish: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = koinViewModel(),
    editor: DishEditorViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showEditor by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<FavoriteInfo?>(null) }
    LaunchedEffect(Unit) { viewModel.refresh() }
    val listState = rememberLazyListState()
    val reorder = rememberDragToReorderState(listState, viewModel::onMove, viewModel::onMoveDone)
    // While searching only part of the list shows: sorting is for the whole list.
    val canReorder = state.query.isBlank() && state.favorites.size > 1
    Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        // Room below the last card, so the floating button does not cover it.
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.favorites.isNotEmpty()) {
            item {
                // Design picture 22.11.2026: the same round search pill as in the meal screen.
                TextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = { Text(stringResource(Res.string.favorites_search), maxLines = 1) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    shape = CircleShape,
                    colors = searchPillColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            // Design picture 22.11.2026: a small heading over the dishes instead of a badge in its own row.
            item {
                Text(
                    pluralStringResource(Res.plurals.favorites_count, state.favorites.size, state.favorites.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            if (state.visibleFavorites.isEmpty()) {
                item { Text(stringResource(Res.string.favorites_no_match), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        if (!state.loading && state.favorites.isEmpty()) {
            item { Text(stringResource(Res.string.favorites_empty)) }
        }
        items(state.visibleFavorites, key = { it.id }) { favorite ->
            DishCard(
                modifier = Modifier.reorderableItem(reorder, favorite.id),
                handle = if (canReorder) Modifier.dragHandle(reorder, favorite.id) else null,
                name = favorite.name,
                ingredients = favorite.items.mapNotNull { state.ingredients[it.ingredientId] }.map { ingredientName(it) },
                onLog = { onLogDish(favorite.id) },
                onEdit = {
                    editor.start(favorite)
                    showEditor = true
                },
                onDelete = { toDelete = favorite },
            )
        }
    }
    // User 23.11.2026: the button at the end of the list was not visible with many dishes. It now floats at the
    // bottom right and is always there.
    ExtendedFloatingActionButton(
        onClick = {
            editor.start()
            showEditor = true
        },
        icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
        text = { Text(stringResource(Res.string.favorites_add)) },
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
    )
    }
    // Feedback 19.11.: every delete asks first.
    toDelete?.let { dish ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            text = { Text(stringResource(Res.string.favorites_delete_question, dish.name)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.onDelete(dish.id)
                    toDelete = null
                }) { Text(stringResource(Res.string.history_delete)) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text(stringResource(Res.string.common_cancel)) } },
        )
    }
    if (showEditor) {
        DishEditorDialog(
            viewModel = editor,
            onDone = {
                showEditor = false
                viewModel.refresh()
            },
        )
    }
}

/** Design of 19.11.: name, ingredients separated by dots; one action "log as meal", "Edit" and "Delete" behind "…". */
@Composable
private fun DishCard(
    name: String,
    ingredients: List<String>,
    onLog: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    /** Drag handle (user note 21.11.2026: sort dishes with the finger); null while searching. */
    handle: Modifier? = null,
) {
    // User note 22.11.2026: the card of the start screen (cream white, fine border, soft shadow).
    val home = AppColors.extra.home
    Card(
        colors = CardDefaults.cardColors(containerColor = home.card, contentColor = home.ink),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, home.cardBorder),
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 16.dp)) {
            // Design picture 22.11.2026: name with the actions at the top right, ingredients, then one wide button.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).padding(top = 8.dp))
                // User note 22.11.2026: from the right: drag handle, edit, delete (no more "…" menu).
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(Res.string.history_delete), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = stringResource(Res.string.favorites_edit), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                }
                if (handle != null) {
                    Icon(
                        Icons.Outlined.DragIndicator,
                        contentDescription = stringResource(Res.string.favorites_drag),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = handle.size(40.dp).padding(8.dp),
                    )
                }
            }
            Text(
                ingredients.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, end = 12.dp),
            )
            // User note 21.11.2026: the one action says what it does ("Als Mahlzeit eintragen").
            FilledTonalButton(
                onClick = onLog,
                shape = CircleShape,
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = AppColors.extra.home.badge, contentColor = AppColors.extra.home.accent),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, end = 12.dp).heightIn(min = 48.dp),
            ) {
                Icon(Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    stringResource(Res.string.favorites_log),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun DishEditorDialog(
    viewModel: DishEditorViewModel,
    onDone: () -> Unit,
    customViewModel: CustomIngredientViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showCustom by remember { mutableStateOf(false) }
    LaunchedEffect(state.saved) { if (state.saved) onDone() }
    AlertDialog(
        onDismissRequest = onDone,
        title = { Text(stringResource(if (state.editingId != null) Res.string.favorites_edit_title else Res.string.favorites_editor_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text(stringResource(Res.string.favorites_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                state.sameNameDish?.let {
                    Text(
                        stringResource(Res.string.favorites_same_name, it.name),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Text(stringResource(Res.string.favorites_ingredients), style = MaterialTheme.typography.titleSmall)
                IngredientPicker(
                    query = state.query,
                    results = state.results,
                    selected = state.ingredients,
                    onQueryChange = viewModel::onQueryChange,
                    onAdd = viewModel::onAdd,
                    onRemove = viewModel::onRemove,
                    dishes = state.dishResults,
                    onAddDish = viewModel::onAddDish,
                    findsDishes = true,
                )
                // Not found: create it here, the duplicate check runs in the dialog.
                if (state.query.isNotBlank()) {
                    TextButton(onClick = {
                        customViewModel.start(state.query)
                        showCustom = true
                    }) { Text(stringResource(Res.string.custom_create_named, state.query.trim())) }
                }
                state.ingredients.filter { it.id in state.needsPreparation }.forEach { ingredient ->
                    Text(ingredientName(ingredient), style = MaterialTheme.typography.titleSmall)
                    PreparationChoice(state.preparations[ingredient.id]) { viewModel.onPreparationChange(ingredient.id, it) }
                }
            }
        },
        confirmButton = {
            Button(onClick = viewModel::onSave, enabled = state.canSave) {
                Text(stringResource(if (state.sameNameDish != null) Res.string.favorites_replace else Res.string.common_save))
            }
        },
        dismissButton = { TextButton(onClick = onDone) { Text(stringResource(Res.string.common_cancel)) } },
    )
    if (showCustom) {
        CustomIngredientDialog(
            viewModel = customViewModel,
            onCreated = {
                showCustom = false
                viewModel.onIngredientCreated(it)
            },
            onDismiss = { showCustom = false },
        )
    }
}
