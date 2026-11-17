package com.nfar.allergydetective.ui.meal

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.custom_category
import allergydetective.shared.generated.resources.custom_create
import allergydetective.shared.generated.resources.custom_name
import allergydetective.shared.generated.resources.custom_similar
import allergydetective.shared.generated.resources.custom_similar_chosen
import allergydetective.shared.generated.resources.custom_title
import allergydetective.shared.generated.resources.custom_triggers
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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

/** "New ingredient": name, category, "similar to …" and the triggers taken from it. */
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
                OutlinedTextField(
                    value = state.baseQuery,
                    onValueChange = viewModel::onBaseQueryChange,
                    label = { Text(stringResource(Res.string.custom_similar)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                state.baseResults.forEach { candidate ->
                    Text(
                        ingredientName(candidate),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable { viewModel.onBaseSelected(candidate) }
                            .padding(vertical = 12.dp),
                    )
                }
                state.base?.let {
                    Text(stringResource(Res.string.custom_similar_chosen, ingredientName(it)), style = MaterialTheme.typography.bodySmall)
                }
                Text(stringResource(Res.string.custom_category), style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.categories.forEach { category ->
                        FilterChip(
                            selected = state.categoryId == category.id,
                            onClick = { viewModel.onCategoryChange(category.id) },
                            label = { Text(displayName(NamePrefix.CATEGORY, category.key)) },
                        )
                    }
                }
                Text(stringResource(Res.string.custom_triggers), style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.properties.forEach { property ->
                        FilterChip(
                            selected = property.id in state.propertyIds,
                            onClick = { viewModel.onPropertyToggle(property.id) },
                            label = { Text(displayName(NamePrefix.PROPERTY, property.key)) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = viewModel::onCreate, enabled = state.canCreate) { Text(stringResource(Res.string.custom_create)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } },
    )
}
