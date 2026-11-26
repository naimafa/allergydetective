package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.common_save
import allergydetective.shared.generated.resources.custom_duplicate
import allergydetective.shared.generated.resources.custom_name
import allergydetective.shared.generated.resources.favorites_edit
import allergydetective.shared.generated.resources.ingredient_search
import allergydetective.shared.generated.resources.my_ingredients_add_contents
import allergydetective.shared.generated.resources.my_ingredients_contains
import allergydetective.shared.generated.resources.my_ingredients_contains_none
import allergydetective.shared.generated.resources.my_ingredients_delete
import allergydetective.shared.generated.resources.my_ingredients_delete_question
import allergydetective.shared.generated.resources.my_ingredients_edit
import allergydetective.shared.generated.resources.my_ingredients_empty
import allergydetective.shared.generated.resources.my_ingredients_intro
import allergydetective.shared.generated.resources.my_ingredients_kind
import allergydetective.shared.generated.resources.my_ingredients_replace_text
import allergydetective.shared.generated.resources.my_ingredients_replace_title
import allergydetective.shared.generated.resources.my_ingredients_replace_with
import allergydetective.shared.generated.resources.my_ingredients_title
import allergydetective.shared.generated.resources.my_ingredients_unused
import allergydetective.shared.generated.resources.my_ingredients_used
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.domain.diary.CustomIngredient
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.ui.common.AppFilterChip
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.ScreenScaffold
import com.nfar.allergydetective.ui.common.StepCard
import com.nfar.allergydetective.ui.common.displayName
import com.nfar.allergydetective.ui.meal.IngredientBadge
import com.nfar.allergydetective.ui.meal.ingredientName
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * "Meine Zutaten" (user 25.11.2026): own ingredients with their picture and how often they are used; edit them,
 * delete an unused one, replace a used one ("Wodurch ersetzen?") so nothing logged is lost.
 */
@Composable
fun MyIngredientsScreen(onBack: () -> Unit, viewModel: MyIngredientsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    ScreenScaffold(
        title = stringResource(Res.string.my_ingredients_title),
        titleIcon = Icons.Outlined.Restaurant,
        onBack = onBack,
        snackbarHostState = snackbar,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text(stringResource(Res.string.my_ingredients_intro), style = MaterialTheme.typography.bodyMedium) }
            if (!state.loading && state.items.isEmpty()) {
                item { Text(stringResource(Res.string.my_ingredients_empty), style = MaterialTheme.typography.bodyLarge) }
            }
            items(state.items, key = { it.id }) { item ->
                IngredientLine(item, onEdit = { viewModel.onEdit(item) }, onDelete = { viewModel.onDelete(item) })
            }
        }
    }
    state.editing?.let { EditDialog(state, viewModel) }
    state.deleting?.let { item ->
        AlertDialog(
            onDismissRequest = viewModel::onCancelDelete,
            text = { Text(stringResource(Res.string.my_ingredients_delete_question, item.name)) },
            confirmButton = { Button(onClick = viewModel::onConfirmDelete) { Text(stringResource(Res.string.my_ingredients_delete)) } },
            dismissButton = { TextButton(onClick = viewModel::onCancelDelete) { Text(stringResource(Res.string.common_cancel)) } },
        )
    }
    state.replacing?.let { ReplaceDialog(it.ingredient, it.query, it.results, it.target, viewModel) }
}

@Composable
private fun IngredientLine(item: CustomIngredient, onEdit: () -> Unit, onDelete: () -> Unit) {
    StepCard(title = item.name) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IngredientBadge(item.info, item.name)
            Text(
                if (item.used) stringResource(Res.string.my_ingredients_used, item.mealCount, item.dishCount) else stringResource(Res.string.my_ingredients_unused),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f).padding(start = 10.dp),
            )
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, contentDescription = stringResource(Res.string.favorites_edit), modifier = Modifier.size(22.dp)) }
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, contentDescription = stringResource(Res.string.my_ingredients_delete), modifier = Modifier.size(22.dp)) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditDialog(state: MyIngredientsState, viewModel: MyIngredientsViewModel) {
    val edit = state.editing ?: return
    AlertDialog(
        onDismissRequest = viewModel::onCancelEdit,
        title = { Text(stringResource(Res.string.my_ingredients_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = edit.name,
                    onValueChange = viewModel::onEditName,
                    label = { Text(stringResource(Res.string.custom_name)) },
                    singleLine = true,
                    isError = edit.duplicate != null,
                    modifier = Modifier.fillMaxWidth(),
                )
                edit.duplicate?.let {
                    Text(stringResource(Res.string.custom_duplicate, ingredientName(it)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                Text(stringResource(Res.string.my_ingredients_kind), style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.categories.forEach { category ->
                        AppFilterChip(
                            selected = category.id == edit.categoryId,
                            onClick = { viewModel.onEditCategory(category.id) },
                            label = { Text(displayName(NamePrefix.CATEGORY, category.key)) },
                        )
                    }
                }
                Text(stringResource(Res.string.my_ingredients_contains), style = MaterialTheme.typography.titleSmall)
                val contained = state.properties.filter { it.id in edit.propertyIds }
                if (contained.isEmpty()) {
                    Text(stringResource(Res.string.my_ingredients_contains_none), style = MaterialTheme.typography.bodySmall)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    contained.forEach { property ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.onRemoveProperty(property.id) },
                            label = { Text("${displayName(NamePrefix.PROPERTY, property.key)}  ✕") },
                        )
                    }
                }
                SearchField(edit.query, stringResource(Res.string.my_ingredients_add_contents), edit.results, viewModel::onEditQuery, viewModel::onAddContents)
            }
        },
        confirmButton = { Button(onClick = viewModel::onSaveEdit, enabled = edit.canSave) { Text(stringResource(Res.string.common_save)) } },
        dismissButton = { TextButton(onClick = viewModel::onCancelEdit) { Text(stringResource(Res.string.common_cancel)) } },
    )
}

@Composable
private fun ReplaceDialog(
    ingredient: CustomIngredient,
    query: String,
    results: List<IngredientInfo>,
    target: IngredientInfo?,
    viewModel: MyIngredientsViewModel,
) {
    AlertDialog(
        onDismissRequest = viewModel::onCancelReplace,
        title = { Text(stringResource(Res.string.my_ingredients_replace_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(Res.string.my_ingredients_replace_text, ingredient.name), style = MaterialTheme.typography.bodyMedium)
                target?.let { chosen ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IngredientBadge(chosen, ingredientName(chosen))
                        Text(ingredientName(chosen), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 10.dp))
                    }
                }
                SearchField(query, stringResource(Res.string.ingredient_search), results, viewModel::onReplaceQuery, viewModel::onReplaceTarget)
            }
        },
        confirmButton = {
            Button(onClick = viewModel::onConfirmReplace, enabled = target != null) {
                Text(target?.let { stringResource(Res.string.my_ingredients_replace_with, ingredientName(it)) } ?: stringResource(Res.string.my_ingredients_delete))
            }
        },
        dismissButton = { TextButton(onClick = viewModel::onCancelReplace) { Text(stringResource(Res.string.common_cancel)) } },
    )
}

/** A search field with its hits below; a tap on a hit picks it. */
@Composable
private fun SearchField(query: String, label: String, results: List<IngredientInfo>, onQuery: (String) -> Unit, onPick: (IngredientInfo) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    results.forEach { result ->
        val name = ingredientName(result)
        TextButton(onClick = { onPick(result) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IngredientBadge(result, name)
                Text(name, modifier = Modifier.padding(start = 10.dp))
            }
        }
    }
}
