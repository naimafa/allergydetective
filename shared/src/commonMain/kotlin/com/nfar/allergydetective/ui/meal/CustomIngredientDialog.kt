package com.nfar.allergydetective.ui.meal

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.custom_category_fallback
import allergydetective.shared.generated.resources.custom_contains
import allergydetective.shared.generated.resources.custom_contains_hint
import allergydetective.shared.generated.resources.custom_create
import allergydetective.shared.generated.resources.custom_create_anyway
import allergydetective.shared.generated.resources.custom_duplicate
import allergydetective.shared.generated.resources.custom_similar_question
import allergydetective.shared.generated.resources.custom_use_existing
import allergydetective.shared.generated.resources.custom_derived
import allergydetective.shared.generated.resources.custom_name
import allergydetective.shared.generated.resources.custom_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.displayName
import org.jetbrains.compose.resources.stringResource

/** "New ingredient": name and what it contains; category and triggers follow from that. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomIngredientDialog(
    viewModel: CustomIngredientViewModel,
    onCreated: (IngredientInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.created) {
        state.created?.let {
            viewModel.onCreatedHandled()
            onCreated(it)
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.custom_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text(stringResource(Res.string.custom_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                state.duplicate?.let { existing ->
                    NameHint(stringResource(Res.string.custom_duplicate, ingredientName(existing)), viewModel::onUseExisting)
                }
                state.similar?.takeIf { state.askSimilar }?.let { similar ->
                    NameHint(stringResource(Res.string.custom_similar_question, ingredientName(similar)), viewModel::onUseExisting)
                }
                Text(stringResource(Res.string.custom_contains), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(Res.string.custom_contains_hint), style = MaterialTheme.typography.bodySmall)
                IngredientPicker(
                    query = state.query,
                    results = state.results,
                    selected = state.components,
                    onQueryChange = viewModel::onQueryChange,
                    onAdd = viewModel::onAddComponent,
                    onRemove = viewModel::onRemoveComponent,
                )
                if (state.propertyIds.isNotEmpty()) {
                    val names = state.properties.filter { it.id in state.propertyIds }
                        .map { displayName(NamePrefix.PROPERTY, it.key) }
                    Text(stringResource(Res.string.custom_derived, names.joinToString(", ")), style = MaterialTheme.typography.bodySmall)
                }
                if (state.components.isEmpty()) {
                    // Only when nothing fits: a plain ingredient without known triggers.
                    Text(stringResource(Res.string.custom_category_fallback), style = MaterialTheme.typography.bodySmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.categories.forEach { category ->
                            FilterChip(
                                selected = state.chosenCategoryId == category.id,
                                onClick = { viewModel.onCategoryChange(category.id) },
                                label = { Text(displayName(NamePrefix.CATEGORY, category.key)) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = viewModel::onCreate, enabled = state.canCreate) {
                Text(stringResource(if (state.askSimilar) Res.string.custom_create_anyway else Res.string.custom_create))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } },
    )
}

/** "Already exists" / "Did you mean …?" with a button to take the existing ingredient. */
@Composable
private fun NameHint(text: String, onUse: () -> Unit) {
    Column {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onUse) { Text(stringResource(Res.string.custom_use_existing)) }
    }
}
