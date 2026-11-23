package com.nfar.allergydetective.ui.favorites

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
import allergydetective.shared.generated.resources.favorites_more
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.foundation.layout.size
import allergydetective.shared.generated.resources.favorites_log
import allergydetective.shared.generated.resources.favorites_no_match
import allergydetective.shared.generated.resources.favorites_search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.nfar.allergydetective.ui.common.StatusBadge
import com.nfar.allergydetective.ui.common.StatusKind
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
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.favorites.isNotEmpty()) {
            item {
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    StatusBadge(pluralStringResource(Res.plurals.favorites_count, state.favorites.size, state.favorites.size), StatusKind.OPEN)
                }
            }
            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = { Text(stringResource(Res.string.favorites_search)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
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
        item {
            FilledTonalButton(
                onClick = {
                    editor.start()
                    showEditor = true
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) { Text(stringResource(Res.string.favorites_add)) }
        }
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
private fun DishCard(name: String, ingredients: List<String>, onLog: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 4.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text(
                ingredients.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, end = 8.dp),
            )
            HorizontalDivider(modifier = Modifier.padding(top = 12.dp, end = 8.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh)
            // User note 21.11.2026: "Erfassen" in every row read as "add a dish below this row". The one action
            // now says what it does ("Als Mahlzeit eintragen"); "Bearbeiten" moved into the "…" menu with "Löschen".
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)) {
                FilledTonalButton(onClick = onLog, contentPadding = PaddingValues(horizontal = 14.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        stringResource(Res.string.favorites_log),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.MoreHoriz, contentDescription = stringResource(Res.string.favorites_more))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.favorites_edit)) },
                            onClick = {
                                menuOpen = false
                                onEdit()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.history_delete), color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            },
                        )
                    }
                }
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
