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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.nfar.allergydetective.ui.meal.CustomIngredientViewModel
import com.nfar.allergydetective.ui.meal.IngredientPicker
import com.nfar.allergydetective.ui.meal.PreparationChoice
import com.nfar.allergydetective.ui.meal.ingredientName
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = koinViewModel(),
    editor: DishEditorViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showEditor by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.refresh() }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            OutlinedButton(
                onClick = {
                    editor.start()
                    showEditor = true
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text(stringResource(Res.string.favorites_add)) }
        }
        if (!state.loading && state.favorites.isEmpty()) {
            item { Text(stringResource(Res.string.favorites_empty)) }
        }
        items(state.favorites, key = { it.id }) { favorite ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(favorite.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        favorite.items.mapNotNull { state.ingredients[it.ingredientId] }.map { ingredientName(it) }.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = {
                    editor.start(favorite)
                    showEditor = true
                }) { Text(stringResource(Res.string.favorites_edit)) }
                TextButton(onClick = { viewModel.onDelete(favorite.id) }) { Text(stringResource(Res.string.history_delete)) }
            }
        }
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
